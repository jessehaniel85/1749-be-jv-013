package com.ada.seguranca.exception;

/** O username escolhido já está em uso. Vira 409 (Conflict) no GlobalExceptionHandler. */
public class UsuarioJaExisteException extends RuntimeException {

    public UsuarioJaExisteException() {
        super("Este nome de usuário já está em uso.");
    }
}
