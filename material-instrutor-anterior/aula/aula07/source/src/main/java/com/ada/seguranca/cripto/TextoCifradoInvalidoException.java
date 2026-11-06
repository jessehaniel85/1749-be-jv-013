package com.ada.seguranca.cripto;

public class TextoCifradoInvalidoException extends RuntimeException {

    public TextoCifradoInvalidoException() {
        super("Texto cifrado inválido");
    }
}
