package com.ada.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Aula07Test {

    private static final String ATAQUE = "x' OR '1'='1";

    @Autowired
    private MockMvc mvc;

    @Test
    void pesquisaVulneravelEnganadaPeloSqlInjectionDevolveTodosOsAlunos() throws Exception {
        mvc.perform(get("/alunos/pesquisa/vulneravel").param("nome", ATAQUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void pesquisaComInstrucaoPreparadaTrataOAtaqueComoUmNome() throws Exception {
        mvc.perform(get("/alunos/pesquisa").param("nome", ATAQUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void pesquisaComInstrucaoPreparadaEncontraONomeNormal() throws Exception {
        mvc.perform(get("/alunos/pesquisa").param("nome", "Batman"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("batman@ada.com"));
    }

    @Test
    void alunoInexistenteDevolve404ComCorpoPadronizado() throws Exception {
        mvc.perform(get("/alunos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value("Aluno 999 não encontrado"));
    }

    @Test
    void erroInesperadoDevolveMensagemGenericaSemVazarDetalhes() throws Exception {
        String corpo = mvc.perform(get("/erro-teste"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro interno. Tente novamente mais tarde."))
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).doesNotContain("jdbc").doesNotContain("IllegalStateException");
    }

    @Test
    void erroDoSpringMvcMantemOStatus400EmVezDeVirar500() throws Exception {
        // o 400 do cookie ausente (aula 02) não pode ser engolido pelo handler genérico de Exception
        mvc.perform(get("/boas-vindas/estrito"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Requisição inválida."));
    }

    @Test
    void rateLimiterAgoraDevolve429EmVezDe500() throws Exception {
        mvc.perform(get("/alunos")).andExpect(status().isOk());

        mvc.perform(get("/alunos"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}
