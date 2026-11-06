package com.ada.seguranca.exception;

/** Refresh token inválido, expirado, revogado ou reutilizado. Vira 401 no GlobalExceptionHandler. */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensagem) {
        super(mensagem);
    }
}
