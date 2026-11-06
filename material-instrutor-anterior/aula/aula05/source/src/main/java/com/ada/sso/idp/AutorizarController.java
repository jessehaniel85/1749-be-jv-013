package com.ada.sso.idp;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PASSOS 2 a 5 do fluxo de SSO: a aplicação redireciona o navegador para cá.
 *
 *   GET /sso/autorizar?app=notas&state=XYZ
 *
 * Esta rota exige que o usuário esteja autenticado NO PROVEDOR (veja IdpSecurityConfig):
 *
 *   - SEM sessão no provedor: o Spring Security manda o usuário para a tela de login e, depois
 *     de digitar a senha, traz de volta para cá (guarda a URL original). É o "primeiro acesso".
 *   - COM sessão no provedor: o método abaixo executa direto, sem pedir senha. É o "segundo
 *     acesso" que dá a sensação de login único.
 *
 * O método então emite um código de uso único e devolve o navegador à aplicação.
 */
@Controller
@Profile("idp")
public class AutorizarController {

    private static final Logger log = LoggerFactory.getLogger(AutorizarController.class);

    /** Nome do atributo da sessão do provedor com as aplicações que já receberam um código (para o logout único). */
    public static final String APPS_CONECTADAS = "apps-conectadas";

    private final IdpProperties props;
    private final CodigoService codigos;

    public AutorizarController(IdpProperties props, CodigoService codigos) {
        this.props = props;
        this.codigos = codigos;
    }

    @GetMapping("/sso/autorizar")
    public ResponseEntity<Void> autorizar(@RequestParam(required = false) String app,
                                          @RequestParam(required = false) String state,
                                          @AuthenticationPrincipal UsuarioIdp usuario,
                                          HttpSession sessao) {
        // 1) A aplicação é conhecida? Só as cadastradas (a relação de confiança) recebem códigos.
        IdpProperties.Aplicacao aplicacao = props.aplicacao(app);
        if (aplicacao == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aplicação desconhecida.");
        }
        // 2) O "state" é um valor aleatório que a APLICAÇÃO gerou. O provedor só o devolve intacto;
        //    é a aplicação que confere na volta (protege contra CSRF no login, aula 01 e 04).
        //    Aqui apenas limitamos o formato, para não refletirmos lixo na URL.
        if (state == null || !state.matches("[A-Za-z0-9_-]{8,128}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State inválido.");
        }

        // 3) Emite o código para ESTA aplicação e anota na sessão do provedor, para avisá-la no logout único.
        String codigo = codigos.emitir(usuario, app);
        registrarAplicacao(sessao, app);
        log.info("[IdP] usuário '{}' autenticado; código emitido para a aplicação '{}'", usuario.getUsername(), app);

        // 4) Devolve o navegador à aplicação. O endereço vem do CADASTRO, nunca da requisição:
        //    se o cliente pudesse escolher, um atacante mandaria o código para o site dele ("open redirect").
        URI destino = UriComponentsBuilder.fromUriString(aplicacao.urlRetorno())
                .queryParam("code", codigo)
                .queryParam("state", state)
                .build().toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(destino).build();
    }

    @SuppressWarnings("unchecked")
    private void registrarAplicacao(HttpSession sessao, String app) {
        Set<String> apps = (Set<String>) sessao.getAttribute(APPS_CONECTADAS);
        if (apps == null) {
            apps = new LinkedHashSet<>();
        }
        apps.add(app);
        sessao.setAttribute(APPS_CONECTADAS, apps);
    }
}
