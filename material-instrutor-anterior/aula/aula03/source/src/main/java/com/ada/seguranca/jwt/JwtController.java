package com.ada.seguranca.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
// Logger: escreve no console/arquivo de log DO SERVIDOR (o cliente não vê).
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
// @RequestHeader lê um cabeçalho HTTP da requisição e entrega no parâmetro do método.
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Autenticação baseada em TOKEN: o servidor não guarda nada sobre quem está logado.
 * Tudo o que ele precisa saber vem DENTRO do token, que o cliente envia a cada requisição.
 *
 * Os endpoints /gerar, /decodificar, /validar e /biblioteca/* existem só para a aula:
 * em um sistema real, só o login emite token e ninguém expõe "validar" para o mundo.
 *
 * ROTEIRO PARA IMPLEMENTAR COM A TURMA (ordem sugerida):
 *   1. /login         -> confere a senha e devolve um token                        (Bloco 2)
 *   2. /protegido     -> exige um token válido                                     (Bloco 2)
 *   3. /decodificar   -> mostra o que há DENTRO do token, sem chave                (Bloco 3)
 *   4. /validar       -> valida e explica o motivo (para estudar tentativas de forjar) (Bloco 4)
 *   5. /admin         -> separa AUTENTICAÇÃO (quem é?) de AUTORIZAÇÃO (pode?)      (Bloco 4)
 *   6. /gerar         -> emite token com o perfil que quisermos (só para a demo)   (Blocos 4 e 5)
 *   7. /biblioteca/*  -> o mesmo com java-jwt, para provar que JWT é um padrão     (Bloco 4)
 *
 * Diferença para a sessão (SessaoController): aqui NÃO existe HttpSession. Reinicie a
 * aplicação e um token ainda não vencido continua valendo, porque nada ficou na memória.
 */
// @RestController: os métodos devolvem dados (JSON), não páginas.
@RestController
// Todos os caminhos desta classe começam com /jwt.
@RequestMapping("/jwt")
public class JwtController {

    // Logger desta classe: no log, cada linha mostra de qual classe veio.
    private static final Logger log = LoggerFactory.getLogger(JwtController.class);

    // Validade do token do login: 300 s = 5 minutos. Token de vida curta limita o estrago de um roubo.
    private static final long VALIDADE_PADRAO_SEGUNDOS = 300;

    // ---- Os "formatos" dos corpos JSON que a API recebe. Cada record é um JSON esperado. ----

    // {"username":"ana","password":"senha123"}
    public record LoginRequest(String username, String password) {
    }

    // {"usuario":"ana","perfil":"ADMIN","expiraEmSegundos":20}
    // "Long" (com L maiúsculo) pode ser null: assim sabemos quando o campo não foi enviado.
    public record GerarRequest(String usuario, String perfil, Long expiraEmSegundos) {
    }

    // {"token":"eyJ..."}
    public record TokenRequest(String token) {
    }

    // As peças de que o controller precisa (injetadas pelo Spring):
    private final JwtManual manual;           // gera e valida à mão
    private final JwtBiblioteca biblioteca;   // gera e valida com java-jwt
    private final UsuariosDemo usuarios;      // o cadastro de usuários para o login

    public JwtController(JwtManual manual, JwtBiblioteca biblioteca, UsuariosDemo usuarios) {
        this.manual = manual;
        this.biblioteca = biblioteca;
        this.usuarios = usuarios;
    }

    // ------------------------------------------------------------ login

