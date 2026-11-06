package com.ada.seguranca.auth;

import com.ada.seguranca.exception.TokenInvalidoException;
import com.ada.seguranca.exception.UsuarioJaExisteException;
import com.ada.seguranca.security.TokenService;
import com.ada.seguranca.token.RefreshToken;
import com.ada.seguranca.token.RefreshTokenRepository;
import com.ada.seguranca.usuario.Usuario;
import com.ada.seguranca.usuario.UsuarioRepository;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * As regras de negócio da autenticação: cadastrar, entrar, renovar e sair.
 *
 * O FLUXO COM DOIS TOKENS:
 *
 *   login    ->  { accessToken (15 min), refreshToken (7 dias) }
 *   ...usa o accessToken em cada requisição...
 *   accessToken expira (401)
 *   refresh  ->  envia o refreshToken  ->  recebe um NOVO par (e o refreshToken antigo morre)
 *   logout   ->  o refreshToken é revogado no banco
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarios;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarios,
                       RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       TokenService tokenService) {
        this.usuarios = usuarios;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    // ------------------------------------------------------------------ CADASTRO

    @Transactional
    public void registrar(RegisterRequest request) {
        // Normalizamos: "Ana" e "ana" são o mesmo usuário (evita contas parecidas para enganar).
        String username = normalizar(request.username());

        if (usuarios.existsByUsername(username)) {
            throw new UsuarioJaExisteException();
        }
        // Só o HASH vai para o banco. O papel é SEMPRE "USER" no cadastro público.
        usuarios.save(new Usuario(username, passwordEncoder.encode(request.password()), "USER"));
    }

    // ------------------------------------------------------------------ LOGIN

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String username = normalizar(request.username());

        // Aqui acontece a VALIDAÇÃO DO USUÁRIO. O AuthenticationManager:
        //   1. busca o usuário (UsuarioDetailsService)
        //   2. recusa se a conta está desativada
        //   3. compara a senha com o hash (BCrypt.matches)
        // Qualquer falha lança AuthenticationException -> 401 com mensagem genérica.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password()));

        Usuario usuario = usuarios.findByUsername(username).orElseThrow();
        return emitirPar(usuario);
    }

    // ------------------------------------------------------------------ RENOVAÇÃO (refresh)

    /**
     * Troca um refresh token válido por um NOVO par de tokens ("rotação"): o refresh token usado
     * é revogado e um novo é emitido. Assim cada refresh token serve UMA vez só.
     *
     * Por que isso importa: se um atacante roubar o refresh token e usá-lo, o dono legítimo, ao
     * tentar usar o dele, apresentará um token JÁ REVOGADO. Isso é um sinal de roubo, então
     * revogamos TODOS os refresh tokens do usuário e todos os dispositivos precisam logar de novo.
     *
     * noRollbackFor: lançamos TokenInvalidoException DEPOIS de revogar os tokens. Sem esta opção
     * o Spring desfaria a revogação (uma exceção em método @Transactional faz rollback) e o
     * ataque passaria sem consequência.
     */
    @Transactional(noRollbackFor = TokenInvalidoException.class)
    public TokenResponse renovar(String refreshJwt) {
        // 1) O JWT é autêntico? (assinatura, emissor, tipo "refresh", expiração)
        DecodedJWT jwt = verificarRefresh(refreshJwt);

        // 2) O servidor conhece este token? Um JWT bem assinado mas ausente do banco é recusado.
        RefreshToken registro = refreshTokens.findById(jwt.getId())
                .orElseThrow(() -> new TokenInvalidoException("Refresh token desconhecido"));

        // 3) REUSO: o token já foi usado ou revogado, mas alguém o apresentou de novo.
        if (registro.isRevogado()) {
            int revogados = refreshTokens.revogarTodosDoUsuario(registro.getUsuarioId());
            log.warn("Reuso de refresh token detectado (usuário id={}). {} token(s) revogado(s).",
                    registro.getUsuarioId(), revogados);
            throw new TokenInvalidoException("Refresh token já utilizado");
        }

        // 4) Expirou? (o JWT já foi conferido, mas o banco é a segunda barreira)
        if (registro.expirou()) {
            throw new TokenInvalidoException("Refresh token expirado");
        }

        // 5) A conta ainda existe e está ativa? O access token não consulta o banco, mas a
        //    renovação consulta: é aqui que um usuário desativado perde o acesso, no máximo
        //    15 minutos depois.
        Usuario usuario = usuarios.findById(registro.getUsuarioId())
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new TokenInvalidoException("Usuário inativo"));

        // 6) Rotação: aposenta o token usado e emite um par novo.
        registro.revogar();
        refreshTokens.save(registro);
        return emitirPar(usuario);
    }

    // ------------------------------------------------------------------ LOGOUT

    /**
     * Revoga o refresh token informado. Nunca falha: um token inválido já "não funciona", então o
     * resultado desejado (cliente deslogado) é o mesmo.
     *
     * LIMITE do JWT: o access token já emitido continua válido até expirar (até 15 min). O logout
     * impede apenas a RENOVAÇÃO. Para derrubar o access token na hora seria preciso uma lista de
     * tokens bloqueados no servidor, o que tira do JWT a vantagem de ser stateless.
     */
    @Transactional
    public void logout(String refreshJwt) {
        try {
            DecodedJWT jwt = tokenService.validarRefresh(refreshJwt);
            refreshTokens.findById(jwt.getId()).ifPresent(token -> {
                token.revogar();
                refreshTokens.save(token);
            });
        } catch (JWTVerificationException e) {
            // token inválido ou expirado: nada a revogar
        }
    }

    // ------------------------------------------------------------------ apoio

    private TokenResponse emitirPar(Usuario usuario) {
        String accessToken = tokenService.gerarAccessToken(usuario);
        TokenService.RefreshGerado refresh = tokenService.gerarRefreshToken(usuario);

        // Registra o refresh token no banco (só o jti), para poder revogá-lo depois.
        refreshTokens.save(new RefreshToken(refresh.jti(), usuario.getId(), refresh.expiraEm()));

        return new TokenResponse(accessToken, refresh.token(), "Bearer",
                tokenService.segundosDeVidaDoAccessToken());
    }

    private DecodedJWT verificarRefresh(String refreshJwt) {
        try {
            return tokenService.validarRefresh(refreshJwt);
        } catch (JWTVerificationException e) {
            throw new TokenInvalidoException("Refresh token inválido: " + e.getClass().getSimpleName());
        }
    }

    private String normalizar(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
