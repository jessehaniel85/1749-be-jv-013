package com.ada.seguranca.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Os endpoints de autenticação. Todos são LIVRES (veja a SecurityConfig): quem os chama ainda
 * não tem um token válido. O controller só traduz HTTP; as regras estão no AuthService.
 *
 * @Valid: antes de entrar no método, o Spring confere as anotações do record (@NotBlank, @Size...).
 * Se falhar, o método nem executa e a resposta é 400.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // POST /auth/register   {"username":"bia","password":"Senha@12345"}   -> 201
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registrar(@Valid @RequestBody RegisterRequest request) {
        service.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Usuário criado. Faça login em /auth/login."));
    }

    // POST /auth/login   {"username":"ana","password":"Senha@12345"}   -> 200 com o par de tokens
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    // POST /auth/refresh   {"refreshToken":"..."}   -> 200 com um NOVO par (o antigo morre)
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return service.renovar(request.refreshToken());
    }

    // POST /auth/logout   {"refreshToken":"..."}   -> 204 (sem corpo)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        service.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
