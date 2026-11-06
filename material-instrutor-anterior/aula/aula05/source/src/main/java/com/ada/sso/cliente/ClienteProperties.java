package com.ada.sso.cliente;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * A configuração da aplicação (bloco "sso.cliente"): quem ela é e onde está o provedor em que confia.
 * Esta é a sua parte da RELAÇÃO DE CONFIANÇA (a outra parte é o cadastro da aplicação no provedor).
 *
 * @param id             identificador desta aplicação no provedor (vira o "aud" da asserção).
 * @param segredo        o segredo combinado com o provedor, para trocar o código.
 * @param idpUrlPublica  endereço do provedor para o NAVEGADOR; é também o "iss" esperado.
 * @param idpUrlInterna  endereço do provedor para chamadas servidor a servidor.
 * @param menu           links da página inicial (só visual).
 */
@ConfigurationProperties(prefix = "sso.cliente")
public record ClienteProperties(String id, String nome, String cor, String segredo,
                                String idpUrlPublica, String idpUrlInterna, List<Link> menu) {

    public record Link(String rotulo, String caminho) {
    }
}
