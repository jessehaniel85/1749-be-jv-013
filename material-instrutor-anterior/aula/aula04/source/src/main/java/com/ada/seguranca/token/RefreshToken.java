package com.ada.seguranca.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Registro de um refresh token emitido (tabela "refresh_tokens", migration V2).
 *
 * O JWT do refresh token vai para o cliente; aqui no banco fica só o "jti" (id do token) e o
 * estado dele. É isto que permite REVOGAR um token que, sozinho, seria válido até expirar.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    // o jti: o claim "jti" (JWT ID) do refresh token
    @Id
    private String id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Instant expiraEm;

    // true = já foi usado (rotação) ou o usuário fez logout
    @Column(nullable = false)
    private boolean revogado;

    @Column(nullable = false)
    private Instant criadoEm = Instant.now();

    protected RefreshToken() {
        // exigido pelo JPA
    }

    public RefreshToken(String id, Long usuarioId, Instant expiraEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.expiraEm = expiraEm;
    }

    public String getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public boolean isRevogado() {
        return revogado;
    }

    public void revogar() {
        this.revogado = true;
    }

    public boolean expirou() {
        return Instant.now().isAfter(expiraEm);
    }
}
