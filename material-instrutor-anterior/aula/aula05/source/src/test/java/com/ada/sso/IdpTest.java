package com.ada.sso;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do PROVEDOR DE IDENTIDADE: login, sessão única, emissão e troca do código, e logout.
 * (MockMvc em vez de um servidor real, como nas outras aulas.)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("idp")
class IdpTest {

    private static final String STATE = "estado-de-teste-1234";
    private static final String SEGREDO_NOTAS = "segredo-de-demonstracao-notas-0001";
    private static final String SEGREDO_BIBLIOTECA = "segredo-de-demonstracao-biblioteca-0002";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    // ------------------------------------------------------------------ apoio

    private MockHttpSession login(String usuario, String senha) throws Exception {
        MvcResult r = mvc.perform(post("/sso/login").param("username", usuario).param("password", senha).with(csrf()))
                .andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    private String location(MvcResult r) {
        return r.getResponse().getHeader("Location");
    }

    /** Pede o código para a aplicação, já com a sessão do provedor. Devolve o código. */
    private String pedirCodigo(MockHttpSession sessao, String app) throws Exception {
        MvcResult r = mvc.perform(get("/sso/autorizar").param("app", app).param("state", STATE).session(sessao))
                .andExpect(status().isFound()).andReturn();
        return UriComponentsBuilder.fromUriString(location(r)).build().getQueryParams().getFirst("code");
    }

    private String basic(String app, String segredo) {
        return "Basic " + Base64.getEncoder().encodeToString((app + ":" + segredo).getBytes());
    }

    private RSAPublicKey chavePublica() throws Exception {
        String corpo = mvc.perform(get("/sso/chave-publica")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        byte[] bytes = Base64.getDecoder().decode(json.readTree(corpo).get("chave").asText());
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    }

    // ------------------------------------------------------------------ login e sessão única

    @Test
    void semSessaoNoProvedorAutorizarMandaParaALogin() throws Exception {
        MvcResult r = mvc.perform(get("/sso/autorizar").param("app", "notas").param("state", STATE))
                .andExpect(status().isFound()).andReturn();
        assertTrue(location(r).endsWith("/sso/login"), location(r));
    }

    @Test
    void depoisDoLoginOProvedorVoltaSozinhoAoFluxoDaAplicacao() throws Exception {
        // 1) tenta autorizar sem estar logado: o Spring guarda a URL e manda para a tela de login
        MvcResult primeiro = mvc.perform(get("/sso/autorizar").param("app", "notas").param("state", STATE))
                .andReturn();
        MockHttpSession sessao = (MockHttpSession) primeiro.getRequest().getSession(false);

        // 2) digita a senha: o provedor devolve o usuário à /sso/autorizar original
        MvcResult aposLogin = mvc.perform(post("/sso/login").param("username", "ana")
                .param("password", "Escola@2026").with(csrf()).session(sessao)).andExpect(status().isFound()).andReturn();
        // (o Spring Security 6.3 acrescenta "?continue" para reconhecer a requisição guardada)
        assertTrue(location(aposLogin).contains("/sso/autorizar"), location(aposLogin));

        // 3) o navegador segue o redirecionamento: agora há sessão, então o código sai direto
        //    e o "app" e o "state" originais foram restaurados
        MvcResult retomada = mvc.perform(get(location(aposLogin)).session(sessao))
                .andExpect(status().isFound()).andReturn();
        var destino = UriComponentsBuilder.fromUriString(location(retomada)).build();
        assertEquals("notas.localhost", destino.getHost());
        assertEquals(STATE, destino.getQueryParams().getFirst("state"));
        assertNotNull(destino.getQueryParams().getFirst("code"));
    }

    @Test
    void senhaErradaVoltaParaALoginComErro() throws Exception {
        mvc.perform(post("/sso/login").param("username", "ana").param("password", "errada").with(csrf()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/sso/login?erro"));
    }

    @Test
    void usuarioInexistenteTemAMesmaRespostaDeSenhaErrada() throws Exception {
        mvc.perform(post("/sso/login").param("username", "fantasma").param("password", "qualquer").with(csrf()))
                .andExpect(header().string("Location", "/sso/login?erro"));
    }

    @Test
    void loginSemTokenCsrfEhRecusado() throws Exception {
        mvc.perform(post("/sso/login").param("username", "ana").param("password", "Escola@2026"))
                .andExpect(status().isForbidden());
    }

    @Test
    void comSessaoNoProvedorAutorizarEmiteOCodigoSemPedirSenha() throws Exception {
        MockHttpSession sessao = login("ana", "Escola@2026");

        MvcResult r = mvc.perform(get("/sso/autorizar").param("app", "notas").param("state", STATE).session(sessao))
                .andExpect(status().isFound()).andReturn();

        var destino = UriComponentsBuilder.fromUriString(location(r)).build();
        assertEquals("notas.localhost", destino.getHost());
        assertEquals("/sso/callback", destino.getPath());
        assertNotNull(destino.getQueryParams().getFirst("code"));
        assertEquals(STATE, destino.getQueryParams().getFirst("state"));   // devolvido intacto
    }

    @Test
    void segundaAplicacaoTambemRecebeCodigoComAMesmaSessao() throws Exception {
        // É o "login único": uma senha, duas aplicações.
        MockHttpSession sessao = login("ana", "Escola@2026");

        String paraNotas = pedirCodigo(sessao, "notas");
        String paraBiblioteca = pedirCodigo(sessao, "biblioteca");

        assertNotNull(paraNotas);
        assertNotNull(paraBiblioteca);
        assertTrue(!paraNotas.equals(paraBiblioteca));
    }

    // ------------------------------------------------------------------ validação do pedido

    @Test
    void aplicacaoDesconhecidaDevolve400() throws Exception {
        MockHttpSession sessao = login("ana", "Escola@2026");
        mvc.perform(get("/sso/autorizar").param("app", "intrusa").param("state", STATE).session(sessao))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stateInvalidoDevolve400() throws Exception {
        MockHttpSession sessao = login("ana", "Escola@2026");
        mvc.perform(get("/sso/autorizar").param("app", "notas").param("state", "<script>").session(sessao))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oClienteNaoEscolheParaOndeOProvedorVolta() throws Exception {
        // tentativa de "open redirect": o parâmetro "retorno" é ignorado; vale o endereço cadastrado
        MockHttpSession sessao = login("ana", "Escola@2026");
        MvcResult r = mvc.perform(get("/sso/autorizar").param("app", "notas").param("state", STATE)
                .param("retorno", "http://site-do-atacante.test/roubar").session(sessao)).andReturn();
        assertTrue(location(r).startsWith("http://notas.localhost:8081/sso/callback?"), location(r));
    }

    // ------------------------------------------------------------------ troca do código pela asserção

    @Test
    void aAplicacaoTrocaOCodigoPelaAsercaoAssinada() throws Exception {
        MockHttpSession sessao = login("carlos", "Escola@2026");
        String codigo = pedirCodigo(sessao, "notas");

        String corpo = mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", codigo)
                        .header("Authorization", basic("notas", SEGREDO_NOTAS)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString();
        String asercao = json.readTree(corpo).get("assercao").asText();

        // confere COMO UMA APLICAÇÃO FARIA: só com a chave pública
        DecodedJWT jwt = JWT.require(Algorithm.RSA256(chavePublica(), null))
                .withIssuer("http://idp.localhost:9000").withAudience("notas").build().verify(asercao);
        assertEquals("carlos", jwt.getSubject());
        assertEquals("Carlos Lima", jwt.getClaim("nome").asString());
        assertEquals("PROFESSOR", jwt.getClaim("papel").asString());
        assertEquals("login", jwt.getClaim("uso").asString());
        // a senha nunca viaja na asserção
        assertTrue(!asercao.contains("Escola"), "a asserção não deve conter a senha");
    }

    @Test
    void oCodigoValeUmaVezSo() throws Exception {
        String codigo = pedirCodigo(login("ana", "Escola@2026"), "notas");

        mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", codigo).header("Authorization", basic("notas", SEGREDO_NOTAS)))
                .andExpect(status().isOk());
        mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", codigo).header("Authorization", basic("notas", SEGREDO_NOTAS)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void segredoErradoEhRecusadoComEMesmoQueOCodigoSejaValido() throws Exception {
        String codigo = pedirCodigo(login("ana", "Escola@2026"), "notas");

        mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", codigo).header("Authorization", basic("notas", "chute")))
                .andExpect(status().isUnauthorized());
        // e o código NÃO foi gasto pela tentativa recusada: a aplicação certa ainda consegue
        mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", codigo).header("Authorization", basic("notas", SEGREDO_NOTAS)))
                .andExpect(status().isOk());
    }

    @Test
    void semCredencialDaAplicacaoDevolve401() throws Exception {
        String codigo = pedirCodigo(login("ana", "Escola@2026"), "notas");
        mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", codigo)).andExpect(status().isUnauthorized());
    }

    @Test
    void umCodigoDaNotasNaoServeParaABiblioteca() throws Exception {
        String codigo = pedirCodigo(login("ana", "Escola@2026"), "notas");

        // a Biblioteca tem o segredo DELA (certo), mas o código foi emitido para as Notas
        mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", codigo)
                        .header("Authorization", basic("biblioteca", SEGREDO_BIBLIOTECA)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void codigoInventadoDevolve400() throws Exception {
        mvc.perform(post("/sso/token").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("code", "inventado")
                        .header("Authorization", basic("notas", SEGREDO_NOTAS)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ logout

    @Test
    void paginaDeConfirmacaoDeSaidaEhPublicaMasSairExigePost() throws Exception {
        mvc.perform(get("/sso/sair").param("app", "notas")).andExpect(status().isOk());
        // sem token CSRF, o POST é recusado: um site qualquer não deslogaria o usuário
        mvc.perform(post("/sso/sair")).andExpect(status().isForbidden());
    }

    @Test
    void logoutEncerraASessaoDoProvedorMesmoComAplicacoesForaDoAr() throws Exception {
        MockHttpSession sessao = login("ana", "Escola@2026");
        pedirCodigo(sessao, "notas");   // as Notas passam a constar como "conectadas"

        // As aplicações NÃO estão rodando neste teste: o aviso falha, é registrado, e o logout conclui.
        mvc.perform(post("/sso/sair").param("app", "notas").with(csrf()).session(sessao))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/sso/saiu?app=notas"));

        assertTrue(sessao.isInvalid(), "a sessão do provedor deveria ter sido invalidada");
        // sem sessão, o SSO volta a exigir senha
        mvc.perform(get("/sso/autorizar").param("app", "notas").param("state", STATE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/sso/login")));
    }

    @Test
    void paginaSaiuNaoAceitaDestinoDaUrl() throws Exception {
        // o link "voltar" vem do cadastro: um app inventado não gera link nenhum
        String html = mvc.perform(get("/sso/saiu").param("app", "http://atacante.test"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(!html.contains("atacante.test"));
    }

    @Test
    void chavePublicaEhPublicaESemChavePrivada() throws Exception {
        String corpo = mvc.perform(get("/sso/chave-publica")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode no = json.readTree(corpo);
        assertEquals("RS256", no.get("algoritmo").asText());
        assertTrue(no.get("chave").asText().length() > 300);
    }
}
