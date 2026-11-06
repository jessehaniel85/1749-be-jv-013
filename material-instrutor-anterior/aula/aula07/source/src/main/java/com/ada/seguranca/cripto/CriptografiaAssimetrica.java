package com.ada.seguranca.cripto; // pacote onde ficam as classes de criptografia desta aula

import org.springframework.stereotype.Service; // marca a classe como um bean gerenciado pelo Spring

import javax.crypto.Cipher;                    // executa a cifragem e a decifragem
import javax.crypto.spec.OAEPParameterSpec;    // parâmetros do preenchimento OAEP do RSA
import javax.crypto.spec.PSource;              // "rótulo" opcional do OAEP (aqui, o padrão: vazio)
import java.nio.charset.StandardCharsets;      // UTF-8, para converter String <-> bytes
import java.security.GeneralSecurityException; // exceção base dos erros de criptografia
import java.security.KeyPair;                  // guarda o par de chaves: pública + privada
import java.security.KeyPairGenerator;         // gera pares de chaves
import java.security.Signature;                // assina e verifica assinaturas digitais
import java.security.spec.MGF1ParameterSpec;   // função MGF1 usada pelo OAEP (aqui, com SHA-256)
import java.util.Base64;                       // transforma bytes em texto, para trafegar em JSON

/**
 * Criptografia ASSIMÉTRICA: um PAR de chaves (RSA-2048).
 *
 * - A chave PÚBLICA pode ser distribuída a qualquer pessoa e CIFRA.
 * - A chave PRIVADA nunca sai do dono e DECIFRA.
 * - Para ASSINAR é o contrário: a privada assina e qualquer um verifica com a pública.
 *
 * Limite do RSA: só cifra textos pequenos (no máximo 190 bytes com RSA-2048 e OAEP/SHA-256).
 * Por isso, na prática (inclusive no HTTPS), o RSA protege uma chave simétrica e o AES
 * cifra os dados, que são rápidos e grandes.
 *
 * Neste exemplo o par é gerado ao subir a aplicação. A chave privada fica só na memória e
 * não é exposta por nenhum endpoint.
 */
@Service // o Spring cria uma única instância desta classe e a injeta onde for pedida
public class CriptografiaAssimetrica {

    /**
     * Tamanho máximo, em bytes, do texto que o RSA-2048 com OAEP/SHA-256 consegue cifrar.
     * Conta: 256 bytes da chave - 2 x 32 bytes do SHA-256 - 2 bytes de controle = 190.
     */
    public static final int TAMANHO_MAXIMO_BYTES = 190;

    // "RSA" é o algoritmo; "OAEPPadding" é o preenchimento moderno e seguro
    // (o antigo PKCS#1 v1.5 tem ataques conhecidos); "ECB" aqui é só um nome de
    // compatibilidade, o RSA não cifra em blocos encadeados
    private static final String CIFRA = "RSA/ECB/OAEPPadding";

    // algoritmo da assinatura: calcula o hash SHA-256 do texto e o assina com RSA
    private static final String ASSINATURA = "SHA256withRSA";

    // o par de chaves: a pública cifra e verifica, a privada decifra e assina
    private final KeyPair par;

    public CriptografiaAssimetrica() throws GeneralSecurityException {
        // pede ao Java um gerador de pares de chaves RSA
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        // define o tamanho da chave: 2048 bits (o mínimo recomendado hoje)
        gerador.initialize(2048);
        // gera o par; a privada só existe na memória, enquanto a aplicação roda
        this.par = gerador.generateKeyPair();
    }

    /** Chave pública em Base64 (formato X.509), pronta para ser compartilhada. */
    public String chavePublicaBase64() {
        // getEncoded() devolve a chave pública em bytes (formato X.509); Base64 para virar texto
        return Base64.getEncoder().encodeToString(par.getPublic().getEncoded());
    }

