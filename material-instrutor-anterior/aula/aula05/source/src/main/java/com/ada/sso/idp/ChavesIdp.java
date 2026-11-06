package com.ada.sso.idp;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * O par de chaves RSA do provedor (aula 02):
 *
 *   PRIVADA - só o provedor tem. Assina as asserções.
 *   PÚBLICA - qualquer um pode ter. As aplicações a usam para CONFERIR a assinatura.
 *
 * Como só o provedor tem a privada, uma asserção com assinatura válida só pode ter vindo dele.
 * É a base da relação de confiança: a aplicação não precisa guardar nenhum segredo para validar.
 *
 * Gerado a cada subida (demonstração). Reiniciar o provedor troca a chave; as aplicações
 * percebem a assinatura inválida e buscam a chave nova (veja VerificadorDeAsercao).
 * Em produção a chave fica em um cofre/keystore e é rotacionada com calma.
 */
@Component
@Profile("idp")
public class ChavesIdp {

    private final RSAPublicKey publica;
    private final RSAPrivateKey privada;

    public ChavesIdp() {
        try {
            KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
            gerador.initialize(2048);
            KeyPair par = gerador.generateKeyPair();
            this.publica = (RSAPublicKey) par.getPublic();
            this.privada = (RSAPrivateKey) par.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA indisponível nesta JVM", e);
        }
    }

    public RSAPublicKey publica() {
        return publica;
    }

    public RSAPrivateKey privada() {
        return privada;
    }

    /** A chave pública em Base64 (formato X.509), pronta para publicar. */
    public String publicaEmBase64() {
        return Base64.getEncoder().encodeToString(publica.getEncoded());
    }
}
