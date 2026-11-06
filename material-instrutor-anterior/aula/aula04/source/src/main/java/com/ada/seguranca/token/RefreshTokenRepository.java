package com.ada.seguranca.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /**
     * Revoga TODOS os refresh tokens ativos de um usuário. Usado quando detectamos que um
     * refresh token já usado foi apresentado de novo (veja AuthService.renovar).
     * Devolve quantos tokens foram revogados.
     */
    @Modifying(clearAutomatically = true)
    @Query("update RefreshToken r set r.revogado = true where r.usuarioId = :usuarioId and r.revogado = false")
    int revogarTodosDoUsuario(@Param("usuarioId") Long usuarioId);
}
