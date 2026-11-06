package com.ada.seguranca;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void segundaChamadaDentroDaJanelaEBloqueadaPeloRateLimiter() throws Exception {
        mvc.perform(get("/alunos")).andExpect(status().isOk());

        // sem tratamento de exceções (aula 07), o RequestNotPermitted escapa do controller
        // e, em uma execução real, vira um 500
        assertThatThrownBy(() -> mvc.perform(get("/alunos")))
                .isInstanceOf(ServletException.class)
                .hasCauseInstanceOf(RequestNotPermitted.class);
    }
}
