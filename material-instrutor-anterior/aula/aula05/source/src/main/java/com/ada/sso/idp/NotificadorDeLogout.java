package com.ada.sso.idp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Set;

/**
 * LOGOUT ÚNICO (Single Logout), por canal de fundo (back-channel).
 *
 * Quando o usuário sai no provedor, o provedor avisa, de servidor para servidor, cada aplicação
 * em que ele entrou, enviando um token de logout assinado. Cada aplicação encerra as sessões
 * locais daquele usuário. Sem isto, sair do provedor deixaria o usuário logado nas aplicações.
 *
 * Por que isto é "difícil de implementar bem" (risco da aula):
 *  - o provedor precisa ENXERGAR cada aplicação na rede (aqui, o endereço interno de cada uma);
 *  - se uma aplicação estiver fora do ar naquele momento, ela NÃO é avisada (tentamos uma vez
 *    e registramos a falha; um sistema real faria novas tentativas);
 *  - cada aplicação precisa saber encerrar as sessões de um usuário.
 *
 * Roda ANTES de a sessão do provedor ser invalidada (precisamos ler dela quais aplicações avisar).
 */
@Component
@Profile("idp")
public class NotificadorDeLogout implements LogoutHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificadorDeLogout.class);

    private final IdpProperties props;
    private final AsercaoService asercoes;
    private final RestClient http;

    @Autowired
    public NotificadorDeLogout(IdpProperties props, AsercaoService asercoes) {
        this(props, asercoes, criarCliente());
    }

    // Usado pelos testes, que trocam o cliente HTTP por um servidor de mentira.
    public NotificadorDeLogout(IdpProperties props, AsercaoService asercoes, RestClient http) {
        this.props = props;
        this.asercoes = asercoes;
        this.http = http;
    }

    private static RestClient criarCliente() {
        // Prazos curtos: uma aplicação lenta ou fora do ar não pode travar o logout do usuário.
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(2000);
        fabrica.setReadTimeout(2000);
        return RestClient.builder().requestFactory(fabrica).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication autenticacao) {
        HttpSession sessao = request.getSession(false);
        if (sessao == null || autenticacao == null) {
            return;
        }
        Set<String> apps = (Set<String>) sessao.getAttribute(AutorizarController.APPS_CONECTADAS);
        if (apps == null) {
            return;
        }
        for (String id : Set.copyOf(apps)) {
            IdpProperties.Aplicacao aplicacao = props.aplicacao(id);
            if (aplicacao == null) {
                continue;
            }
            try {
                MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
                corpo.add("logout_token", asercoes.gerarTokenDeLogout(autenticacao.getName(), id));
                http.post().uri(aplicacao.urlLogout())
                        .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                        .body(corpo)
                        .retrieve().toBodilessEntity();
                log.info("[IdP] logout único: aplicação '{}' avisada sobre a saída de '{}'", id, autenticacao.getName());
            } catch (Exception e) {
                // Aplicação fora do ar ou recusou: registramos e seguimos. O logout do provedor acontece de qualquer jeito.
                log.warn("[IdP] logout único: não foi possível avisar a aplicação '{}' ({})", id, e.getMessage());
            }
        }
    }
}
