package com.ada.sso;

import com.ada.sso.cliente.ClienteProperties;
import com.ada.sso.cliente.IdpClient;
import com.ada.sso.cliente.IniciaLoginSso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Base dos testes das APLICAÇÕES (Notas e Biblioteca): troca a chamada HTTP ao provedor pelo
 * provedor em memória (IdpEmProcesso). Cada subclasse escolhe o perfil com @ActiveProfiles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ClienteSsoBase.Config.class)
abstract class ClienteSsoBase {

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        IdpEmProcesso idpEmProcesso(ClienteProperties props) {
            return new IdpEmProcesso(props.id());
        }
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected IdpEmProcesso idp;
    @Autowired protected ClienteProperties props;

    /** Faz o caminho completo do SSO e devolve a sessão local já autenticada. */
    protected MockHttpSession entrar(String username, String nome, String papel, String paginaDesejada) throws Exception {
        // 1) acessa a página sem sessão: a aplicação manda para o provedor, com um "state"
        MvcResult inicio = mvc.perform(get(paginaDesejada)).andReturn();
        MockHttpSession sessao = (MockHttpSession) inicio.getRequest().getSession(false);
        String state = stateDe(inicio);

        // 2) o provedor autentica e devolve o navegador com um código
        String codigo = idp.emitirCodigoPara(username, nome, papel);

        // 3) o navegador chega ao callback da aplicação
        mvc.perform(get("/sso/callback").param("code", codigo).param("state", state).session(sessao));
        return sessao;
    }

    protected String stateDe(MvcResult r) {
        String destino = r.getResponse().getHeader("Location");
        return UriComponentsBuilder.fromUriString(destino).build().getQueryParams().getFirst("state");
    }

    protected static final String ESTADOS = IniciaLoginSso.ESTADOS_PENDENTES;
}
