package com.ada.seguranca.cripto; // pacote onde ficam as classes de criptografia desta aula

import javax.crypto.Cipher;                    // executa a cifragem e a decifragem
import javax.crypto.KeyGenerator;              // gera chaves secretas aleatórias
import javax.crypto.SecretKey;                 // representa uma chave simétrica
import javax.crypto.spec.GCMParameterSpec;     // parâmetros do modo GCM: tamanho da tag e IV
import java.nio.ByteBuffer;                    // junta arrays de bytes (IV + texto cifrado)
import java.nio.charset.StandardCharsets;      // UTF-8, para converter String <-> bytes
import java.security.GeneralSecurityException; // exceção base dos erros de criptografia
import java.security.SecureRandom;             // gerador de números aleatórios criptograficamente seguro
import java.util.Base64;                       // transforma bytes em texto, para trafegar em JSON

import org.springframework.stereotype.Service; // marca a classe como um bean gerenciado pelo Spring

/**
 * Criptografia SIMÉTRICA: uma única chave cifra e decifra (AES-256 no modo GCM).
 *
 * O GCM, além de cifrar, autentica: se o texto cifrado for alterado em um único bit,
 * a decifragem falha (integridade). O IV (vetor de inicialização) é aleatório a cada
 * cifragem, não é secreto e viaja junto com o texto cifrado.
 *
 * Neste exemplo a chave é gerada ao subir a aplicação: reiniciar a aplicação torna
 * impossível decifrar o que foi cifrado antes. Em um sistema real a chave vem de
 * configuração externa (variável de ambiente ou cofre de segredos), nunca do código.
 */
@Service // o Spring cria uma única instância desta classe e a injeta onde for pedida
public class CriptografiaSimetrica {

    // "AES" é o algoritmo, "GCM" é o modo de operação (cifra e autentica) e
    // "NoPadding" porque o GCM não precisa completar o último bloco
    private static final String ALGORITMO = "AES/GCM/NoPadding";

    // tamanho do IV em bytes: 12 (96 bits) é o valor recomendado para o GCM
    private static final int IV_BYTES = 12;

    // tamanho da tag de autenticação em bits: 128 é o máximo e o mais seguro
    private static final int TAG_BITS = 128;

    // a chave secreta; é a mesma para cifrar e para decifrar (por isso "simétrica")
    private final SecretKey chave;

    // gerador de aleatórios para o IV; SecureRandom, nunca Random comum
    private final SecureRandom aleatorio = new SecureRandom();

    public CriptografiaSimetrica() throws GeneralSecurityException {
        // pede ao Java um gerador de chaves AES
        KeyGenerator gerador = KeyGenerator.getInstance("AES");
        // define o tamanho da chave: 256 bits
        gerador.init(256);
        // gera uma chave aleatória; ela só existe na memória, enquanto a aplicação roda
        this.chave = gerador.generateKey();
    }

    /** Devolve Base64 de: IV (12 bytes) + texto cifrado + tag de autenticação. */
    public String cifrar(String texto) {
        try {
            // cria um array de 12 bytes para o IV
            byte[] iv = new byte[IV_BYTES];
            // preenche o IV com bytes aleatórios; um IV novo a cada cifragem
            // (repetir o IV com a mesma chave quebra a segurança do GCM)
            aleatorio.nextBytes(iv);
            // obtém o objeto que cifra, configurado para AES/GCM/NoPadding
            Cipher cifra = Cipher.getInstance(ALGORITMO);
            // prepara para CIFRAR, informando a chave e os parâmetros (tag e IV)
            cifra.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, iv));
            // cifra os bytes do texto; o resultado já vem com a tag de autenticação no final
            byte[] cifrado = cifra.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            // junta IV + texto cifrado em um só array; o IV vai na frente porque
            // quem decifra precisa dele (ele não é secreto)
            byte[] saida = ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array();
            // converte para Base64: bytes aleatórios não são texto e quebrariam o JSON
            return Base64.getEncoder().encodeToString(saida);
        } catch (GeneralSecurityException e) {
            // falha inesperada (algoritmo indisponível, por exemplo): é um erro da aplicação
            throw new IllegalStateException("Falha ao cifrar", e);
        }
    }

    public String decifrar(String base64) {
        try {
            // desfaz o Base64 e volta aos bytes: IV + texto cifrado + tag
            byte[] dados = Base64.getDecoder().decode(base64);
            // se não há nada além do IV, o texto cifrado não pode ser válido
            if (dados.length <= IV_BYTES) {
                throw new TextoCifradoInvalidoException();
            }
            // obtém o objeto que decifra, com o mesmo algoritmo usado para cifrar
            Cipher cifra = Cipher.getInstance(ALGORITMO);
            // prepara para DECIFRAR com a MESMA chave; o IV são os 12 primeiros bytes
            cifra.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, dados, 0, IV_BYTES));
            // decifra os bytes que vêm depois do IV; o GCM confere a tag aqui e
            // lança AEADBadTagException se o conteúdo foi alterado ou a chave é outra
            byte[] claro = cifra.doFinal(dados, IV_BYTES, dados.length - IV_BYTES);
            // converte os bytes de volta para texto
            return new String(claro, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // chave errada, texto adulterado ou Base64 inválido: não revelar o motivo
            // (IllegalArgumentException é o que o Base64 lança para entrada inválida)
            throw new TextoCifradoInvalidoException();
        }
    }
}
