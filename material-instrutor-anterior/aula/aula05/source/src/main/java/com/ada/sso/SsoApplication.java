package com.ada.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * UM projeto, TRÊS processos. O perfil do Spring decide qual papel esta execução assume:
 *
 *   --spring.profiles.active=idp          -> o provedor de identidade (porta 9000)
 *   --spring.profiles.active=notas        -> a aplicação Notas (porta 8081)
 *   --spring.profiles.active=biblioteca   -> a aplicação Biblioteca (porta 8082)
 *
 * Os pacotes mostram a separação: tudo em "idp" só existe no provedor; tudo em "cliente"
 * só existe nas aplicações. Elas NÃO compartilham código de segurança nem banco: só se
 * falam por HTTP, como sistemas independentes.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SsoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsoApplication.class, args);
    }
}
