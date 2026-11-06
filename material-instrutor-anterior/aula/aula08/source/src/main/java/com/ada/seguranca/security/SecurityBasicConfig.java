package com.ada.seguranca.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Passos 4 e 5 da aula: autenticação básica (HTTP Basic) com rotas livres e protegidas.
 * Ativa com o perfil "basica". O CSRF fica LIGADO (padrão do Spring Security): um POST
 * com as credenciais corretas ainda assim devolve 403.
 */
@Configuration
@EnableWebSecurity
@Profile("basica")
public class SecurityBasicConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/publico/**").permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
