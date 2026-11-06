package com.ada.seguranca.publico;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicoController {

    // Rota liberada na configuração da autenticação básica (perfil "basica").
    @GetMapping("/publico/ola")
    public String ola() {
        return "Olá! Esta rota é pública.";
    }
}
