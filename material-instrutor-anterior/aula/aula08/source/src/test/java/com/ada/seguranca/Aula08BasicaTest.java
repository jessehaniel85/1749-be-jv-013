package com.ada.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("basica")
class Aula08BasicaTest {

    private static final String BASIC =
            "Basic " + Base64.getEncoder().encodeToString("aluno:ada123".getBytes());

    @Autowired
    private MockMvc mvc;

    @Test
    void rotaPublicaNaoExigeCredenciais() throws Exception {
        mvc.perform(get("/publico/ola")).andExpect(status().isOk());
    }

    @Test
    void rotaProtegidaSemCredenciaisDevolve401() throws Exception {
        mvc.perform(get("/alunos/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaComCredenciaisCorretasDevolve200() throws Exception {
        mvc.perform(get("/alunos/1").header("Authorization", BASIC)).andExpect(status().isOk());
    }

    @Test
    void credenciaisErradasDevolvem401() throws Exception {
        String errado = "Basic " + Base64.getEncoder().encodeToString("aluno:errada".getBytes());

        mvc.perform(get("/alunos/1").header("Authorization", errado)).andExpect(status().isUnauthorized());
    }

    @Test
    void postComCredenciaisCorretasAindaDevolve403PorCausaDoCsrf() throws Exception {
        mvc.perform(post("/alunos").header("Authorization", BASIC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Aquaman\",\"email\":\"aquaman@ada.com\"}"))
                .andExpect(status().isForbidden());
    }
}
