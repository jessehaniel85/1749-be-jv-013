package com.ada.seguranca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Aula02HashTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private String hashDe(String texto) throws Exception {
        String resposta = mvc.perform(post("/cripto/hash").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("texto", texto))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(resposta).get("hash").asText();
    }

    @Test
    void devolveOSha256CorretoDeUmValorConhecido() throws Exception {
        // vetor de teste oficial do SHA-256: o hash de "abc"
        assertThat(hashDe("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void omesmoTextoGeraSempreOMesmoHash() throws Exception {
        assertThat(hashDe("pagar R$ 100")).isEqualTo(hashDe("pagar R$ 100"));
    }

    @Test
    void mudarUmCaractereMudaOHash() throws Exception {
        assertThat(hashDe("pagar R$ 100")).isNotEqualTo(hashDe("pagar R$ 900"));
    }

    @Test
    void oHashTemSempre64CaracteresHexadecimaisQualquerQueSejaOTamanhoDoTexto() throws Exception {
        assertThat(hashDe("a")).hasSize(64).matches("[0-9a-f]+");
        assertThat(hashDe("x".repeat(5000))).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void informaOAlgoritmoUsado() throws Exception {
        String resposta = mvc.perform(post("/cripto/hash").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"abc\"}"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(json.readTree(resposta).get("algoritmo").asText()).isEqualTo("SHA-256");
    }

    @Test
    void textoVazioDevolve400() throws Exception {
        mvc.perform(post("/cripto/hash").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
