package com.ada.sso.idp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * A configuração do provedor (bloco "sso.idp" do application-idp.yml).
 *
 * @param emissor     endereço público do provedor; vai no claim "iss" das asserções.
 * @param aplicacoes  as aplicações cadastradas (a relação de confiança), por id.
 */
@ConfigurationProperties(prefix = "sso.idp")
public record IdpProperties(String emissor, Map<String, Aplicacao> aplicacoes) {

    /**
     * Uma aplicação cadastrada no provedor.
     *
     * @param urlRetorno   para onde o navegador volta com o código (único endereço aceito).
     * @param urlLogout    endereço interno que recebe o aviso de logout único (servidor a servidor).
     * @param urlPosLogout para onde o usuário pode voltar depois de sair.
     * @param segredo      o segredo que a aplicação apresenta ao trocar o código.
     */
    public record Aplicacao(String nome, String urlRetorno, String urlLogout, String urlPosLogout, String segredo) {
    }

    /** Devolve a aplicação cadastrada com este id, ou null se for desconhecida. */
    public Aplicacao aplicacao(String id) {
        return id == null ? null : aplicacoes.get(id);
    }
}
