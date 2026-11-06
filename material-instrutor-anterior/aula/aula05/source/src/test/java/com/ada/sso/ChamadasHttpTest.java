package com.ada.sso;

import com.ada.sso.cliente.ClienteProperties;
import com.ada.sso.cliente.HttpIdpClient;
import com.ada.sso.cliente.SsoException;
import com.ada.sso.idp.AsercaoService;
import com.ada.sso.idp.ChavesIdp;
import com.ada.sso.idp.IdpProperties;
import com.ada.sso.idp.NotificadorDeLogout;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Testa as chamadas HTTP servidor a servidor SEM abrir porta: um servidor de mentira confere o
 * que cada lado envia (endereço, método, cabeçalho Basic, corpo).
 */
class ChamadasHttpTest {

    private final ClienteProperties props = new ClienteProperties("notas", "Notas", "#000", "segredo-notas",
            "http://idp.localhost:9000", "http://localhost:9000", List.of());

    // ------------------------------------------------------------------ aplicação -> provedor

    @Test
    void aAplicacaoTrocaOCodigoComIdESegredoEmHttpBasic() {
        RestClient.Builder builder = RestClient.builder().baseUrl(props.idpUrlInterna());
        MockRestServiceServer provedor = MockRestServiceServer.bindTo(builder).build();
        String basicEsperado = "Basic " + java.util.Base64.getEncoder().encodeToString("notas:segredo-notas".getBytes());
        provedor.expect(requestTo("http://localhost:9000/sso/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", basicEsperado))
                .andExpect(header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"))
                .andExpect(content().string("code=abc123"))
                .andRespond(withSuccess("{\"assercao\":\"jwt-de-mentira\",\"expiresIn\":300}", MediaType.APPLICATION_JSON));

        String asercao = new HttpIdpClient(props, builder.build()).trocarCodigo("abc123");

        assertEquals("jwt-de-mentira", asercao);
        provedor.verify();
    }

    @Test
    void provedorQueRecusaOCodigoViraSsoException() {
        RestClient.Builder builder = RestClient.builder().baseUrl(props.idpUrlInterna());
        MockRestServiceServer provedor = MockRestServiceServer.bindTo(builder).build();
        provedor.expect(requestTo("http://localhost:9000/sso/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"erro\":\"codigo_invalido\"}"));

        assertThrows(SsoException.class, () -> new HttpIdpClient(props, builder.build()).trocarCodigo("x"));
    }

    @Test
    void respostaSemAsercaoViraSsoException() {
        RestClient.Builder builder = RestClient.builder().baseUrl(props.idpUrlInterna());
        MockRestServiceServer provedor = MockRestServiceServer.bindTo(builder).build();
        provedor.expect(requestTo("http://localhost:9000/sso/token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(SsoException.class, () -> new HttpIdpClient(props, builder.build()).trocarCodigo("x"));
    }

    @Test
    void aAplicacaoBuscaAChavePublicaNoEnderecoInterno() {
        RestClient.Builder builder = RestClient.builder().baseUrl(props.idpUrlInterna());
        MockRestServiceServer provedor = MockRestServiceServer.bindTo(builder).build();
        provedor.expect(requestTo("http://localhost:9000/sso/chave-publica"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"algoritmo\":\"RS256\",\"chave\":\"MIIB...\"}", MediaType.APPLICATION_JSON));

        assertEquals("MIIB...", new HttpIdpClient(props, builder.build()).buscarChavePublica());
    }

    // ------------------------------------------------------------------ provedor -> aplicações (logout único)

    @Test
    void oProvedorAvisaCadaAplicacaoConectadaNoLogout() {
        IdpProperties idp = new IdpProperties("http://idp.localhost:9000", Map.of(
                "notas", new IdpProperties.Aplicacao("Notas", "http://notas.localhost:8081/sso/callback",
                        "http://localhost:8081/sso/logout-backchannel", "http://notas.localhost:8081/", "s1"),
                "biblioteca", new IdpProperties.Aplicacao("Biblioteca", "http://biblioteca.localhost:8082/sso/callback",
                        "http://localhost:8082/sso/logout-backchannel", "http://biblioteca.localhost:8082/", "s2")));
        AsercaoService asercoes = new AsercaoService(idp, new ChavesIdp());

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer apps = MockRestServiceServer.bindTo(builder).build();
        for (String url : List.of("http://localhost:8081/sso/logout-backchannel", "http://localhost:8082/sso/logout-backchannel")) {
            apps.expect(requestTo(url))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().string(org.hamcrest.Matchers.startsWith("logout_token=ey")))   // um JWT
                    .andRespond(withStatus(HttpStatus.NO_CONTENT));
        }

        MockHttpServletRequest request = new MockHttpServletRequest();
        Set<String> conectadas = new LinkedHashSet<>(List.of("notas", "biblioteca"));
        request.getSession().setAttribute("apps-conectadas", conectadas);

        new NotificadorDeLogout(idp, asercoes, builder.build()).logout(request, new MockHttpServletResponse(),
                UsernamePasswordAuthenticationToken.authenticated("ana", null, List.of()));

        apps.verify();   // as duas aplicações foram avisadas
    }

    @Test
    void aplicacaoForaDoArNaoImpedeOLogout() {
        IdpProperties idp = new IdpProperties("http://idp.localhost:9000", Map.of(
                "notas", new IdpProperties.Aplicacao("Notas", "http://notas.localhost:8081/sso/callback",
                        "http://localhost:8081/sso/logout-backchannel", "http://notas.localhost:8081/", "s1")));
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer apps = MockRestServiceServer.bindTo(builder).build();
        apps.expect(requestTo("http://localhost:8081/sso/logout-backchannel"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("apps-conectadas", new LinkedHashSet<>(List.of("notas")));

        // não lança exceção: a falha é registrada e o logout segue
        new NotificadorDeLogout(idp, new AsercaoService(idp, new ChavesIdp()), builder.build())
                .logout(request, new MockHttpServletResponse(),
                        UsernamePasswordAuthenticationToken.authenticated("ana", null, List.of()));
        apps.verify();
    }
}
