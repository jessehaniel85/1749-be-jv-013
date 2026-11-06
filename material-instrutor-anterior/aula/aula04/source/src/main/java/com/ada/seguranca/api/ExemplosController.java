package com.ada.seguranca.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Três rotas só para PROVAR as regras da SecurityConfig, uma de cada nível de proteção:
 *
 *   /publico/ping    -> livre (qualquer um)
 *   /me              -> exige um access token válido (qualquer papel)
 *   /admin/painel    -> exige o papel ADMIN
 *
 * Em um sistema real, aqui estariam os seus recursos (alunos, chamados...).
 */
@RestController
public class ExemplosController {

    @GetMapping("/publico/ping")
    public Map<String, String> ping() {
        return Map.of("mensagem", "pong (rota pública)");
    }

    // O Spring entrega o Authentication montado pelo JwtAuthenticationFilter a partir do token.
    @GetMapping("/me")
    public Map<String, Object> me(Authentication autenticacao) {
        return Map.of(
                "usuario", autenticacao.getName(),
                "autoridades", autenticacao.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList());
    }

    @GetMapping("/admin/painel")
    public Map<String, String> painel(Authentication autenticacao) {
        return Map.of("mensagem", "Bem-vindo ao painel, " + autenticacao.getName());
    }
}
