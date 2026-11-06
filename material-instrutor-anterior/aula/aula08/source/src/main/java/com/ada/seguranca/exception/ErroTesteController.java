package com.ada.seguranca.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Só para a demonstração da aula 07: força um erro inesperado para mostrar que a resposta
 * é genérica e que o detalhe aparece apenas no log. Remova antes de usar em um projeto real.
 */
@RestController
public class ErroTesteController {

    @GetMapping("/erro-teste")
    public String erroTeste() {
        throw new IllegalStateException("detalhe interno: falha ao conectar em jdbc:h2:mem:testdb");
    }
}
