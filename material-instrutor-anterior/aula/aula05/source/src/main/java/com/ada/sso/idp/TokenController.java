package com.ada.sso.idp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * PASSO 6 (parte do servidor): a aplicação troca o código pela asserção.
 *
 * Esta chamada NÃO passa pelo navegador. É a aplicação (servidor) falando direto com o provedor:
 *
 *   POST /sso/token
 *   Authorization: Basic base64(idDaAplicacao:segredo)     <- a aplicação prova quem é
 *   code=<o código recebido na URL>
 *
 * Quatro conferências: a aplicação é cadastrada, o segredo confere, o código existe e não foi
 * usado, e o código foi emitido para ESTA aplicação. Qualquer falha devolve erro SEM detalhes.
 */
@RestController
@Profile("idp")
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final IdpProperties props;
    private final CodigoService codigos;
    private final AsercaoService asercoes;

    public TokenController(IdpProperties props, CodigoService codigos, AsercaoService asercoes) {
        this.props = props;
        this.codigos = codigos;
        this.asercoes = asercoes;
    }

    @PostMapping(path = "/sso/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> trocarCodigo(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacao,
            @RequestParam(required = false) String code) {

        // 1) Quem está chamando? (HTTP Basic: id da aplicação e segredo)
        String appId = autenticarAplicacao(autorizacao);
        if (appId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Basic")
                    .body(Map.of("erro", "cliente_invalido"));
        }

        // 2) O código é válido para esta aplicação? (resgatar() também o consome: uso único)
        Optional<Codigo> codigo = codigos.resgatar(code, appId);
        if (codigo.isEmpty()) {
            log.warn("[IdP] troca de código recusada para a aplicação '{}'", appId);
            return ResponseEntity.badRequest().body(Map.of("erro", "codigo_invalido"));
        }

        // 3) Entrega a asserção assinada. no-store: nenhum cache pode guardar esta resposta.
        log.info("[IdP] asserção entregue à aplicação '{}' para o usuário '{}'", appId, codigo.get().username());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "assercao", asercoes.gerarAsercao(codigo.get()),
                        "expiresIn", asercoes.validadeDaAsercaoEmSegundos()));
    }

    /** Devolve o id da aplicação se o cabeçalho Basic traz um id cadastrado e o segredo certo; senão, null. */
    private String autenticarAplicacao(String autorizacao) {
        if (autorizacao == null || !autorizacao.startsWith("Basic ")) {
            return null;
        }
        String decodificado;
        try {
            decodificado = new String(Base64.getDecoder().decode(autorizacao.substring(6)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        int separador = decodificado.indexOf(':');
        if (separador < 0) {
            return null;
        }
        String id = decodificado.substring(0, separador);
        String segredo = decodificado.substring(separador + 1);

        IdpProperties.Aplicacao aplicacao = props.aplicacao(id);
        // MessageDigest.isEqual compara em TEMPO CONSTANTE (não vaza, pelo tempo de resposta,
        // quantos caracteres do segredo estavam certos). Um equals() comum vazaria.
        if (aplicacao == null || !MessageDigest.isEqual(
                aplicacao.segredo().getBytes(StandardCharsets.UTF_8),
                segredo.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        return id;
    }
}
