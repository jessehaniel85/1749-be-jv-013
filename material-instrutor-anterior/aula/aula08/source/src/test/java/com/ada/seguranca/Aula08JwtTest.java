package com.ada.seguranca;

import com.ada.seguranca.usuario.Usuario;
import com.ada.seguranca.usuario.UsuarioRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.jwt.secret=" + Aula08JwtTest.SEGREDO)
@AutoConfigureMockMvc
@ActiveProfiles("jwt")
class Aula08JwtTest {

    static final String SEGREDO = "chave-de-teste-com-mais-de-32-caracteres-0123456789";
    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private UsuarioRepository repository;

    private String novoUsername() {
        return "user-" + UUID.randomUUID();
    }

    private void cadastra(String username) throws Exception {
        mvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", SENHA))))
                .andExpect(status().isCreated());
    }

    private String autentica(String username) throws Exception {
        String corpo = mvc.perform(post("/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", SENHA))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("token").asText();
    }

    private String tokenValido() throws Exception {
        String username = novoUsername();
        cadastra(username);
        return autentica(username);
    }

    @Test
    void cadastroGravaASenhaComoHashBCrypt() throws Exception {
        String username = novoUsername();
        cadastra(username);

        Usuario usuario = repository.findByUsername(username).orElseThrow();
        assertThat(usuario.getPassword()).isNotEqualTo(SENHA).startsWith("$2");
    }

    @Test
    void usernameDuplicadoDevolve409() throws Exception {
        String username = novoUsername();
        cadastra(username);

        mvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", SENHA))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Usuário já existe"));
    }

    @Test
    void autenticacaoComCredenciaisCorretasDevolveUmJwtComTresPartes() throws Exception {
        String username = novoUsername();
        cadastra(username);

        assertThat(autentica(username).split("\\.")).hasSize(3);
    }

    @Test
    void senhaErradaEUsuarioInexistenteDevolvemO401ComAMesmaMensagem() throws Exception {
        String username = novoUsername();
        cadastra(username);

        mvc.perform(post("/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", "errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuário ou senha inválidos."));

        mvc.perform(post("/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "nao-existe", "password", SENHA))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuário ou senha inválidos."));
    }

    @Test
    void semTokenDevolve401() throws Exception {
        mvc.perform(get("/alunos/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void comTokenNoCabecalhoAuthorizationBearerDevolve200() throws Exception {
        mvc.perform(get("/alunos/1").header("Authorization", "Bearer " + tokenValido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Batman"));
    }

    @Test
    void comTokenNoCabecalhoAccessTokenDevolve200() throws Exception {
        mvc.perform(get("/alunos/1").header("access_token", tokenValido()))
                .andExpect(status().isOk());
    }

    @Test
    void tokenAdulteradoDevolve401() throws Exception {
        String token = tokenValido();
        int meio = token.lastIndexOf('.') + 10;   // um caractere no meio da assinatura
        char original = token.charAt(meio);
        char trocado = original == 'A' ? 'B' : 'A';
        String adulterado = token.substring(0, meio) + trocado + token.substring(meio + 1);

        mvc.perform(get("/alunos/1").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoDevolve401() throws Exception {
        String expirado = JWT.create()
                .withIssuer("api-seguranca")
                .withSubject("qualquer")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SEGREDO));

        mvc.perform(get("/alunos/1").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAssinadoComOutraChaveDevolve401() throws Exception {
        String falso = JWT.create()
                .withIssuer("api-seguranca")
                .withSubject("qualquer")
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.HMAC256("outra-chave-que-o-servidor-nao-conhece-000000"));

        mvc.perform(get("/alunos/1").header("Authorization", "Bearer " + falso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postComTokenFuncionaPoisOCsrfFoiDesabilitado() throws Exception {
        mvc.perform(post("/alunos").header("Authorization", "Bearer " + tokenValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Aquaman\",\"email\":\"aquaman@ada.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Aquaman"));
    }

    @Test
    void erroDeNegocioContinuaPadronizadoComToken() throws Exception {
        mvc.perform(get("/alunos/999").header("Authorization", "Bearer " + tokenValido()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Aluno 999 não encontrado"));
    }
}
