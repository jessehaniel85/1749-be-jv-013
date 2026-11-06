package com.ada.sso.cliente;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;

/**
 * A segurança de cada APLICAÇÃO (Notas e Biblioteca). Repare no que NÃO existe aqui:
 * formulário de login, usuários, senhas, BCrypt. A aplicação delega a pergunta "quem é você?"
 * ao provedor (IniciaLoginSso) e só cuida da sua sessão local e das SUAS permissões.
 *
 * @EnableMethodSecurity liga o @PreAuthorize usado nos controllers (ex.: só PROFESSOR lança notas).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile({"notas", "biblioteca"})
public class ClienteSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, IniciaLoginSso iniciaLogin) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Livres: a volta do provedor, o aviso de logout (que se protege com a assinatura),
                // a página "sem permissão", o visual e o favicon. O favicon precisa estar livre: senão
                // o navegador o pede sozinho e o Spring o guardaria como "a página que o usuário queria".
                .requestMatchers("/sso/callback", "/sso/logout-backchannel", "/negado",
                        "/estilo.css", "/favicon.ico", "/error").permitAll()
                // TODO o resto exige sessão local.
                .anyRequest().authenticated())

            // Quem não tem sessão vai para o provedor (e não para uma tela de login própria).
            .exceptionHandling(e -> e
                .authenticationEntryPoint(iniciaLogin)
                // Logado, mas sem o papel exigido: página de "sem permissão" (autenticação OK, autorização negada).
                .accessDeniedHandler((req, res, ex) -> {
                    if (ex instanceof CsrfException) {
                        res.sendError(HttpServletResponse.SC_FORBIDDEN);   // formulário sem token CSRF: 403 puro
                    } else {
                        res.sendRedirect("/negado");
                    }
                }))

            // O aviso de logout vem de outro SERVIDOR, não de um navegador: não tem token CSRF.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/sso/logout-backchannel"))

            // "Sair deste sistema": encerra SÓ a sessão local (POST com CSRF). O provedor continua
            // com a sessão dele: voltar aqui loga de novo sozinho. É diferente do logout único.
            .logout(logout -> logout
                .logoutUrl("/sair")
                .logoutSuccessUrl("/")
                .deleteCookies("NOTAS_SESSAO", "BIBLIOTECA_SESSAO"));

        return http.build();
    }
}
