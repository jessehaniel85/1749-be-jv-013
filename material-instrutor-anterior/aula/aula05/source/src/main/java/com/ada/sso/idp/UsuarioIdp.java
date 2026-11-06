package com.ada.sso.idp;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Um usuário do provedor: as credenciais (login e hash da senha) E os atributos de identidade
 * que o provedor compartilha com as aplicações (nome, e-mail, papel).
 *
 * É aqui, e SÓ aqui, que existe senha. As aplicações nunca a veem.
 */
public class UsuarioIdp implements UserDetails {

    private final String username;
    private final String senhaHash;
    private final String nome;
    private final String email;
    private final String papel;

    public UsuarioIdp(String username, String senhaHash, String nome, String email, String papel) {
        this.username = username;
        this.senhaHash = senhaHash;
        this.nome = nome;
        this.email = email;
        this.papel = papel;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getPapel() {
        return papel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + papel));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
