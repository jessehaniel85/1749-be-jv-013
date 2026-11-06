package com.ada.sso.cliente;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** A página inicial (comum às duas aplicações) e a página "sem permissão". */
@Controller
@Profile({"notas", "biblioteca"})
public class PaginasController {

    private final ClienteProperties props;

    public PaginasController(ClienteProperties props) {
        this.props = props;
    }

    // Só chega aqui quem tem sessão local. Quem não tem foi mandado ao provedor (IniciaLoginSso).
    @GetMapping("/")
    public String inicio(Authentication autenticacao, Model model) {
        model.addAttribute("app", props);
        // O que esta aplicação sabe do usuário veio TODO da asserção do provedor.
        model.addAttribute("usuario", (UsuarioSso) autenticacao.getPrincipal());
        return "inicio";
    }

    @GetMapping("/negado")
    public String negado(Model model) {
        model.addAttribute("app", props);
        return "negado";
    }
}
