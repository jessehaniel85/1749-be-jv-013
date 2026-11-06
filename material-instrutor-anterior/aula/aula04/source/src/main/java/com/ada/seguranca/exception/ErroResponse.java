package com.ada.seguranca.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * O formato ÚNICO de erro da API. Nunca expõe stack trace nem detalhes internos (aula 07).
 *
 * @param detalhes problemas campo a campo na validação (400); vazio, e omitido do JSON, nos demais.
 */
public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        Instant timestamp,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<String> detalhes) {
}
