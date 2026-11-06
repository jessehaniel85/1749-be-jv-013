package com.ada.seguranca.cookie;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
// ResponseCookie monta o cookie que o SERVIDOR envia ao navegador (cabeçalho Set-Cookie).
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
// @CookieValue lê um cookie que o NAVEGADOR enviou de volta (cabeçalho Cookie).
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cookies da aula 02: ler um cookie (@CookieValue) e criar um com os atributos de segurança.
 *
 * Os três atributos de segurança e o ataque que cada um bloqueia:
 *
 *   Atributo   | O que faz                                                  | Liga com
 *   -----------+------------------------------------------------------------+----------------
 *   Secure     | O cookie só é enviado por HTTPS.                           | HTTPS (aula 02)
 *   HttpOnly   | O JavaScript da página NÃO consegue ler o cookie           | XSS (aula 01)
 *              | (document.cookie).                                         |
 *   SameSite   | O navegador só envia o cookie em requisições originadas    | CSRF (aula 01)
 *              | do mesmo site.                                             |
 *
 * ATENÇÃO: este controller mostra o MECANISMO do cookie. O "user-id=42" NÃO é uma sessão:
 * quem troca o 42 por 1 no DevTools vira outro usuário. A versão correta (cookie com um ID
 * aleatório + os dados no servidor) está nos métodos loginTestWithSessionID e
 * perfilTestWithSessionID, no fim desta classe. Com a HttpSession do Tomcat, ela aparece
 * pronta no SessaoController, no pacote jwt (aula 03).
 */
@RestController
public class CookieController {

    // A "memória" do servidor para o exemplo do session-id: ID da sessão -> usuário.
    // Em um sistema real isto seria a HttpSession do Tomcat (ou um Redis). Está só na memória:
    // reiniciar a aplicação apaga tudo, e os cookies que sobraram no navegador deixam de valer.
    // ConcurrentHashMap porque várias requisições acessam o mapa ao mesmo tempo.
    private final Map<String, String> sessoes = new ConcurrentHashMap<>();

    // Gerador de números aleatórios CRIPTOGRAFICAMENTE seguro (o java.util.Random é previsível).
    private final SecureRandom aleatorio = new SecureRandom();

    // ---------------------------------------------------------------- LER cookies

    // Lê o cookie "user-id"; se ele não vier na requisição, usa o valor padrão.
    // O navegador manda o cookie sozinho, no cabeçalho "Cookie: user-id=42".
    @GetMapping("/boas-vindas")
    public String boasVindas(
            @CookieValue(name = "user-id", defaultValue = "default-user-id") String userId) {
        return "Olá, usuário " + userId;
    }

    // Sem defaultValue: se o cookie não vier, o Spring rejeita a requisição (400).
    // PERGUNTE: "como este erro deveria ser tratado?" (a resposta está na aula 07)
    @GetMapping("/boas-vindas/estrito")
    public String boasVindasEstrito(@CookieValue(name = "user-id") String userId) {
        return "Olá, usuário " + userId;
    }

    // ---------------------------------------------------------------- CRIAR cookies

    // Cria um cookie com os atributos de segurança: o JavaScript da página não consegue lê-lo.
    // Resposta HTTP gerada:
    //   Set-Cookie: user-id=42; Max-Age=3600; Path=/; Secure; HttpOnly; SameSite=Strict
    @GetMapping("/login-teste")
    public ResponseEntity<String> loginTeste() {
        // from(nome, valor): o cookie "user-id" com o valor "42".
        ResponseCookie cookie = ResponseCookie.from("user-id", "42")
                // HttpOnly: o JavaScript da página NÃO lê o cookie (document.cookie não o mostra).
                // Bloqueia: o ROUBO do cookie por um XSS (aula 01). O cookie continua sendo
                // enviado ao servidor normalmente; só o script da página não o enxerga.
                .httpOnly(true)
                // Secure: o navegador só ENVIA o cookie em conexões HTTPS.
                // Bloqueia: o cookie vazar em texto puro por uma conexão HTTP (aula 02, seção HTTPS).
                // Em localhost (HTTP) o comportamento varia por navegador: teste antes da aula.
                .secure(true)
                // SameSite: o navegador só envia o cookie em requisições originadas do MESMO site.
                //   Strict = nunca em requisições vindas de outro site (nem em um clique de link)
                //   Lax    = também em navegação "de topo" por GET (clicar em um link), o padrão
                //            dos navegadores atuais; None = sempre (exige Secure)
                // Bloqueia: o CSRF (aula 01): o site do atacante dispara a requisição, mas o
                // navegador não anexa o cookie.
                .sameSite("Strict")
                // Path: em quais caminhos do site o cookie é enviado. "/" = em todos.
                .path("/")
                // Max-Age: vida do cookie. Sem isso ele seria um "cookie de sessão do navegador"
                // (some ao fechar o navegador). Com 1 hora ele é um cookie permanente.
                .maxAge(Duration.ofHours(1))
                .build();
        return ResponseEntity.ok()
                // O cookie vai no cabeçalho Set-Cookie; o navegador o guarda e o devolve depois.
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Cookie criado");
    }

