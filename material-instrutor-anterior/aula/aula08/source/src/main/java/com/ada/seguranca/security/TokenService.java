package com.ada.seguranca.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Profile("jwt")
public class TokenService {

    private static final String EMISSOR = "api-seguranca";

    private final Algorithm algorithm;

    // A chave vem de configuração externa (variável de ambiente JWT_SECRET), nunca do código.
    public TokenService(@Value("${app.jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String gerar(String username) {
        return JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(username)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                .sign(algorithm);
    }

    /**
     * Devolve o usuário (subject) se o token é válido. Confere assinatura, emissor e expiração;
     * lança JWTVerificationException se qualquer uma falhar. O algoritmo usado é o esperado
     * (HMAC256), e não o que o header do token declara.
     */
    public String validar(String token) {
        return JWT.require(algorithm)
                .withIssuer(EMISSOR)
                .build()
                .verify(token)
                .getSubject();
    }
}
