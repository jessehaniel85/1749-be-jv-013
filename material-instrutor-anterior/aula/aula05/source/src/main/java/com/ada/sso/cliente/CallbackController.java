package com.ada.sso.cliente;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

/**
 * PASSO 6 do fluxo: o navegador volta do provedor para cá com ?code=...&state=...
 *
 * Aqui a aplicação NÃO confia em nada que veio na URL até conferir:
 *   1. o "state" é um dos que EU gerei? (senão, alguém tentou me enganar)
 *   2. troca o código pela asserção, direto com o provedor (servidor a servidor, com o segredo)
 *   3. verifica a assinatura e os claims da asserção
 *   4. só então cria a SESSÃO LOCAL: a aplicação passa a lembrar do usuário pelo cookie dela
 *
 * Depois disso o provedor sai de cena: nas próximas requisições vale a sessão local.
 */
@Controller
@Profile({"notas", "biblioteca"})
public class CallbackController {

    private static final Logger log = LoggerFactory.getLogger(CallbackController.class);

    private final ClienteProperties props;
    private final IdpClient idp;
    private final VerificadorDeAsercao verificador;
    private final RegistroDeSessoes registro;

    private final SecurityContextRepository repositorioDeContexto = new HttpSessionSecurityContextRepository();
    private final RequestCache cacheDeRequisicao = new HttpSessionRequestCache();

    public CallbackController(ClienteProperties props, IdpClient idp,
                              VerificadorDeAsercao verificador, RegistroDeSessoes registro) {
        this.props = props;
        this.idp = idp;
        this.verificador = verificador;
        this.registro = registro;
    }

    @GetMapping("/sso/callback")
    @SuppressWarnings("unchecked")
    public String callback(@RequestParam String code, @RequestParam String state,
                           HttpServletRequest request, HttpServletResponse response) {

        // 1) O state confere? remove() garante que cada state serve uma vez só.
        HttpSession sessao = request.getSession(false);
        Set<String> pendentes = sessao == null ? null : (Set<String>) sessao.getAttribute(IniciaLoginSso.ESTADOS_PENDENTES);
        if (pendentes == null || !pendentes.remove(state)) {
            log.warn("[{}] retorno com state desconhecido: recusado", props.id());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State inválido");
        }
        sessao.setAttribute(IniciaLoginSso.ESTADOS_PENDENTES, pendentes);

        // 2 e 3) Troca o código pela asserção e confere a assinatura e os claims.
        UsuarioSso usuario;
        try {
            usuario = verificador.verificarAsercao(idp.trocarCodigo(code));
        } catch (SsoException e) {
            log.warn("[{}] login único recusado: {}", props.id(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha no login único");
        }

        // 4) Cria a sessão local.
        // changeSessionId(): troca o ID do cookie de sessão logo ANTES de autenticar, para que
        // ninguém que tenha plantado um ID conhecido (session fixation) herde a sessão logada.
        request.changeSessionId();
        var autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.papel())));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        repositorioDeContexto.saveContext(contexto, request, response);   // grava na sessão
        registro.registrar(usuario.sub(), request.getSession());          // índice para o logout único
        log.info("[{}] login único concluído: '{}' ({}) tem sessão local", props.id(), usuario.sub(), usuario.papel());

        // Volta para a página que o usuário queria no início (ou para a inicial).
        SavedRequest salva = cacheDeRequisicao.getRequest(request, response);
        cacheDeRequisicao.removeRequest(request, response);
        return "redirect:" + (salva != null ? salva.getRedirectUrl() : "/");
    }
}