    // Cookie SEM HttpOnly, só para comparar: document.cookie no console do navegador o mostra.
    // Faça a comparação ao vivo: chame /login-teste e /login-teste/legivel, abra o console (F12)
    // e rode document.cookie. Só "user-id-legivel" aparece. É isto que um XSS roubaria.
    @GetMapping("/login-teste/legivel")
    public ResponseEntity<String> loginTesteLegivel() {
        ResponseCookie cookie = ResponseCookie.from("user-id-legivel", "42")
                // false: o JavaScript da página consegue ler este cookie (vulnerável a XSS).
                .httpOnly(false)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Cookie legível criado");
    }

    // ------------------------------------------------- CRIAR e USAR um ID de sessão

    // O cookie CERTO para identificar um usuário: ele leva só um ID aleatório e sem significado.
    // Quem é o usuário fica GUARDADO NO SERVIDOR (o mapa "sessoes"). Compare com o /login-teste,
    // onde o próprio cookie dizia quem era o usuário ("user-id=42") e qualquer um podia editá-lo.
    //
    //   1. Login:        o servidor gera o ID e anota   { 9F3A... -> ana }
    //   2. Resposta:     Set-Cookie: session-id=9F3A...; HttpOnly; Secure; SameSite=Strict
    //   3. Requisições:  Cookie: session-id=9F3A...  (o servidor acha a ficha: "é a Ana")
    //
    // Sem autenticação, só para ver o MECANISMO: o parâmetro "usuario" não pede senha. O login
    // de verdade (usuário + senha) está no SessaoController, no pacote jwt.
    // O nome do cookie é "session-id" (e não "JSESSIONID") de propósito: o Tomcat usa o
    // JSESSIONID para a HttpSession real, e os dois não podem se misturar.
    // Teste: /login-teste/session-id?usuario=ana
    @GetMapping("/login-teste/session-id")
    public ResponseEntity<String> loginTestWithSessionID(
            @RequestParam(name = "usuario", defaultValue = "ana") String usuario) {
        // Aceita só letras (até 20): o nome volta na resposta e vai para o mapa, e entrada do
        // usuário nunca deve ser usada sem validar (aula 01). Fora disso: 400.
        if (!usuario.matches("[A-Za-z]{1,20}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário inválido.");
        }
        // 16 bytes aleatórios = 128 bits. Impossível de adivinhar por tentativa e erro.
        // PERGUNTE: "e se o ID fosse 1, 2, 3...?" (Qualquer um testaria o próximo número
        // e assumiria a sessão de outra pessoa.)
        byte[] bytes = new byte[16];
        aleatorio.nextBytes(bytes);
        // Em hexadecimal: 32 caracteres, por exemplo 9F3A6C0E... (maiúsculo, só para ler melhor).
        String sessionId = HexFormat.of().withUpperCase().formatHex(bytes);

        // A "ficha" fica no servidor: este ID pertence a este usuário.
        sessoes.put(sessionId, usuario);

        // O cookie leva APENAS o ID, nunca o nome do usuário. Mesmos atributos de segurança
        // do /login-teste (explicados lá): o XSS não lê, só vai por HTTPS e não vai em
        // requisições de outro site.
        ResponseCookie cookie = ResponseCookie.from("session-id", sessionId)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                // 30 minutos, como o tempo padrão de uma sessão no Tomcat.
                .maxAge(Duration.ofMinutes(30))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                // Repare: o corpo não revela o ID. Quem precisa dele é o navegador, pelo cookie.
                .body("Sessão criada para " + usuario);
    }

    // Quem é você? O servidor recebe só o ID (no cookie) e procura a ficha no mapa.
    // Pelo navegador (com o cookie guardado) ou pelo Postman/curl (mande o cabeçalho
    // "Cookie: session-id=<o ID que veio no Set-Cookie>").
    // required = false: sem o cookie, o Spring entregaria 400; queremos responder 401
    // (não autenticado), que é o status certo para "não sei quem é você".
    @GetMapping("/perfil-teste/session-id")
    public String perfilTestWithSessionID(
            @CookieValue(name = "session-id", required = false) String sessionId) {
        // Sem cookie, ou com um ID que o servidor não conhece (inventado, de uma sessão que
        // já foi esquecida, ou de antes de o servidor reiniciar): 401.
        // PERGUNTE: "e se eu trocar um caractere do ID no DevTools?" (Vira um ID que não
        // existe no mapa: 401. Dá para ser outro usuário só acertando um ID de 128 bits.)
        String usuario = sessionId == null ? null : sessoes.get(sessionId);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado.");
        }
        // A identidade vem do SERVIDOR (o mapa), não do cookie.
        return "Olá, " + usuario + "! O servidor te achou pelo ID da sessão.";
    }
}
