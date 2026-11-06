package com.ada.sso.cliente;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * PASSO 6 (parte da verificação): a aplicação CONFERE a comprovação do provedor antes de confiar.
 *
 * Confere, nesta ordem: a assinatura (RSA, com a chave PÚBLICA do provedor), o emissor ("iss" é o
 * provedor em que confiamos), o destinatário ("aud" é ESTA aplicação, e não outra), a validade
 * ("exp") e o tipo ("uso"). Qualquer falha lança exceção e o login não acontece.
 *
 * O "aud" importa: sem ele, uma asserção emitida para a Biblioteca poderia ser apresentada às
 * Notas. Cada aplicação só aceita o que foi emitido para ela.
 */
@Component
@Profile({"notas", "biblioteca"})
public class VerificadorDeAsercao {

    // Se o provedor trocou de chave (reiniciou), buscamos a nova. No máximo uma vez por intervalo:
    // sem esse limite, qualquer um mandando tokens falsos faria a aplicação martelar o provedor.
    private static final Duration INTERVALO_MINIMO_ENTRE_BUSCAS = Duration.ofSeconds(10);

    private final ClienteProperties props;
    private final IdpClient idp;

    private RSAPublicKey chave;
    private Instant ultimaBusca = Instant.MIN;

    public VerificadorDeAsercao(ClienteProperties props, IdpClient idp) {
        this.props = props;
        this.idp = idp;
    }

    /** Verifica a asserção de login e devolve quem é o usuário. */
    public UsuarioSso verificarAsercao(String asercao) {
        DecodedJWT jwt = verificar(asercao, "login");
        return new UsuarioSso(
                jwt.getSubject(),
                jwt.getClaim("nome").asString(),
                jwt.getClaim("email").asString(),
                jwt.getClaim("papel").asString());
    }

    /** Verifica o token de logout enviado pelo provedor e devolve de quem são as sessões a encerrar. */
    public String verificarLogout(String tokenDeLogout) {
        return verificar(tokenDeLogout, "logout").getSubject();
    }

    private DecodedJWT verificar(String jwt, String uso) {
        try {
            try {
                return montar(chaveAtual(false), uso).verify(jwt);
            } catch (SignatureVerificationException e) {
                // Pode ser um token falso OU o provedor ter trocado de chave. Buscamos a chave de
                // novo (com limite) e tentamos uma única vez mais.
                return montar(chaveAtual(true), uso).verify(jwt);
            }
        } catch (JWTVerificationException e) {
            throw new SsoException("Asserção inválida: " + e.getClass().getSimpleName(), e);
        }
    }

    private JWTVerifier montar(RSAPublicKey chavePublica, String uso) {
        // O algoritmo é FIXADO aqui (RS256). Um token que declare "alg: none" ou HS256 no header
        // é rejeitado (os ataques da aula 03).
        return JWT.require(Algorithm.RSA256(chavePublica, null))
                .withIssuer(props.idpUrlPublica())
                .withAudience(props.id())
                .withClaim("uso", uso)
                .build();
    }

    private synchronized RSAPublicKey chaveAtual(boolean forcarNovaBusca) {
        boolean podeBuscar = Duration.between(ultimaBusca, Instant.now()).compareTo(INTERVALO_MINIMO_ENTRE_BUSCAS) >= 0;
        if (chave == null || (forcarNovaBusca && podeBuscar)) {
            chave = converter(idp.buscarChavePublica());
            ultimaBusca = Instant.now();
        }
        return chave;
    }

    private RSAPublicKey converter(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new SsoException("A chave pública do provedor é inválida.", e);
        }
    }
}
