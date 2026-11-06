package com.ada.seguranca.usuario;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * A "ponte" entre o seu banco e o Spring Security: dado um username, devolve o usuário com o
 * HASH da senha e os papéis. O Spring Security (DaoAuthenticationProvider) então compara a senha
 * recebida com esse hash usando o PasswordEncoder (BCrypt).
 *
 * Basta existir UM bean UserDetailsService: o Spring Security o encontra sozinho.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = repository.findByUsername(username)
                // O Spring Security converte esta exceção em "credenciais inválidas" (mesma
                // resposta de senha errada), para não revelar quais usuários existem.
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenhaHash())
                // roles("ADMIN") vira a autoridade "ROLE_ADMIN"
                .roles(usuario.getPapel())
                // VALIDAÇÃO DO USUÁRIO: conta desativada não entra, mesmo com a senha certa.
                // O Spring Security lança DisabledException, que cai no 401 genérico.
                .disabled(!usuario.isAtivo())
                .build();
    }
}
