package com.ada.sso;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da aplicação BIBLIOTECA: o mesmo código das Notas rodando com outro perfil, com a SUA
 * identidade (app=biblioteca) e as SUAS regras de permissão.
 */
@ActiveProfiles("biblioteca")
class BibliotecaTest extends ClienteSsoBase {

    @Test
    void semSessaoRedirecionaParaOProvedorIdentificandoABiblioteca() throws Exception {
        mvc.perform(get("/meus-emprestimos"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        startsWith("http://idp.localhost:9000/sso/autorizar?app=biblioteca&state=")));
    }

    @Test
    void alunaVeOsProprioEmprestimos() throws Exception {
        MockHttpSession sessao = entrar("ana", "Ana Souza", "ALUNO", "/meus-emprestimos");

        mvc.perform(get("/meus-emprestimos").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dom Casmurro")));
    }

    @Test
    void bibliotecariaGerenciaOAcervo() throws Exception {
        MockHttpSession sessao = entrar("marta", "Marta Reis", "BIBLIOTECARIO", "/acervo/gerenciar");

        mvc.perform(get("/acervo/gerenciar").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Exemplares")));
    }

    @Test
    void professorQueLancaNotasNaoGerenciaOAcervo() throws Exception {
        // o MESMO usuário (carlos, PROFESSOR) tem poder em um sistema e não no outro: cada aplicação decide
        MockHttpSession sessao = entrar("carlos", "Carlos Lima", "PROFESSOR", "/");

        mvc.perform(get("/acervo/gerenciar").session(sessao))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/negado"));
    }

    @Test
    void asercaoDasNotasNaoServeNaBiblioteca() throws Exception {
        // O código/asserção é emitido para uma aplicação (aud). Este provedor de teste serve a Biblioteca;
        // uma asserção emitida para "notas" é recusada.
        idp.asercaoForcada = idp.asercoes.gerarAsercao(new com.ada.sso.idp.Codigo(
                "ana", "Ana", "ana@escola.test", "ALUNO", "notas",
                java.time.Instant.now().plusSeconds(60)));
        try {
            var inicio = mvc.perform(get("/")).andReturn();
            MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
            mvc.perform(get("/sso/callback").param("code", idp.emitirCodigoPara("ana", "Ana", "ALUNO"))
                    .param("state", stateDe(inicio)).session(sessao)).andExpect(status().isBadGateway());
        } finally {
            idp.asercaoForcada = null;
        }
    }
}