    // POST /jwt/login com {"username":"ana","password":"senha123"}
    // Resposta: {"token":"eyJ..."}. Compare com o /sessao/login, que devolvia só uma mensagem
    // e mandava um cookie: aqui o cliente recebe o PRÓPRIO token e é responsável por guardá-lo.
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        UsuariosDemo.Usuario usuario = usuarios.autenticar(request.username(), request.password())
                // a mesma resposta para usuário inexistente e senha errada
                // (se as mensagens fossem diferentes, um atacante descobriria quais usuários existem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));
        // Senha conferida: emite o token com o nome, o perfil e a validade (5 min).
        return Map.of("token", manual.gerar(usuario.username(), usuario.perfil(), VALIDADE_PADRAO_SEGUNDOS));
    }

    // ------------------------------------------- rotas protegidas por token

    // GET /jwt/protegido: qualquer usuário com token VÁLIDO entra.
    // O token pode vir de dois jeitos (os dois são aceitos):
    //   Authorization: Bearer <token>     (padrão da indústria)
    //   access_token: <token>             (formato pedido no projeto final)
    // required = false: se o cabeçalho faltar, o Spring entrega null em vez de dar erro 400;
    // quem decide que é um 401 somos nós, em exigirTokenValido.
    @GetMapping("/protegido")
    public Map<String, Object> protegido(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestHeader(name = "access_token", required = false) String accessToken) {
        // Se o token for inválido, este método lança 401 e as linhas abaixo nem executam.
        Map<String, Object> payload = exigirTokenValido(authorization, accessToken);
        // Daqui para baixo o token foi validado: podemos confiar no payload.
        // O servidor sabe quem é o usuário SEM ter guardado nada: está tudo no token.
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Olá, " + payload.get("sub") + "! Você está autenticado por token.");
        resposta.put("perfil", payload.get("role"));
        resposta.put("expiraEm", payload.get("exp"));
        return resposta;
    }

    // GET /jwt/admin: só quem tem o perfil ADMIN entra.
    @GetMapping("/admin")
    public Map<String, String> admin(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestHeader(name = "access_token", required = false) String accessToken) {
        // 1º: o token é válido? Se não, 401 (não sabemos quem é).
        Map<String, Object> payload = exigirTokenValido(authorization, accessToken);
        // autenticado (sabemos QUEM é), mas autorizado? (pode fazer ISTO?): 403, não 401
        // PERGUNTE: "qual a diferença entre 401 e 403?"
        //   401 = não sei quem você é (autenticação).  403 = sei quem é, mas não pode (autorização).
        // O perfil vem do payload, e confiamos nele porque a ASSINATURA provou que é nosso.
        if (!"ADMIN".equals(payload.get("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return Map.of("mensagem", "Área administrativa. Bem-vindo, " + payload.get("sub"));
    }

    // --------------------------------------------- ferramentas da demonstração
    // ATENÇÃO: estes endpoints são só para a aula. Em um sistema real, um "/gerar" aberto
    // permitiria a qualquer um criar um token ADMIN. Diga isso à turma antes de usá-los.

    /** SÓ PARA A DEMONSTRAÇÃO: emite um token com o usuário, o perfil e a validade escolhidos. */
    @PostMapping("/gerar")
    public Map<String, String> gerar(@RequestBody GerarRequest request) {
        return Map.of("token", manual.gerar(usuarioDe(request), perfilDe(request), validadeDe(request)));
    }

    /** O mesmo token, gerado com a biblioteca java-jwt (aula 08). */
    @PostMapping("/biblioteca/gerar")
    public Map<String, String> gerarComBiblioteca(@RequestBody GerarRequest request) {
        return Map.of("token", biblioteca.gerar(usuarioDe(request), perfilDe(request), validadeDe(request)));
    }

    /** Qualquer pessoa com o token lê o header e o payload: não é preciso chave nenhuma. */
    // Este endpoint faz na API o que o jwt.io faz na tela. Prova que o payload NÃO é secreto.
    @PostMapping("/decodificar")
    public Map<String, Map<String, Object>> decodificar(@RequestBody TokenRequest request) {
        try {
            return manual.decodificar(textoDe(request));
        } catch (IllegalArgumentException e) {
            // Token com formato errado (não tem 3 partes, Base64 ou JSON inválido): 400, não 500.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token malformado");
        }
    }

    /** Valida à mão e explica o motivo (o /protegido, de propósito, não explica). */
    // Serve para a turma ver POR QUE cada tentativa de forjar falha ("assinatura inválida",
    // "token expirado", "algoritmo não permitido"...).
    @PostMapping("/validar")
    public Map<String, Object> validar(@RequestBody TokenRequest request) {
        JwtManual.Resultado resultado = manual.validar(textoDe(request));
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("valido", resultado.valido());
        resposta.put("motivo", resultado.motivo());
        resposta.put("payload", resultado.payload());
        return resposta;
    }

    /** Valida com a biblioteca java-jwt. */
    @PostMapping("/biblioteca/validar")
    public Map<String, Object> validarComBiblioteca(@RequestBody TokenRequest request) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        try {
            // Se algo estiver errado, verify() lança uma exceção; se passar, devolve o token lido.
            DecodedJWT jwt = biblioteca.validar(textoDe(request));
            resposta.put("valido", true);
            resposta.put("usuario", jwt.getSubject());
            resposta.put("perfil", jwt.getClaim("role").asString());
        } catch (JWTVerificationException e) {
            resposta.put("valido", false);
            // O NOME da exceção diz o motivo: SignatureVerificationException, TokenExpiredException...
            resposta.put("motivo", e.getClass().getSimpleName());
        }
        return resposta;
    }

    // ------------------------------------------------------------- apoio

    /**
     * Extrai o token da requisição e o valida. Se for inválido, lança 401.
     * Se for válido, devolve o payload (as claims) para o endpoint usar.
     * É o "porteiro" reaproveitado por /protegido e /admin. Na aula 08 vira um filtro do
     * Spring Security que roda ANTES de qualquer endpoint.
     */
    private Map<String, Object> exigirTokenValido(String authorization, String accessToken) {
        String token = extrairToken(authorization, accessToken);
        // Sem token nenhum, validamos uma string vazia: cai em "formato inválido", sem NullPointerException.
        JwtManual.Resultado resultado = manual.validar(token == null ? "" : token);
        if (!resultado.valido()) {
            // o MOTIVO vai só para o log do servidor; quem chama recebe uma resposta genérica
            // PERGUNTE: "por que não dizer ao cliente 'assinatura inválida'?"
            //   Resposta: ajudaria um atacante a saber o que tentar em seguida. O detalhe é útil
            //   para NÓS (log), não para quem ataca.
            log.info("Token recusado: {}", resultado.motivo());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado.");
        }
        return resultado.payload();
    }

    /** "Authorization: Bearer <token>" (padrão) ou "access_token: <token>" (pedido no projeto). */
    // static e sem "private" para o DesafioController reutilizar.
    static String extrairToken(String authorization, String accessToken) {
        // "Bearer " (com espaço) tem 7 caracteres; substring(7) pega o que vem depois dele.
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        // Não veio Bearer: tenta o cabeçalho access_token (pode ser null).
        return accessToken;
    }

    // Garante que o corpo trouxe um token: sem isso, 400 (erro do cliente), não 500.
    private String textoDe(TokenRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o token");
        }
        // trim() tira espaços e quebras de linha que sobram ao copiar e colar o token.
        return request.token().trim();
    }

    // O nome do usuário é obrigatório para gerar um token.
    private String usuarioDe(GerarRequest request) {
        if (request == null || request.usuario() == null || request.usuario().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o usuário");
        }
        return request.usuario();
    }

    // Se o perfil não for informado, assume o menos privilegiado (USER).
    private String perfilDe(GerarRequest request) {
        return request.perfil() == null || request.perfil().isBlank() ? "USER" : request.perfil();
    }

    // validade em segundos; negativa gera um token que já nasce expirado (para a demonstração)
    // Útil no Bloco 5: com 20 segundos, a turma vê o token roubado parar de funcionar.
    private long validadeDe(GerarRequest request) {
        long segundos = request.expiraEmSegundos() == null ? VALIDADE_PADRAO_SEGUNDOS : request.expiraEmSegundos();
        // Limite de 24 horas para cima ou para baixo: ninguém gera um token "eterno" sem querer.
        if (segundos < -86_400 || segundos > 86_400) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validade fora do limite (24 horas)");
        }
        return segundos;
    }
}
