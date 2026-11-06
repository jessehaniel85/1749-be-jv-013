package com.ada.sso.idp;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/** As telas do provedor: login, conta (quem está logado), confirmação de saída e "você saiu". */
@Controller
@Profile("idp")
public class PaginasIdpController {

    private final IdpProperties props;

    public PaginasIdpController(IdpProperties props) {
        this.props = props;
    }

    // A tela onde a SENHA é digitada. Só existe aqui, no provedor.
    @GetMapping("/sso/login")
    public String login() {
        return "login";
    }

    // Quem visita o provedor direto (sem vir de uma aplicação) vê a sessão dele.
    @GetMapping("/")
    @SuppressWarnings("unchecked")
    public String conta(@AuthenticationPrincipal UsuarioIdp usuario, HttpSession sessao, Model model) {
        model.addAttribute("usuario", usuario);
        Set<String> apps = (Set<String>) sessao.getAttribute(AutorizarController.APPS_CONECTADAS);
        model.addAttribute("apps", apps == null ? Set.of() : apps);
        return "conta";
    }

    // Confirmação do logout. Sair é uma AÇÃO que muda estado, então o botão faz um POST com
    // token CSRF: se bastasse abrir uma URL, qualquer site deslogaria o usuário com um <img src=...>.
    @GetMapping("/sso/sair")
    public String confirmarSaida(@RequestParam(required = false) String app, Model model) {
        model.addAttribute("app", props.aplicacao(app) == null ? "" : app);
        return "sair";
    }

    @GetMapping("/sso/saiu")
    public String saiu(@RequestParam(required = false) String app, Model model) {
        IdpProperties.Aplicacao aplicacao = props.aplicacao(app);
        model.addAttribute("aplicacao", aplicacao);
        return "saiu";
    }
}
