package com.ada.seguranca.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Um usuário do sistema. Mapeia a tabela "usuarios" criada pela migration V1.
 *
 * O Hibernate NÃO cria esta tabela (ddl-auto = validate): ele só confere, na subida, se as
 * colunas abaixo existem. Os nomes em camelCase viram snake_case sozinhos (senhaHash -> senha_hash).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // O HASH BCrypt, nunca a senha. Por isso não existe getSenha() em lugar nenhum.
    @Column(nullable = false)
    private String senhaHash;

    // "USER" ou "ADMIN" (sem o prefixo ROLE_; ele é acrescentado onde o Spring Security pede).
    @Column(nullable = false)
    private String papel;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private Instant criadoEm = Instant.now();

    protected Usuario() {
        // exigido pelo JPA
    }

    public Usuario(String username, String senhaHash, String papel) {
        this.username = username;
        this.senhaHash = senhaHash;
        this.papel = papel;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
