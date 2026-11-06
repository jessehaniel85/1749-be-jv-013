package com.ada.seguranca.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * As configurações do bloco "app.jwt" do application.yml, em um objeto tipado.
 * O Spring converte "15m" e "7d" em Duration sozinho.
 *
 * @param secret     chave de assinatura (HS256). Vem da variável de ambiente JWT_SECRET.
 * @param issuer     quem emite o token (claim "iss"); o verificador exige o mesmo valor.
 * @param accessTtl  vida do access token.
 * @param refreshTtl vida do refresh token.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("ada-auth") String issuer,
        @DefaultValue("15m") Duration accessTtl,
        @DefaultValue("7d") Duration refreshTtl) {
}
