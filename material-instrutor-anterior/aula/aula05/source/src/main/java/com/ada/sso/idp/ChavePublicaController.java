package com.ada.sso.idp;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Publica a chave PÚBLICA do provedor. As aplicações a buscam aqui para conferir a assinatura das
 * asserções. A chave pública não é segredo: pode ser aberta a qualquer um.
 *
 * (No OpenID Connect da aula 06 isto se chama "JWKS", e o endereço é padronizado.)
 *
 * ATENÇÃO À CONFIANÇA: quem controla este endereço controla em quem a aplicação confia. Em
 * produção ele é acessado por HTTPS (aula 02) para que ninguém no caminho troque a chave.
 */
@RestController
@Profile("idp")
public class ChavePublicaController {

    private final ChavesIdp chaves;

    public ChavePublicaController(ChavesIdp chaves) {
        this.chaves = chaves;
    }

    @GetMapping("/sso/chave-publica")
    public Map<String, String> chavePublica() {
        return Map.of(
                "algoritmo", "RS256",
                "formato", "X.509 em Base64",
                "chave", chaves.publicaEmBase64());
    }
}
