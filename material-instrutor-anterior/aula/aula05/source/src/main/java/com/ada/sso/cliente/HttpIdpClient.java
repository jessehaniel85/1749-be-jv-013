package com.ada.sso.cliente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/** A implementação real de IdpClient: HTTP para o endereço INTERNO do provedor. */
@Component
@Profile({"notas", "biblioteca"})
public class HttpIdpClient implements IdpClient {

    private final ClienteProperties props;
    private final RestClient http;

    @Autowired
    public HttpIdpClient(ClienteProperties props) {
        // Prazos curtos: um provedor lento ou fora do ar não pode deixar o login pendurado.
        this(props, criarCliente(props));
    }

    // Usado pelos testes, que trocam o cliente HTTP por um servidor de mentira.
    public HttpIdpClient(ClienteProperties props, RestClient http) {
        this.props = props;
        this.http = http;
    }

    private static RestClient criarCliente(ClienteProperties props) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(3000);
        fabrica.setReadTimeout(3000);
        return RestClient.builder().requestFactory(fabrica).baseUrl(props.idpUrlInterna()).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String trocarCodigo(String codigo) {
        MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
        corpo.add("code", codigo);
        try {
            Map<String, Object> resposta = http.post().uri("/sso/token")
                    // a aplicação se identifica ao provedor: id + segredo (HTTP Basic)
                    .headers(h -> h.setBasicAuth(props.id(), props.segredo()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(corpo)
                    .retrieve()
                    .body(Map.class);
            Object asercao = resposta == null ? null : resposta.get("assercao");
            if (asercao == null) {
                throw new SsoException("O provedor não devolveu a asserção.");
            }
            return asercao.toString();
        } catch (RestClientException e) {
            // 400 (código inválido/usado/expirado), 401 (segredo errado) ou provedor fora do ar
            throw new SsoException("O provedor recusou ou não respondeu à troca do código.", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String buscarChavePublica() {
        try {
            Map<String, Object> resposta = http.get().uri("/sso/chave-publica").retrieve().body(Map.class);
            Object chave = resposta == null ? null : resposta.get("chave");
            if (chave == null) {
                throw new SsoException("O provedor não publicou a chave.");
            }
            return chave.toString();
        } catch (RestClientException e) {
            throw new SsoException("Não foi possível obter a chave pública do provedor.", e);
        }
    }
}
