package com.ada.seguranca.jwt;

// JWT = ponto de entrada da biblioteca java-jwt (da empresa Auth0).
import com.auth0.jwt.JWT;
// Algorithm escolhe COMO assinar/verificar (HMAC256, RSA256...).
import com.auth0.jwt.algorithms.Algorithm;
// Exceção-mãe de tudo o que pode dar errado ao verificar (assinatura, expiração, emissor...).
import com.auth0.jwt.exceptions.JWTVerificationException;
// DecodedJWT = o token já verificado, com getters para as claims.
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * O mesmo JWT, agora com a biblioteca java-jwt (a que será usada na aula 08).
 * Como o JWT é um PADRÃO, um token gerado aqui é aceito por {@link JwtManual} e vice-versa.
 *
 * ROTEIRO PARA IMPLEMENTAR COM A TURMA:
 *   1. Compare com o JwtManual: o que eram ~25 linhas (header, payload, HMAC, Base64) virou uma
 *      cadeia de métodos. A biblioteca faz o mesmo trabalho por baixo.
 *   2. Prove que é o mesmo padrão: gere em /jwt/biblioteca/gerar e valide em /jwt/validar
 *      (à mão), e o contrário. Os dois lados se entendem.
 *   3. Discuta: por que usar a biblioteca em produção? (revisada por muita gente, trata
 *      casos de borda, recebe correções de segurança). Escrever criptografia à mão é para aprender.
 */
// @Service: o Spring cria uma instância e a entrega ao JwtController.
@Service
public class JwtBiblioteca {

    // Reaproveitamos o JwtManual só para pegar a MESMA chave e a mesma constante EMISSOR.
    // É isso que permite um token de uma classe ser validado pela outra.
    private final JwtManual manual;

    // O Spring injeta o JwtManual aqui (injeção por construtor).
    public JwtBiblioteca(JwtManual manual) {
        this.manual = manual;
    }

    public String gerar(String usuario, String perfil, long segundosDeValidade) {
        // Um único "agora" para iat e exp ficarem coerentes entre si.
        Instant agora = Instant.now();
        // Cada linha abaixo corresponde a uma linha do payload que montamos à mão:
        return JWT.create()
                .withIssuer(JwtManual.EMISSOR)            // "iss"
                .withSubject(usuario)                     // "sub"
                .withClaim("role", perfil)                // claim customizada "role"
                .withIssuedAt(agora)                      // "iat"
                .withExpiresAt(agora.plusSeconds(segundosDeValidade))   // "exp"
                // sign() monta o header (alg HS256, typ JWT), calcula o HMAC-SHA256 com a chave
                // e junta as 3 partes: tudo o que o JwtManual.gerar fazia manualmente.
                .sign(Algorithm.HMAC256(manual.chavePadrao()));
    }

    /**
     * Valida assinatura, emissor e expiração. Lança JWTVerificationException se algo falhar.
     * Compare com JwtManual.validar: lá devolvemos um Resultado com o motivo; aqui a biblioteca
     * usa EXCEÇÕES (SignatureVerificationException, TokenExpiredException, ...).
     */
    public DecodedJWT validar(String token) throws JWTVerificationException {
        // require() descreve o que ESPERAMOS: o algoritmo (e a chave) e o emissor.
        // Como o algoritmo é definido por nós, um token "alg: none" é recusado, como no manual.
        return JWT.require(Algorithm.HMAC256(manual.chavePadrao()))
                .withIssuer(JwtManual.EMISSOR)
                // build() fecha a configuração; verify() confere formato, assinatura, emissor e
                // expiração (o "exp" é checado automaticamente) e devolve o token decodificado.
                .build()
                .verify(token);
    }
}
