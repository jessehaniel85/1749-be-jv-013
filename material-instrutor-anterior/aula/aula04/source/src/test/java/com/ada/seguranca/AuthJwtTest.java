package com.ada.seguranca;

import com.ada.seguranca.security.TokenService;
import com.ada.seguranca.usuario.Usuario;
import com.ada.seguranca.usuario.UsuarioRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do fluxo completo: cadastro, login, access token, refresh com rotação, logout e Flyway.
 * (MockMvc em vez de um servidor real, como nas outras aulas.)
 */
@SpringBootTest(properties = "app.jwt.secret=chave-de-teste-com-pelo-menos-32-caracteres!!")
@AutoConfigureMockMvc
class AuthJwtTest {

    private static final String SEGREDO = "chave-de-teste-com-pelo-menos-32-caracteres!!";
    private static final String SENHA = "Senha@12345";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired JdbcTemplate jdbc;

    // ------------------------------------------------------------------ apoio

    private ResultActions postJson(String url, String corpo) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String novoUsuario() {
        return "user" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void cadastrar(String username) throws Exception {
        postJson("/auth/register", "{\"username\":\"" + username + "\",\"password\":\"" + SENHA + "\"}")
                .andExpect(status().isCreated());
    }

    private JsonNode login(String username, String senha) throws Exception {
        String corpo = postJson("/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + senha + "\"}")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return postJson("/auth/refresh", "{\"refreshToken\":\"" + refreshToken + "\"}");
    }

    private ResultActions getComToken(String url, String token) throws Exception {
        return mvc.perform(get(url).header("Authorization", "Bearer " + token));
    }

    // ------------------------------------------------------------------ cadastro e validação

    @Test
    void cadastroValidoCriaUsuarioEGuardaApenasOHashDaSenha() throws Exception {
        String username = novoUsuario();
        cadastrar(username);

        Usuario u = usuarios.findByUsername(username).orElseThrow();
        assertNotEquals(SENHA, u.getSenhaHash());
        assertTrue(u.getSenhaHash().startsWith("$2a$"), "deveria ser um hash BCrypt");
        assertEquals("USER", u.getPapel());
    }

    @Test
    void cadastroDuplicadoDevolve409() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        postJson("/auth/register", "{\"username\":\"" + username.toUpperCase() + "\",\"password\":\"" + SENHA + "\"}")
                .andExpect(status().isConflict());   // "USER..." e "user..." são o mesmo usuário
    }

