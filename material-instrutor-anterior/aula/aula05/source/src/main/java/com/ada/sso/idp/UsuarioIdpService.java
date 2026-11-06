package com.ada.sso.idp;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * O "cadastro de pessoas da escola", dono único das credenciais.
 *
 * Para a demonstração os usuários ficam na memória, com o hash BCrypt calculado na subida.
 * Em um sistema real seria um banco (como na aula 04) ou o diretório da empresa (LDAP/AD).
 *
 * Senha de demonstração de todos: Escola@2026
 */
@Service
@Profile("idp")
public class UsuarioIdpService implements UserDetailsService {

    private static final String SENHA_DEMO = "Escola@2026";

    private final Map<String, UsuarioIdp> usuarios;

    public UsuarioIdpService(PasswordEncoder encoder) {
        String hash = encoder.encode(SENHA_DEMO);
        this.usuarios = List.of(
                new UsuarioIdp("ana", hash, "Ana Souza", "ana@escola.test", "ALUNO"),
                new UsuarioIdp("carlos", hash, "Carlos Lima", "carlos@escola.test", "PROFESSOR"),
                new UsuarioIdp("marta", hash, "Marta Reis", "marta@escola.test", "BIBLIOTECARIO"))
                .stream().collect(Collectors.toMap(UsuarioIdp::getUsername, Function.identity()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UsuarioIdp usuario = usuarios.get(username);
        if (usuario == null) {
            // o Spring Security devolve a mesma resposta de "senha errada": não revela quem existe
            throw new UsernameNotFoundException("Usuário não encontrado");
        }
        return usuario;
    }
}
