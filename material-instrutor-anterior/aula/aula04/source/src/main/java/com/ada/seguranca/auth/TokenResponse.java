package com.ada.seguranca.auth;

/**
 * O que o cliente recebe no login e na renovação.
 *
 * @param accessToken  vai em TODA requisição: Authorization: Bearer {accessToken}
 * @param refreshToken guardado com cuidado; usado só em POST /auth/refresh
 * @param tokenType    sempre "Bearer" (o esquema do cabeçalho Authorization)
 * @param expiresIn    segundos de vida do access token (900 = 15 min)
 */
public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}