    /** Cifra com a chave PÚBLICA. */
    public String cifrar(String texto) {
        // converte o texto em bytes; o limite do RSA é medido em bytes, não em caracteres
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        // recusa textos grandes demais, com uma mensagem clara em vez de um erro interno
        if (bytes.length > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException(
                    "O RSA-2048 só cifra até " + TAMANHO_MAXIMO_BYTES + " bytes por vez");
        }
        try {
            // obtém o objeto que cifra, configurado para RSA com OAEP
            Cipher cifra = Cipher.getInstance(CIFRA);
            // prepara para CIFRAR usando a chave PÚBLICA e os parâmetros do OAEP
            cifra.init(Cipher.ENCRYPT_MODE, par.getPublic(), parametrosOaep());
            // cifra e converte o resultado para Base64
            return Base64.getEncoder().encodeToString(cifra.doFinal(bytes));
        } catch (GeneralSecurityException e) {
            // falha inesperada (algoritmo indisponível, por exemplo): é um erro da aplicação
            throw new IllegalStateException("Falha ao cifrar", e);
        }
    }

    /** Decifra com a chave PRIVADA. */
    public String decifrar(String base64) {
        try {
            // desfaz o Base64 e volta aos bytes cifrados
            byte[] dados = Base64.getDecoder().decode(base64);
            // obtém o objeto que decifra, com o mesmo algoritmo usado para cifrar
            Cipher cifra = Cipher.getInstance(CIFRA);
            // prepara para DECIFRAR usando a chave PRIVADA (só quem a tem consegue)
            cifra.init(Cipher.DECRYPT_MODE, par.getPrivate(), parametrosOaep());
            // decifra e converte os bytes de volta para texto
            return new String(cifra.doFinal(dados), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // texto cifrado com outra chave, adulterado ou Base64 inválido: não revelar o motivo
            throw new TextoCifradoInvalidoException();
        }
    }

    /** Assina com a chave PRIVADA: prova de quem escreveu e de que o texto não foi alterado. */
    public String assinar(String texto) {
        try {
            // obtém o objeto que assina: SHA-256 + RSA
            Signature assinatura = Signature.getInstance(ASSINATURA);
            // prepara para ASSINAR com a chave PRIVADA (só o dono pode assinar)
            assinatura.initSign(par.getPrivate());
            // informa o texto a ser assinado
            assinatura.update(texto.getBytes(StandardCharsets.UTF_8));
            // calcula a assinatura e converte para Base64
            return Base64.getEncoder().encodeToString(assinatura.sign());
        } catch (GeneralSecurityException e) {
            // falha inesperada: é um erro da aplicação
            throw new IllegalStateException("Falha ao assinar", e);
        }
    }

    /** Verifica com a chave PÚBLICA. */
    public boolean verificar(String texto, String assinaturaBase64) {
        try {
            // obtém o objeto que verifica, com o mesmo algoritmo usado para assinar
            Signature assinatura = Signature.getInstance(ASSINATURA);
            // prepara para VERIFICAR com a chave PÚBLICA (qualquer pessoa pode fazer isso)
            assinatura.initVerify(par.getPublic());
            // informa o texto que se quer conferir
            assinatura.update(texto.getBytes(StandardCharsets.UTF_8));
            // true se a assinatura bate com o texto; basta mudar um caractere para dar false
            return assinatura.verify(Base64.getDecoder().decode(assinaturaBase64));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // assinatura malformada ou Base64 inválido: tratar como assinatura inválida
            return false;
        }
    }

    // OAEP com SHA-256 também na função MGF1 (o padrão do Java usaria SHA-1 nela)
    private OAEPParameterSpec parametrosOaep() {
        // 1º: hash do OAEP (SHA-256); 2º: nome da função de máscara (MGF1);
        // 3º: o hash usado dentro da MGF1 (SHA-256); 4º: rótulo opcional (vazio)
        return new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    }
}
