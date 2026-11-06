package com.ada.seguranca;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Aula02Test {

    @Autowired
    private MockMvc mvc;

    @Test
    void semCookieUsaOValorPadrao() throws Exception {
        mvc.perform(get("/boas-vindas"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("default-user-id")));
    }

    @Test
    void comCookieUsaOValorDoCookie() throws Exception {
        mvc.perform(get("/boas-vindas").cookie(new Cookie("user-id", "42")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("42")));
    }

    @Test
    void semDefaultValueECookieAusenteDevolve400() throws Exception {
        mvc.perform(get("/boas-vindas/estrito"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cookieDeLoginTemOsAtributosDeSeguranca() throws Exception {
        String setCookie = mvc.perform(get("/login-teste"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE);

        assertThat(setCookie)
                .contains("user-id=42", "HttpOnly", "Secure", "SameSite=Strict", "Max-Age=3600");
    }

    @Test
    void cookieLegivelNaoTemHttpOnly() throws Exception {
        String setCookie = mvc.perform(get("/login-teste/legivel"))
                .andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).contains("user-id-legivel=42").doesNotContain("HttpOnly");
    }

    // Faz o login do exemplo e devolve o valor do cookie session-id (o ID da sessão).
    private String fazLogin(String usuario) throws Exception {
        String setCookie = mvc.perform(get("/login-teste/session-id").param("usuario", usuario))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).startsWith("session-id=")
                .contains("HttpOnly", "Secure", "SameSite=Strict", "Max-Age=1800");
        return setCookie.substring("session-id=".length(), setCookie.indexOf(';'));
    }

    @Test
    void cookieDeSessaoLevaSoUmIdAleatorioENaoOUsuario() throws Exception {
        String id = fazLogin("ana");

        // 16 bytes em hexadecimal: 32 caracteres, e o cookie NÃO revela quem é o usuário
        assertThat(id).hasSize(32).matches("[0-9A-F]+").doesNotContain("ana");
        // dois logins geram IDs diferentes
        assertThat(fazLogin("ana")).isNotEqualTo(id);
    }

    @Test
    void servidorAchaOUsuarioPeloIdDaSessao() throws Exception {
        String id = fazLogin("bia");

        mvc.perform(get("/perfil-teste/session-id").cookie(new Cookie("session-id", id)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bia")));
    }

    @Test
    void semCookieOuComIdInventadoDevolve401() throws Exception {
        mvc.perform(get("/perfil-teste/session-id"))
                .andExpect(status().isUnauthorized());
        // trocar o ID no DevTools: não existe ficha para ele
        mvc.perform(get("/perfil-teste/session-id").cookie(new Cookie("session-id", "42")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioComCaracteresEstranhosDevolve400() throws Exception {
        mvc.perform(get("/login-teste/session-id").param("usuario", "<script>"))
                .andExpect(status().isBadRequest());
    }
}
