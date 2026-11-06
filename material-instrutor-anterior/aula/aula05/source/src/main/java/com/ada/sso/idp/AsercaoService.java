package com.ada.sso.idp;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Cria os documentos assinados que o provedor entrega às aplicações.
 *
 * ASERÇÃO ("uso" = "login"): "eu, o provedor, autentiquei o usuário X para a aplicação Y".
 * É o equivalente ao que o SAML chama de asserção, mas em JWT (a aula 06 chama isto de ID Token).
 *
 * TOKEN DE LOGOUT ("uso" = "logout"): "o usuário X saiu; encerre as sessões dele".
 *
 * O claim "uso" evita confusão: uma asserção de login não vale como ordem de logout, e vice-versa.
 */
@Service
@Profile("idp")
public class AsercaoService {

    private static final long VALIDADE_ASERCAO_SEGUNDOS = 300;
    private static final long VALIDADE_LOGOUT_SEGUNDOS = 120;

    private final IdpProperties props;
    private final Algorithm algoritmo;

    public AsercaoService(IdpProperties props, ChavesIdp chaves) {
        this.props = props;
        // RSA256 = RSA com SHA-256. Assina com a chave PRIVADA (a API pede o par; a pública não assina).
        this.algoritmo = Algorithm.RSA256(chaves.publica(), chaves.privada());
    }

    public long validadeDaAsercaoEmSegundos() {
        return VALIDADE_ASERCAO_SEGUNDOS;
    }

    public String gerarAsercao(Codigo codigo) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(props.emissor())                     // iss: quem emitiu (o provedor)
                .withSubject(codigo.username())                  // sub: quem é o usuário
                .withAudience(codigo.appId())                    // aud: para QUAL aplicação (só ela aceita)
                .withJWTId(UUID.randomUUID().toString())         // jti: id único desta asserção
                .withClaim("uso", "login")
                .withClaim("nome", codigo.nome())                // atributos de identidade
                .withClaim("email", codigo.email())
                .withClaim("papel", codigo.papel())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plusSeconds(VALIDADE_ASERCAO_SEGUNDOS))
                .sign(algoritmo);
    }

    public String gerarTokenDeLogout(String username, String appId) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(props.emissor())
                .withSubject(username)
                .withAudience(appId)
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("uso", "logout")
                .withIssuedAt(agora)
                .withExpiresAt(agora.plusSeconds(VALIDADE_LOGOUT_SEGUNDOS))
                .sign(algoritmo);
    }
}
