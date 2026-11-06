package com.ada.seguranca.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpo de POST /auth/register. As anotações são a VALIDAÇÃO DE ENTRADA: nada que não cumpra
 * estas regras chega ao serviço nem ao banco (resposta 400, tratada no GlobalExceptionHandler).
 *
 * Repare que NÃO existe um campo "papel": o cliente não escolhe ser ADMIN. Todo cadastro público
 * vira USER. (Se o record aceitasse o papel, qualquer um se cadastraria como administrador:
 * é o ataque de "mass assignment".)
 */
public record RegisterRequest(

        @NotBlank(message = "é obrigatório")
        @Size(min = 3, max = 30, message = "deve ter entre 3 e 30 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "aceita apenas letras, números, ponto, hífen e underline")
        String username,

        @NotBlank(message = "é obrigatória")
        // máx. 72: o BCrypt só considera os 72 primeiros bytes da senha
        @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "deve ter ao menos uma letra e um número")
        String password) {
}
