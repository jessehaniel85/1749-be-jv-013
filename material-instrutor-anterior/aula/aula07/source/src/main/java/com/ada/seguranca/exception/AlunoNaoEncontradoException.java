package com.ada.seguranca.exception;

public class AlunoNaoEncontradoException extends RuntimeException {

    public AlunoNaoEncontradoException(Long id) {
        super("Aluno " + id + " não encontrado");
    }
}
