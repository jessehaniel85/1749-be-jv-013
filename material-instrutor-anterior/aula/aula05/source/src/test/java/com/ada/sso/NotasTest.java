package com.ada.sso;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testes da aplicação NOTAS como cliente do login único. */
@ActiveProfiles("notas")
class NotasTest extends ClienteSsoBase {

    // ------------------------------------------------------------------ passo 2: redirecionar ao provedor

    @Test
    void semSessaoAAplicacaoRedirecionaParaOProvedor() throws Exception {
        mvc.perform(get("/minhas-notas"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        startsWith("http://idp.localhost:9000/sso/autorizar?app=notas&state=")));
    }

    @Test
    void aAplicacaoNaoTemTelaDeLoginPropria() throws Exception {
        // nada de /login: quem não tem sessão vai para o provedor, e as senhas não existem aqui
        mvc.perform(get("/login")).andExpect(status().isFound())
                .andExpect(header().string("Location", startsWith("http://idp.localhost:9000/")));
    }

    @Test
    void faviconNaoViraAPaginaDeDestinoDoLogin() throws Exception {
        // sem permitAll no favicon, o navegador o pediria sozinho e o Spring o guardaria como destino
        mvc.perform(get("/favicon.ico")).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ passo 6: voltar do provedor

    @Test
    void fluxoCompletoCriaASessaoLocalEVoltaParaAPaginaPedida() throws Exception {
        MvcResult inicio = mvc.perform(get("/minhas-notas")).andReturn();
        MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
        String codigo = idp.emitirCodigoPara("ana", "Ana Souza", "ALUNO");

        mvc.perform(get("/sso/callback").param("code", codigo).param("state", stateDe(inicio)).session(sessao))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/minhas-notas")));

        // agora a sessão local basta: o provedor não participa mais
        mvc.perform(get("/minhas-notas").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Matemática")))
                .andExpect(content().string(containsString("8,5")));
    }

    @Test
    void paginaInicialMostraOQueVeioDoProvedor() throws Exception {
        MockHttpSession sessao = entrar("ana", "Ana Souza", "ALUNO", "/");

        mvc.perform(get("/").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana Souza")))
                .andExpect(content().string(containsString("ana@escola.test")))
                .andExpect(content().string(containsString("ALUNO")));
    }

    @Test
    void retornoSemEstadoConhecidoEhRecusado() throws Exception {
        MvcResult inicio = mvc.perform(get("/")).andReturn();
        MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
        String codigo = idp.emitirCodigoPara("ana", "Ana Souza", "ALUNO");

        // state que ESTA aplicação nunca gerou (alguém tentou injetar um código alheio)
        mvc.perform(get("/sso/callback").param("code", codigo).param("state", "outro-state-qualquer").session(sessao))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/").session(sessao)).andExpect(status().isFound());   // continua sem login
    }

    @Test
    void retornoSemSessaoNemStateEhRecusado() throws Exception {
        mvc.perform(get("/sso/callback").param("code", "x").param("state", "y"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oStateValeUmaVezSo() throws Exception {
        MvcResult inicio = mvc.perform(get("/")).andReturn();
        MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
        String state = stateDe(inicio);

        mvc.perform(get("/sso/callback").param("code", idp.emitirCodigoPara("ana", "Ana", "ALUNO"))
                .param("state", state).session(sessao)).andExpect(status().isFound());
        // repetir o mesmo retorno (replay) não passa
        mvc.perform(get("/sso/callback").param("code", idp.emitirCodigoPara("ana", "Ana", "ALUNO"))
                .param("state", state).session(sessao)).andExpect(status().isBadRequest());
    }

    @Test
    void codigoJaUsadoOuInventadoNaoCriaSessao() throws Exception {
        MvcResult inicio = mvc.perform(get("/")).andReturn();
        MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);

        mvc.perform(get("/sso/callback").param("code", "codigo-inventado").param("state", stateDe(inicio)).session(sessao))
                .andExpect(status().isBadGateway());
        mvc.perform(get("/").session(sessao)).andExpect(status().isFound());
    }

    // ------------------------------------------------------------------ verificação da asserção

    @Test
    void asercaoAssinadaPorOutraChaveEhRecusada() throws Exception {
        // O atacante forja uma asserção PERFEITA (iss, aud, claims certos) mas assinada com a chave dele
        KeyPair par = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        idp.asercaoForcada = JWT.create().withIssuer(IdpEmProcesso.EMISSOR).withAudience("notas")
                .withSubject("carlos").withClaim("uso", "login").withClaim("papel", "PROFESSOR")
                .withExpiresAt(Instant.now().plusSeconds(300))
                .sign(Algorithm.RSA256((RSAPublicKey) par.getPublic(), (RSAPrivateKey) par.getPrivate()));
        try {
            MvcResult inicio = mvc.perform(get("/")).andReturn();
            MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
            mvc.perform(get("/sso/callback").param("code", idp.emitirCodigoPara("carlos", "Carlos", "PROFESSOR"))
                    .param("state", stateDe(inicio)).session(sessao)).andExpect(status().isBadGateway());
            mvc.perform(get("/").session(sessao)).andExpect(status().isFound());
        } finally {
            idp.asercaoForcada = null;
        }
    }

    @Test
    void asercaoSemAssinaturaAlgNoneEhRecusada() throws Exception {
        idp.asercaoForcada = JWT.create().withIssuer(IdpEmProcesso.EMISSOR).withAudience("notas")
                .withSubject("carlos").withClaim("uso", "login").withClaim("papel", "PROFESSOR")
                .withExpiresAt(Instant.now().plusSeconds(300)).sign(Algorithm.none());
        try {
            MvcResult inicio = mvc.perform(get("/")).andReturn();
            MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
            mvc.perform(get("/sso/callback").param("code", idp.emitirCodigoPara("carlos", "Carlos", "PROFESSOR"))
                    .param("state", stateDe(inicio)).session(sessao)).andExpect(status().isBadGateway());
        } finally {
            idp.asercaoForcada = null;
        }
    }

    @Test
    void asercaoEmitidaParaOutraAplicacaoEhRecusada() throws Exception {
        // assinatura e emissor corretos (o provedor assinou de verdade), mas "aud" é a Biblioteca
        idp.asercaoForcada = idp.asercoes.gerarAsercao(new com.ada.sso.idp.Codigo(
                "ana", "Ana", "ana@escola.test", "ALUNO", "biblioteca", Instant.now().plusSeconds(60)));
        try {
            MvcResult inicio = mvc.perform(get("/")).andReturn();
            MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
            mvc.perform(get("/sso/callback").param("code", idp.emitirCodigoPara("ana", "Ana", "ALUNO"))
                    .param("state", stateDe(inicio)).session(sessao)).andExpect(status().isBadGateway());
        } finally {
            idp.asercaoForcada = null;
        }
    }

    @Test
    void tokenDeLogoutNaoServeComoAsercaoDeLogin() throws Exception {
        idp.asercaoForcada = idp.asercoes.gerarTokenDeLogout("ana", "notas");
        try {
            MvcResult inicio = mvc.perform(get("/")).andReturn();
            MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
            mvc.perform(get("/sso/callback").param("code", idp.emitirCodigoPara("ana", "Ana", "ALUNO"))
                    .param("state", stateDe(inicio)).session(sessao)).andExpect(status().isBadGateway());
        } finally {
            idp.asercaoForcada = null;
        }
    }

    // ------------------------------------------------------------------ autorização é local

    @Test
    void professorLancaNotas() throws Exception {
        MockHttpSession sessao = entrar("carlos", "Carlos Lima", "PROFESSOR", "/lancar-notas");

        mvc.perform(get("/lancar-notas").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Física")));
    }

    @Test
    void alunoLogadoMasSemPapelRecebePaginaDeSemPermissao() throws Exception {
        MockHttpSession sessao = entrar("ana", "Ana Souza", "ALUNO", "/");

        mvc.perform(get("/lancar-notas").session(sessao))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/negado"));
        mvc.perform(get("/negado").session(sessao)).andExpect(status().isOk());
    }

    @Test
    void alunoSoVeAsPropriasNotas() throws Exception {
        // o dono vem da sessão, não de parâmetro: carlos não tem notas lançadas como aluno
        MockHttpSession sessao = entrar("carlos", "Carlos Lima", "PROFESSOR", "/");

        mvc.perform(get("/minhas-notas").param("usuario", "ana").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Você não tem notas lançadas.")));
    }

    // ------------------------------------------------------------------ logout

    @Test
    void logoutUnicoDoProvedorEncerraAsSessoesLocaisDoUsuario() throws Exception {
        MockHttpSession sessaoAna = entrar("ana", "Ana Souza", "ALUNO", "/");
        MockHttpSession sessaoCarlos = entrar("carlos", "Carlos Lima", "PROFESSOR", "/");

        String tokenDeLogout = idp.asercoes.gerarTokenDeLogout("ana", "notas");
        mvc.perform(post("/sso/logout-backchannel").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("logout_token", tokenDeLogout))
                .andExpect(status().isNoContent());

        assertTrue(sessaoAna.isInvalid(), "a sessão da ana deveria ter sido encerrada");
        assertTrue(!sessaoCarlos.isInvalid(), "a sessão do carlos NÃO deveria ser afetada");
    }

    @Test
    void avisoDeLogoutSemAssinaturaDoProvedorEhIgnorado() throws Exception {
        MockHttpSession sessaoAna = entrar("ana", "Ana Souza", "ALUNO", "/");

        // qualquer um tentando derrubar a sessão da ana
        mvc.perform(post("/sso/logout-backchannel").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("logout_token", "isto.nao.vale"))
                .andExpect(status().isBadRequest());
        // um token de logout destinado a OUTRA aplicação também não vale aqui
        mvc.perform(post("/sso/logout-backchannel").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                        .param("logout_token", idp.asercoes.gerarTokenDeLogout("ana", "biblioteca")))
                .andExpect(status().isBadRequest());

        assertTrue(!sessaoAna.isInvalid());
    }

    @Test
    void asercaoDeLoginNaoServeComoOrdemDeLogout() throws Exception {
        MockHttpSession sessaoAna = entrar("ana", "Ana Souza", "ALUNO", "/");
        String asercao = idp.asercoes.gerarAsercao(new com.ada.sso.idp.Codigo(
                "ana", "Ana", "ana@escola.test", "ALUNO", "notas", Instant.now().plusSeconds(60)));

        mvc.perform(post("/sso/logout-backchannel").contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED).param("logout_token", asercao))
                .andExpect(status().isBadRequest());
        assertTrue(!sessaoAna.isInvalid());
    }

    @Test
    void sairSoDesteSistemaEncerraASessaoLocal() throws Exception {
        MockHttpSession sessao = entrar("ana", "Ana Souza", "ALUNO", "/");

        mvc.perform(post("/sair").with(csrf()).session(sessao)).andExpect(status().isFound());
        assertTrue(sessao.isInvalid());
    }

    @Test
    void sairSemTokenCsrfEhRecusado() throws Exception {
        MockHttpSession sessao = entrar("ana", "Ana Souza", "ALUNO", "/");

        mvc.perform(post("/sair").session(sessao)).andExpect(status().isForbidden());
        assertTrue(!sessao.isInvalid());
    }
}
