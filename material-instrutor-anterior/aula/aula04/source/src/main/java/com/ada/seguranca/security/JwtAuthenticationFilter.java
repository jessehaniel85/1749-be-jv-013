package com.ada.seguranca.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * O "porteiro" de cada requisição. Roda ANTES do controller:
 *
 *   1. lê o cabeçalho  Authorization: Bearer <access token>
 *   2. valida o token (assinatura, emissor, tipo "access", expiração)
 *   3. se válido, avisa o Spring Security QUEM é o usuário e QUAL o papel dele
 *   4. se ausente ou inválido, NÃO faz nada: a requisição segue "anônima" e, numa rota
 *      protegida, o Spring Security responde 401 (veja RestAuthenticationEntryPoint).
 *
 * Repare que NÃO consultamos o banco: o usuário e o papel vêm de dentro do token. Esta é a
 * vantagem do JWT (a API fica "stateless"). O preço: se o usuário for desativado ou mudar de
 * papel, o access token já emitido continua valendo até expirar (por isso a vida curta de 15 min).
 *
 * Esta classe NÃO é um @Component de propósito: ela é criada na SecurityConfig e entra só na
 * cadeia do Spring Security (um @Component seria registrado também como filtro do servlet).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            String token = cabecalho.substring(PREFIXO.length());
            try {
                DecodedJWT jwt = tokenService.validarAccess(token);
                String papel = jwt.getClaim(TokenService.CLAIM_PAPEL).asString();

                // Authentication = "este é o usuário desta requisição". O Spring Security guarda
                // na thread da requisição e consulta nas regras (authenticated(), hasRole("ADMIN")).
                // O papel precisa do prefixo ROLE_ para o hasRole("ADMIN") funcionar.
                var autenticacao = new UsernamePasswordAuthenticationToken(
                        jwt.getSubject(),                                   // quem é
                        null,                                               // sem credencial: já foi validada
                        List.of(new SimpleGrantedAuthority("ROLE_" + papel))); // o que pode
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            } catch (JWTVerificationException e) {
                // Assinatura errada, token adulterado, expirado, refresh token usado no lugar do
                // access, alg=none... NÃO dizemos o motivo ao cliente (não ajuda o atacante).
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
