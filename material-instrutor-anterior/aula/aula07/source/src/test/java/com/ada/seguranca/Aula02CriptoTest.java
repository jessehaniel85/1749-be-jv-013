package com.ada.seguranca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Aula02CriptoTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private String chamar(String caminho, Map<String, String> corpo, int statusEsperado, String campo) throws Exception {
        String resposta = mvc.perform(post(caminho).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(corpo)))
                .andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return campo == null ? resposta : json.readTree(resposta).get(campo).asText();
    }

    // ---------- simétrica ----------

    @Test
    void simetricaCifraEDecifraComAMesmaChave() throws Exception {
        String cifrado = chamar("/cripto/simetrica/cifrar", Map.of("texto", "Olá, Ada!"), 200, "cifrado");

        assertThat(cifrado).isNotEqualTo("Olá, Ada!");
        assertThat(chamar("/cripto/simetrica/decifrar", Map.of("cifrado", cifrado), 200, "texto"))
                .isEqualTo("Olá, Ada!");
    }

    @Test
    void simetricaDevolveUmResultadoDiferenteACadaCifragemPorCausaDoIvAleatorio() throws Exception {
        String a = chamar("/cripto/simetrica/cifrar", Map.of("texto", "mesmo texto"), 200, "cifrado");
        String b = chamar("/cripto/simetrica/cifrar", Map.of("texto", "mesmo texto"), 200, "cifrado");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void simetricaRejeitaOTextoCifradoAdulterado() throws Exception {
        String cifrado = chamar("/cripto/simetrica/cifrar", Map.of("texto", "transferir R$ 100"), 200, "cifrado");
        byte[] bytes = Base64.getDecoder().decode(cifrado);
        bytes[bytes.length - 1] ^= 1;   // altera um único bit
        String adulterado = Base64.getEncoder().encodeToString(bytes);

        chamar("/cripto/simetrica/decifrar", Map.of("cifrado", adulterado), 400, null);
    }

    // ---------- assimétrica ----------

    @Test
    void assimetricaExpoeSoAChavePublica() throws Exception {
        String corpo = mvc.perform(get("/cripto/assimetrica/chave-publica"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(json.readTree(corpo).get("chavePublica").asText()).isNotBlank();
        assertThat(corpo).doesNotContainIgnoringCase("private");
    }

    @Test
    void assimetricaCifraComAPublicaEDecifraComAPrivada() throws Exception {
        String cifrado = chamar("/cripto/assimetrica/cifrar", Map.of("texto", "senha: ada123"), 200, "cifrado");

        assertThat(cifrado).isNotEqualTo("senha: ada123");
        assertThat(chamar("/cripto/assimetrica/decifrar", Map.of("cifrado", cifrado), 200, "texto"))
                .isEqualTo("senha: ada123");
    }

    @Test
    void assimetricaNaoCifraTextoMaiorQueOLimiteDoRsa() throws Exception {
        chamar("/cripto/assimetrica/cifrar", Map.of("texto", "x".repeat(191)), 400, null);
    }

    @Test
    void assinaturaValidaComATextoOriginalEInvalidaSeOTextoMudar() throws Exception {
        String assinatura = chamar("/cripto/assimetrica/assinar", Map.of("texto", "pagar R$ 100"), 200, "assinatura");

        String ok = chamar("/cripto/assimetrica/verificar",
                Map.of("texto", "pagar R$ 100", "assinatura", assinatura), 200, "valida");
        String adulterado = chamar("/cripto/assimetrica/verificar",
                Map.of("texto", "pagar R$ 900", "assinatura", assinatura), 200, "valida");

        assertThat(ok).isEqualTo("true");
        assertThat(adulterado).isEqualTo("false");
    }

    @Test
    void textoVazioDevolve400() throws Exception {
        chamar("/cripto/simetrica/cifrar", Map.of("texto", ""), 400, null);
    }
}
