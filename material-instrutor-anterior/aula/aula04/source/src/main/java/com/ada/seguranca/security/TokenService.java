package com.ada.seguranca.security;

import com.ada.seguranca.usuario.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Cria e valida os DOIS tipos de token da aplicação. Os dois são JWT assinados com a mesma chave,
 * e o que os distingue é o claim "token_use":
 *
 *   ACCESS  ("access")  - vai em TODA requisição (Authorization: Bearer ...). Vida curta (15 min).
 *                         Carrega o papel do usuário, então a API não precisa consultar o banco.
 *   REFRESH ("refresh") - usado só em POST /auth/refresh para pedir um novo par de tokens.
 *                         Vida longa (7 dias), tem um "jti" registrado no banco para poder ser revogado.
 *
 * POR QUE "token_use"? Sem ele, um refresh token (7 dias) apresentado como access token seria aceito
 * em qualquer rota. Cada verificador abaixo EXIGE o valor certo desse claim.
 */
@Service
public class TokenService {

    static final String CLAIM_TIPO = "token_use";
    static final String CLAIM_PAPEL = "role";
    static final String ACCESS = "access";
    static final String REFRESH = "refresh";

    // O refresh token gerado + o que o banco precisa guardar sobre ele.
    public record RefreshGerado(String token, String jti, Instant expiraEm) {
    }

    private final JwtProperties props;
    private final Algorithm algoritmo;
    // O verificador é imutável e thread-safe: criamos uma vez e reaproveitamos.
    private final JWTVerifier verificadorAccess;
    private final JWTVerifier verificadorRefresh;

    public TokenService(JwtProperties props) {
        // HS256 exige chave de pelo menos 256 bits (32 bytes). Chave curta = token fácil de forjar
        // (lembra o desafio da chave "ada123" da aula 03). Melhor falhar na subida do que rodar inseguro.
        if (props.secret() == null
                || props.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "app.jwt.secret (variável JWT_SECRET) precisa ter pelo menos 32 caracteres.");
        }
        this.props = props;
        this.algoritmo = Algorithm.HMAC256(props.secret());
        this.verificadorAccess = verificador(ACCESS);
        this.verificadorRefresh = verificador(REFRESH);
    }

    private JWTVerifier verificador(String tipo) {
        // JWT.require(algoritmo): o algoritmo esperado é FIXADO aqui, no servidor. Um token que
        // declare "alg":"none" ou outro algoritmo no header é rejeitado (ataque da aula 03).
        return JWT.require(algoritmo)
                .withIssuer(props.issuer())          // confere o "iss"
                .withClaim(CLAIM_TIPO, tipo)         // confere se é access OU refresh, conforme o caso
                .build();                            // a expiração ("exp") é conferida automaticamente
    }

    // ------------------------------------------------------------------ ACCESS TOKEN

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(props.issuer())
                .withSubject(usuario.getUsername())              // sub: de quem é o token
                .withClaim(CLAIM_PAPEL, usuario.getPapel())      // o papel viaja no token
                .withClaim(CLAIM_TIPO, ACCESS)
                .withIssuedAt(agora)                             // iat
                .withExpiresAt(agora.plus(props.accessTtl()))    // exp: vida curta
                .sign(algoritmo);                                // assina com HMAC-SHA256
    }

    /** Valida um access token. Lança JWTVerificationException se for inválido, adulterado ou expirado. */
    public DecodedJWT validarAccess(String token) throws JWTVerificationException {
        return verificadorAccess.verify(token);
    }

    // ------------------------------------------------------------------ REFRESH TOKEN

    public RefreshGerado gerarRefreshToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(props.refreshTtl());
        // jti = "JWT ID": um identificador ÚNICO deste token. É a chave do registro no banco.
        String jti = UUID.randomUUID().toString();
        String token = JWT.create()
                .withIssuer(props.issuer())
                .withSubject(usuario.getUsername())
                .withJWTId(jti)
                .withClaim(CLAIM_TIPO, REFRESH)
                // sem o papel: o refresh token não serve para acessar nada, só para renovar.
                .withIssuedAt(agora)
                .withExpiresAt(expiraEm)
                .sign(algoritmo);
        return new RefreshGerado(token, jti, expiraEm);
    }

    /** Valida só a ASSINATURA, o emissor, o tipo e a expiração. Se foi revogado, quem decide é o banco. */
    public DecodedJWT validarRefresh(String token) throws JWTVerificationException {
        return verificadorRefresh.verify(token);
    }

    public long segundosDeVidaDoAccessToken() {
        return props.accessTtl().toSeconds();
    }
}
