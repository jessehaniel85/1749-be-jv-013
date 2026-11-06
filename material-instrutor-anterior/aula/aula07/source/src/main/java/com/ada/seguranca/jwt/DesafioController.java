package com.ada.seguranca.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * DESAFIO da aula 03: um token assinado com uma chave FRACA.
 *
 * A turma recebe um token de usuário comum (USER), descobre a chave no jwt.io tentando palavras
 * fáceis e forja um token ADMIN. Prova de que a segurança do JWT é a segurança da chave.
 *
 * (Não abra este arquivo antes do desafio: a chave está aqui de propósito.)
 *
 * ROTEIRO PARA IMPLEMENTAR COM A TURMA (Bloco 6):
 *   1. GET /jwt/desafio/token  -> a turma recebe um token USER assinado com a chave fraca
 *   2. A turma cola o token no jwt.io e tenta chaves comuns no campo "your-256-bit-secret"
 *      até a tela mostrar "Signature Verified"
 *   3. Com a chave descoberta, muda role para ADMIN no jwt.io (que re-assina sozinho)
 *   4. GET /jwt/desafio/admin com o token forjado -> 200
 *   5. Conclusão: o algoritmo é seguro; a chave fraca não é. Uma chave boa tem 32+ caracteres
 *      aleatórios e fica fora do código (variável de ambiente ou cofre de segredos).
 *
 * Por que isso funciona (e o que faz parte do ataque): o atacante tem o token, então pode testar
 * milhões de chaves OFFLINE, no próprio computador, sem que o servidor perceba nenhuma tentativa.
 */
@RestController
// Os dois endpoints do desafio ficam sob /jwt/desafio.
@RequestMapping("/jwt/desafio")
public class DesafioController {

    // Uma chave fraca, de propósito. Uma chave de verdade é longa e aleatória.
    // É diferente da chave "de verdade" da aplicação (app.demo.jwt-secret): esta só vale AQUI,
    // por isso descobrir esta chave não permite forjar tokens do /jwt/protegido e /jwt/admin.
    static final String SEGREDO_FRACO = "ada123";

    // Reaproveitamos o JwtManual: ele aceita a chave como parâmetro, então serve aos dois casos.
    private final JwtManual manual;

    public DesafioController(JwtManual manual) {
        this.manual = manual;
    }

    // GET /jwt/desafio/token: entrega o token da turma e a missão.
    @GetMapping("/token")
    public Map<String, String> token() {
        // A chave fraca, em bytes, para o HMAC.
        byte[] chave = SEGREDO_FRACO.getBytes(StandardCharsets.UTF_8);
        return Map.of(
                // Usuário "aluno", perfil USER, validade de 1 hora (3600 s): tempo de sobra.
                "token", manual.gerar(chave, "aluno", "USER", 3600),
                "missao", "Você é USER. Torne-se ADMIN e acesse /jwt/desafio/admin. Dica: a chave é uma palavra fácil.");
    }

    // GET /jwt/desafio/admin: só aceita token ADMIN assinado com a chave fraca.
    @GetMapping("/admin")
    public Map<String, String> admin(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestHeader(name = "access_token", required = false) String accessToken) {
        // Mesma extração do JwtController (Bearer ou access_token).
        String token = JwtController.extrairToken(authorization, accessToken);
        // Valida com a chave FRACA (não com a chave padrão da aplicação).
        JwtManual.Resultado resultado = manual.validar(SEGREDO_FRACO.getBytes(StandardCharsets.UTF_8), token == null ? "" : token);
        // Token inválido (assinatura errada, expirado, malformado...): 401.
        if (!resultado.valido()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado.");
        }
        // Token autêntico, mas o perfil ainda é USER: 403 (a pista de que falta só trocar o perfil).
        if (!"ADMIN".equals(resultado.payload().get("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você ainda é USER. Forje um token ADMIN.");
        }
        // Chegou aqui: a assinatura confere E o perfil é ADMIN, ou seja, a turma conseguiu forjar
        // o token. A validação do servidor está correta; quem falhou foi a escolha da chave.
        return Map.of("mensagem",
                "Parabéns, " + resultado.payload().get("sub") + "! Você forjou um token ADMIN: a chave era fraca.");
    }
}
