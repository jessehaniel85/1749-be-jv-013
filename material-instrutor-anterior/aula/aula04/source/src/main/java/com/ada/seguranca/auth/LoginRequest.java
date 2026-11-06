package com.ada.seguranca.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo de POST /auth/login. No login só exigimos que os campos existam (e um limite de tamanho
 * para não processar lixo gigante). As regras de FORÇA da senha valem só no cadastro: aqui a
 * senha certa é a que está no banco.
 */
public record LoginRequest(

        @NotBlank(message = "é obrigatório")
        @Size(max = 30, message = "no máximo 30 caracteres")
        String username,

        @NotBlank(message = "é obrigatória")
        @Size(max = 72, message = "no máximo 72 caracteres")
        String password) {
}
