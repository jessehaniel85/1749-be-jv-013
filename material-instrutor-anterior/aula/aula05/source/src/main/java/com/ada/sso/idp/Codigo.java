package com.ada.sso.idp;

import java.time.Instant;

/** Um código de autorização emitido: de quem é, para qual aplicação e até quando vale. */
public record Codigo(String username, String nome, String email, String papel,
                     String appId, Instant expiraEm) {

    public boolean expirou() {
        return Instant.now().isAfter(expiraEm);
    }
}
