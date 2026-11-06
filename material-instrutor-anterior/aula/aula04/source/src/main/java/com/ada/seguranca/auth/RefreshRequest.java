package com.ada.seguranca.auth;

import jakarta.validation.constraints.NotBlank;

/** Corpo de POST /auth/refresh e POST /auth/logout: o refresh token (o JWT inteiro). */
public record RefreshRequest(

        @NotBlank(message = "é obrigatório")
        String refreshToken) {
}
