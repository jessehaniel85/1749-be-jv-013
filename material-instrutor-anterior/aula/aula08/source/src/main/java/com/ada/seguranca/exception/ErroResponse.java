package com.ada.seguranca.exception;

import java.time.Instant;

public record ErroResponse(int status, String erro, String mensagem, Instant timestamp) {
}
