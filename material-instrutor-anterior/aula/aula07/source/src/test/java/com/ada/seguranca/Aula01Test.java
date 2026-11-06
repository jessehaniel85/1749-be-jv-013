package com.ada.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Herdado da aula 01. O teste do rate limiter mudou de lugar: com o tratamento global de
 * exceções (aula 07), a segunda chamada passa a devolver 429 e é testada em Aula07Test.
 */
@SpringBootTest
@AutoConfigureMockMvc
class Aula01Test {

    private static final String SCRIPT = "<script>alert('XSS')</script>";

    @Autowired
    private MockMvc mvc;

    @Test
    void buscaVulneravelDevolveOScriptSemTratar() throws Exception {
        String corpo = mvc.perform(get("/busca").param("q", SCRIPT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).contains(SCRIPT);
    }

    @Test
    void buscaSeguraEscapaOScript() throws Exception {
        String corpo = mvc.perform(get("/busca/segura").param("q", SCRIPT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpo).contains("&lt;script&gt;").doesNotContain("<script>");
    }
}
