package com.ada.sso.cliente;

import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * As telas da aplicação NOTAS. Os dados são de mentira e ficam aqui no código: o foco é o login.
 *
 * Repare na divisão de responsabilidades:
 *   - QUEM é o usuário                -> vem do provedor (login único)
 *   - O QUE ele pode fazer nas Notas  -> decidido AQUI (cada aplicação é autônoma)
 */
@Controller
@Profile("notas")
public class NotasController {

    private static final Map<String, List<List<String>>> NOTAS = Map.of(
            "ana", List.of(List.of("Matemática", "8,5"), List.of("História", "9,0"), List.of("Física", "7,5")));

    private final ClienteProperties props;

    public NotasController(ClienteProperties props) {
        this.props = props;
    }

    // Qualquer usuário logado vê as PRÓPRIAS notas. O dono vem da sessão (do token), nunca de um
    // parâmetro da URL: assim ninguém abre as notas de outra pessoa trocando um número (aula 01).
    @GetMapping("/minhas-notas")
    public String minhasNotas(Authentication autenticacao, Model model) {
        UsuarioSso usuario = (UsuarioSso) autenticacao.getPrincipal();
        model.addAttribute("app", props);
        model.addAttribute("titulo", "Minhas notas");
        model.addAttribute("colunas", List.of("Disciplina", "Nota"));
        model.addAttribute("linhas", NOTAS.getOrDefault(usuario.sub(), List.of()));
        model.addAttribute("vazio", "Você não tem notas lançadas.");
        return "lista";
    }

    // Só o papel PROFESSOR. Quem é ALUNO ou BIBLIOTECARIO recebe a página "sem permissão".
    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping("/lancar-notas")
    public String lancarNotas(Model model) {
        model.addAttribute("app", props);
        model.addAttribute("titulo", "Lançar notas");
        model.addAttribute("colunas", List.of("Aluno", "Disciplina", "Situação"));
        model.addAttribute("linhas", List.of(
                List.of("ana", "Matemática", "lançada"),
                List.of("ana", "História", "lançada"),
                List.of("ana", "Física", "pendente")));
        model.addAttribute("vazio", "");
        return "lista";
    }
}
