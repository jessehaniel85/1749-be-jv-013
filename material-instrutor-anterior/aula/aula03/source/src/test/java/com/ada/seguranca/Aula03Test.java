package com.ada.seguranca;

import com.ada.seguranca.jwt.JwtBiblioteca;
import com.ada.seguranca.jwt.JwtManual;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Aula03Test {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private JwtManual manual;
    @Autowired
    private JwtBiblioteca biblioteca;

    // ------------------------------------------------------------ apoio

    private ResultActions postJson(String caminho, Object corpo) throws Exception {
        return mvc.perform(post(caminho).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(corpo)));
    }

    private JsonNode corpoDe(ResultActions acao) throws Exception {
        return json.readTree(acao.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private String login(String usuario, String senha) throws Exception {
        return corpoDe(postJson("/jwt/login", Map.of("username", usuario, "password", senha))
                .andExpect(status().isOk())).get("token").asText();
    }

    private JsonNode validar(String token) throws Exception {
        return corpoDe(postJson("/jwt/validar", Map.of("token", token)).andExpect(status().isOk()));
    }

    private String b64(String texto) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    // --------------------------------------------------- sessão x token

    @Test
    void sessaoGuardaOUsuarioNoServidorEPedeOCookieDeSessao() throws Exception {
        MockHttpSession sessao = (MockHttpSession) postJson("/sessao/login", Map.of("username", "ana", "password", "senha123"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);

        mvc.perform(get("/sessao/perfil").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("ana"));
    }

    @Test
    void semASessaoOServidorNaoSabeQuemEVoce() throws Exception {
        mvc.perform(get("/sessao/perfil")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginComSenhaErradaDevolve401NosDoisModelos() throws Exception {
        postJson("/sessao/login", Map.of("username", "ana", "password", "errada")).andExpect(status().isUnauthorized());
        postJson("/jwt/login", Map.of("username", "ana", "password", "errada")).andExpect(status().isUnauthorized());
        postJson("/jwt/login", Map.of("username", "ninguem", "password", "senha123")).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------- o token

    @Test
    void loginDevolveUmJwtComTresPartes() throws Exception {
        assertThat(login("ana", "senha123").split("\\.")).hasSize(3);
    }

    @Test
    void rotaProtegidaExigeToken() throws Exception {
        mvc.perform(get("/jwt/protegido")).andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaAceitaOTokenNosDoisCabecalhos() throws Exception {
        String token = login("ana", "senha123");

        mvc.perform(get("/jwt/protegido").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("USER"));
        mvc.perform(get("/jwt/protegido").header("access_token", token))
                .andExpect(status().isOk());
    }

    @Test
    void qualquerPessoaComOTokenLeOPayloadSemChave() throws Exception {
        String token = login("ana", "senha123");

        JsonNode corpo = corpoDe(postJson("/jwt/decodificar", Map.of("token", token)).andExpect(status().isOk()));

        assertThat(corpo.get("header").get("alg").asText()).isEqualTo("HS256");
        assertThat(corpo.get("payload").get("sub").asText()).isEqualTo("ana");
        assertThat(corpo.get("payload").get("role").asText()).isEqualTo("USER");
    }

    @Test
    void perfilDeUsuarioComumRecebe403NaAreaAdmin() throws Exception {
        mvc.perform(get("/jwt/admin").header("Authorization", "Bearer " + login("ana", "senha123")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/jwt/admin").header("Authorization", "Bearer " + login("admin", "admin789")))
                .andExpect(status().isOk());
    }

    // --------------------------------------------- tentativas de fraude

    @Test
    void mudarOPayloadSemReassinarInvalidaAAssinatura() throws Exception {
        String[] partes = login("ana", "senha123").split("\\.");
        String payloadAdmin = b64("{\"iss\":\"ada-seguranca\",\"sub\":\"ana\",\"role\":\"ADMIN\",\"iat\":1,\"exp\":4102444800}");
        String adulterado = partes[0] + "." + payloadAdmin + "." + partes[2];

        JsonNode resultado = validar(adulterado);

        assertThat(resultado.get("valido").asBoolean()).isFalse();
        assertThat(resultado.get("motivo").asText()).startsWith("assinatura inválida");
        mvc.perform(get("/jwt/admin").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSemAssinaturaComAlgNoneEhRecusado() throws Exception {
        String semAssinatura = b64("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "."
                + b64("{\"iss\":\"ada-seguranca\",\"sub\":\"ana\",\"role\":\"ADMIN\",\"exp\":4102444800}") + ".";

        JsonNode resultado = validar(semAssinatura);

        assertThat(resultado.get("valido").asBoolean()).isFalse();
        assertThat(resultado.get("motivo").asText()).startsWith("algoritmo não permitido");
    }

    @Test
    void tokenAssinadoComOutraChaveEhRecusado() throws Exception {
        String falso = manual.gerar("outra-chave-que-o-servidor-nao-conhece".getBytes(StandardCharsets.UTF_8),
                "ana", "ADMIN", 300);

        assertThat(validar(falso).get("motivo").asText()).startsWith("assinatura inválida");
    }

    @Test
    void tokenExpiradoEhRecusado() throws Exception {
        String expirado = corpoDe(postJson("/jwt/gerar", Map.of("usuario", "ana", "expiraEmSegundos", -10))
                .andExpect(status().isOk())).get("token").asText();

        JsonNode resultado = validar(expirado);

        assertThat(resultado.get("valido").asBoolean()).isFalse();
        assertThat(resultado.get("motivo").asText()).isEqualTo("token expirado");
        mvc.perform(get("/jwt/protegido").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenMalformadoNaoQuebraAValidacao() throws Exception {
        assertThat(validar("isto-nao-e-um-jwt").get("valido").asBoolean()).isFalse();
        assertThat(validar("a.b.c").get("valido").asBoolean()).isFalse();
    }

    // ---------------------------------------------- JWT é um padrão

    @Test
    void tokenDaBibliotecaEAceitoPelaValidacaoManual() throws Exception {
        assertThat(manual.validar(biblioteca.gerar("ana", "USER", 300)).valido()).isTrue();
    }

    @Test
    void tokenFeitoAMaoEAceitoPelaBiblioteca() throws Exception {
        assertThat(biblioteca.validar(manual.gerar("ana", "USER", 300)).getSubject()).isEqualTo("ana");
    }

    // ------------------------------------------------- desafio da chave fraca

    @Test
    void desafioComChaveFracaPermiteForjarUmTokenAdmin() throws Exception {
        String tokenUser = corpoDe(mvc.perform(get("/jwt/desafio/token")).andExpect(status().isOk())).get("token").asText();
        mvc.perform(get("/jwt/desafio/admin").header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isForbidden());

        // quem descobre a chave "ada123" forja um token ADMIN válido
        String forjado = manual.gerar("ada123".getBytes(StandardCharsets.UTF_8), "aluno", "ADMIN", 300);

        mvc.perform(get("/jwt/desafio/admin").header("Authorization", "Bearer " + forjado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("Parabéns")));
    }

    @Test
    void forjarComAChaveForteDoServidorNaoFuncionaNoDesafio() throws Exception {
        String forjado = manual.gerar("ana", "ADMIN", 300);   // assinado com a chave forte

        mvc.perform(get("/jwt/desafio/admin").header("Authorization", "Bearer " + forjado))
                .andExpect(status().isUnauthorized());
    }
}
