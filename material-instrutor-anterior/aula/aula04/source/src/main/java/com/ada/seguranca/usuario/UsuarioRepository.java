package com.ada.seguranca.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // O Spring Data gera a consulta a partir do nome do método:
    //   SELECT ... FROM usuarios WHERE username = ?   (com parâmetro: não há SQL Injection, aula 07)
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
