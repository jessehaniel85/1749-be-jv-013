package com.ada.seguranca.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * As regras de segurança da aplicação: quais rotas são livres, quais exigem login, quais exigem papel.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    TokenService tokenService,
                                    RestAuthenticationEntryPoint entryPoint,
                                    RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
            // CSRF explora cookies que o navegador envia sozinho (aula 01). Aqui o token vai em um
            // cabeçalho montado pelo código do cliente, então desligar é aceitável.
            // Se a API usasse sessão ou cookie para autenticar, NÃO desligue.
            .csrf(AbstractHttpConfigurer::disable)
            // STATELESS: o servidor não cria HttpSession. Cada requisição se prova pelo token.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Sem tela de login nem popup do HTTP Basic: a API só entende Bearer token.
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Rotas LIVRES: quem ainda não tem token precisa chegar nelas.
                .requestMatchers("/publico/**", "/error").permitAll()
                .requestMatchers("/auth/register", "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                // Só o papel ADMIN (a autoridade ROLE_ADMIN).
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // TODO o resto exige um access token válido. Esquecer uma rota aqui NÃO a deixa
                // aberta: o padrão é negar (princípio do menor privilégio, aula 01).
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint(entryPoint)        // 401 em JSON
                .accessDeniedHandler(accessDeniedHandler))   // 403 em JSON
            // Nosso filtro roda ANTES do filtro padrão de usuário/senha, para já encontrar o
            // usuário identificado quando as regras acima forem avaliadas.
            .addFilterBefore(new JwtAuthenticationFilter(tokenService),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt: hash lento de propósito e com salt embutido. Cada encode() da mesma senha gera um
    // texto diferente; por isso a conferência é feita com matches(), nunca com equals().
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // O AuthenticationManager monta sozinho um provedor que usa o UsuarioDetailsService (busca o
    // usuário) e o PasswordEncoder (confere a senha). É ele que o AuthService chama no login.
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
