package com.ada.sso.idp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * A segurança do PROVEDOR. Aqui sim existe login com usuário e senha e uma SESSÃO com cookie
 * (IDP_SESSAO): é essa sessão que faz o segundo acesso não pedir senha.
 */
@Configuration
@EnableWebSecurity
@Profile("idp")
public class IdpSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    NotificadorDeLogout notificador,
                                    IdpProperties props) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Livres: a tela de login, a chave pública, o endpoint que as APLICAÇÕES chamam
                // (ele se protege sozinho com o segredo), as telas de saída e o visual.
                .requestMatchers("/sso/login", "/sso/chave-publica", "/sso/token",
                        "/sso/sair", "/sso/saiu", "/estilo.css", "/favicon.ico", "/error").permitAll()
                // TODO o resto, inclusive /sso/autorizar, exige estar logado NO PROVEDOR.
                .anyRequest().authenticated())

            // CSRF fica LIGADO (o login e o logout são formulários com sessão). A única exceção
            // é /sso/token: não é chamado por navegador, e sim por servidores, que se autenticam
            // com o segredo (um token CSRF não faria sentido ali).
            .csrf(csrf -> csrf.ignoringRequestMatchers("/sso/token"))

            .formLogin(form -> form
                .loginPage("/sso/login")
                .loginProcessingUrl("/sso/login")
                .failureUrl("/sso/login?erro")
                // Sem defaultSuccessUrl: depois do login, o Spring Security volta para a URL que o
                // usuário queria (a /sso/autorizar guardada), e o fluxo de SSO continua sozinho.
                .permitAll())

            .logout(logout -> logout
                .logoutUrl("/sso/sair")                 // POST (com CSRF)
                .addLogoutHandler(notificador)          // avisa as aplicações (logout único)
                .logoutSuccessHandler((req, res, auth) -> {
                    String app = req.getParameter("app");
                    res.sendRedirect("/sso/saiu" + (props.aplicacao(app) != null ? "?app=" + app : ""));
                })
                .deleteCookies("IDP_SESSAO"));

        // Cabeçalhos de segurança padrão do Spring Security ficam ligados, inclusive
        // X-Frame-Options: a tela de login não pode ser embutida em outro site (clickjacking, aula 01).
        return http.build();
    }

    // BCrypt: o hash lento e com salt para as senhas (aula 02 e 04).
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