    @Test
    void cadastroInvalidoDevolve400ComOsCampos() throws Exception {
        postJson("/auth/register", "{\"username\":\"a b\",\"password\":\"123\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes").isArray())
                .andExpect(jsonPath("$.detalhes[?(@ =~ /username.*/)]").exists())
                .andExpect(jsonPath("$.detalhes[?(@ =~ /password.*/)]").exists());
    }

    @Test
    void senhaFracaSemNumeroDevolve400() throws Exception {
        postJson("/auth/register", "{\"username\":\"" + novoUsuario() + "\",\"password\":\"somenteletras\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastroNaoPermiteEscolherOPapel() throws Exception {
        // tentativa de "mass assignment": mandar um campo "papel" que não existe no DTO
        String username = novoUsuario();
        postJson("/auth/register",
                "{\"username\":\"" + username + "\",\"password\":\"" + SENHA + "\",\"papel\":\"ADMIN\"}")
                .andExpect(status().isCreated());
        assertEquals("USER", usuarios.findByUsername(username).orElseThrow().getPapel());
    }

    @Test
    void jsonMalformadoDevolve400() throws Exception {
        postJson("/auth/login", "{isto não é json").andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ login

    @Test
    void loginDevolveAccessERefreshToken() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        JsonNode tokens = login(username, SENHA);

        assertEquals("Bearer", tokens.get("tokenType").asText());
        assertEquals(900, tokens.get("expiresIn").asLong());   // 15 minutos
        assertFalse(tokens.get("accessToken").asText().isBlank());
        assertFalse(tokens.get("refreshToken").asText().isBlank());
        assertNotEquals(tokens.get("accessToken").asText(), tokens.get("refreshToken").asText());
    }

    @Test
    void senhaErradaEUsuarioInexistenteDevolvemExatamenteAMesmaResposta() throws Exception {
        String username = novoUsuario();
        cadastrar(username);

        String senhaErrada = postJson("/auth/login", "{\"username\":\"" + username + "\",\"password\":\"Errada@123\"}")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String inexistente = postJson("/auth/login", "{\"username\":\"nao-existe-xyz\",\"password\":\"Errada@123\"}")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // mesma mensagem (o timestamp é a única diferença possível): não revela quem existe
        assertEquals(json.readTree(senhaErrada).get("mensagem"), json.readTree(inexistente).get("mensagem"));
    }

    @Test
    void contaDesativadaNaoFazLoginMesmoComASenhaCerta() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        Usuario u = usuarios.findByUsername(username).orElseThrow();
        u.setAtivo(false);
        usuarios.save(u);

        postJson("/auth/login", "{\"username\":\"" + username + "\",\"password\":\"" + SENHA + "\"}")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSemCamposDevolve400() throws Exception {
        postJson("/auth/login", "{}").andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ access token

    @Test
    void rotaProtegidaSemTokenDevolve401() throws Exception {
        mvc.perform(get("/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
    }

    @Test
    void rotaPublicaNaoPedeToken() throws Exception {
        mvc.perform(get("/publico/ping")).andExpect(status().isOk());
    }

    @Test
    void accessTokenValidoAcessaMe() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String access = login(username, SENHA).get("accessToken").asText();

        getComToken("/me", access)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value(username))
                .andExpect(jsonPath("$.autoridades[0]").value("ROLE_USER"));
    }

    @Test
    void tokenAdulteradoDevolve401() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String access = login(username, SENHA).get("accessToken").asText();
        // troca o último caractere da assinatura
        char ultimo = access.charAt(access.length() - 1);
        String adulterado = access.substring(0, access.length() - 1) + (ultimo == 'A' ? 'B' : 'A');

        getComToken("/me", adulterado).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoDevolve401() throws Exception {
        String expirado = JWT.create()
                .withIssuer("ada-auth").withSubject("ana")
                .withClaim("role", "USER").withClaim("token_use", "access")
                .withIssuedAt(Instant.now().minusSeconds(3600))
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SEGREDO));

        getComToken("/me", expirado).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAssinadoComOutraChaveDevolve401() throws Exception {
        String falso = JWT.create()
                .withIssuer("ada-auth").withSubject("admin")
                .withClaim("role", "ADMIN").withClaim("token_use", "access")
                .withExpiresAt(Instant.now().plusSeconds(600))
                .sign(Algorithm.HMAC256("uma-outra-chave-qualquer-do-atacante-123"));

        getComToken("/admin/painel", falso).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSemAssinaturaAlgNoneDevolve401() throws Exception {
        String semAssinatura = JWT.create()
                .withIssuer("ada-auth").withSubject("admin")
                .withClaim("role", "ADMIN").withClaim("token_use", "access")
                .withExpiresAt(Instant.now().plusSeconds(600))
                .sign(Algorithm.none());

        getComToken("/admin/painel", semAssinatura).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenNaoServeComoAccessToken() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String refresh = login(username, SENHA).get("refreshToken").asText();

        getComToken("/me", refresh).andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenNaoServeComoRefreshToken() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String access = login(username, SENHA).get("accessToken").asText();

        refresh(access).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ papéis

    @Test
    void usuarioComumRecebe403NaRotaDeAdmin() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String access = login(username, SENHA).get("accessToken").asText();

        getComToken("/admin/painel", access).andExpect(status().isForbidden());
    }

    @Test
    void adminDoSeedAcessaARotaDeAdmin() throws Exception {
        String access = login("admin", "Admin@12345").get("accessToken").asText();

        getComToken("/admin/painel", access)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Bem-vindo ao painel, admin"));
        getComToken("/me", access).andExpect(jsonPath("$.autoridades[0]").value("ROLE_ADMIN"));
    }

    // ------------------------------------------------------------------ refresh

    @Test
    void refreshDevolveNovoParDeTokensEOAntigoMorre() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        JsonNode primeiro = login(username, SENHA);
        String refreshAntigo = primeiro.get("refreshToken").asText();

        String corpo = refresh(refreshAntigo).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode segundo = json.readTree(corpo);

        assertNotEquals(refreshAntigo, segundo.get("refreshToken").asText());
        // o novo access token funciona
        getComToken("/me", segundo.get("accessToken").asText()).andExpect(status().isOk());
        // o refresh token antigo foi revogado (rotação)
        refresh(refreshAntigo).andExpect(status().isUnauthorized());
    }

    @Test
    void reusoDeRefreshTokenRevogaTodosOsTokensDoUsuario() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String refreshA = login(username, SENHA).get("refreshToken").asText();

        // o dono (ou o atacante) usa o refresh A e recebe o B
        String refreshB = json.readTree(refresh(refreshA).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("refreshToken").asText();

        // o outro lado apresenta o A de novo: reuso detectado -> 401...
        refresh(refreshA).andExpect(status().isUnauthorized());
        // ...e o B, que era legítimo, também foi revogado (todos precisam logar de novo).
        // Isto só funciona por causa do noRollbackFor no AuthService.renovar.
        refresh(refreshB).andExpect(status().isUnauthorized());

        // depois de logar de novo, tudo volta ao normal
        String novo = login(username, SENHA).get("refreshToken").asText();
        refresh(novo).andExpect(status().isOk());
    }

    @Test
    void refreshAssinadoPeloServidorMasAusenteDoBancoEhRecusado() throws Exception {
        // JWT perfeito (chave certa, tipo certo, não expirou), mas o jti não está na tabela.
        String falso = JWT.create()
                .withIssuer("ada-auth").withSubject("ana")
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("token_use", "refresh")
                .withExpiresAt(Instant.now().plusSeconds(600))
                .sign(Algorithm.HMAC256(SEGREDO));

        refresh(falso).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshDeUsuarioDesativadoEhRecusado() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String refreshToken = login(username, SENHA).get("refreshToken").asText();

        Usuario u = usuarios.findByUsername(username).orElseThrow();
        u.setAtivo(false);
        usuarios.save(u);

        refresh(refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshComLixoDevolve401() throws Exception {
        refresh("isto.nao.eh-um-jwt").andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ logout

    @Test
    void logoutRevogaORefreshToken() throws Exception {
        String username = novoUsuario();
        cadastrar(username);
        String refreshToken = login(username, SENHA).get("refreshToken").asText();

        postJson("/auth/logout", "{\"refreshToken\":\"" + refreshToken + "\"}")
                .andExpect(status().isNoContent());

        refresh(refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutComTokenInvalidoNaoFalha() throws Exception {
        postJson("/auth/logout", "{\"refreshToken\":\"lixo\"}").andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------ Flyway e configuração

    @Test
    void flywayAplicouAsMigrationsComSucesso() {
        // só as migrations versionadas (o Flyway pode registrar também linhas internas)
        var versoes = jdbc.queryForList(
                "SELECT \"version\" FROM \"flyway_schema_history\" "
                        + "WHERE \"success\" = TRUE AND \"version\" IS NOT NULL ORDER BY \"installed_rank\"",
                String.class);
        assertEquals(java.util.List.of("1", "2", "100"), versoes);   // V1, V2 e V100 (seed)
    }

    @Test
    void chaveCurtaImpedeASubidaDaAplicacao() {
        var propsCurtas = new com.ada.seguranca.security.JwtProperties(
                "curta", "ada-auth", java.time.Duration.ofMinutes(15), java.time.Duration.ofDays(7));
        assertThrows(IllegalArgumentException.class, () -> new TokenService(propsCurtas));
    }
}
