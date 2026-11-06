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
 * As telas da aplicação BIBLIOTECA (dados de mentira). Mesma lição das Notas: o provedor diz QUEM
 * é o usuário; esta aplicação decide o que cada papel pode fazer AQUI. O "carlos" é PROFESSOR e
 * pode lançar notas lá, mas não gerencia o acervo; a "marta" é o contrário.
 */
@Controller
@Profile("biblioteca")
public class BibliotecaController {

    private static final Map<String, List<List<String>>> EMPRESTIMOS = Map.of(
            "ana", List.of(List.of("Dom Casmurro", "devolver até 20/11"), List.of("O Alienista", "devolver até 25/11")),
            "carlos", List.of(List.of("Pedagogia da Autonomia", "devolver até 30/11")));

    private final ClienteProperties props;

    public BibliotecaController(ClienteProperties props) {
        this.props = props;
    }

    @GetMapping("/meus-emprestimos")
    public String meusEmprestimos(Authentication autenticacao, Model model) {
        UsuarioSso usuario = (UsuarioSso) autenticacao.getPrincipal();
        model.addAttribute("app", props);
        model.addAttribute("titulo", "Meus empréstimos");
        model.addAttribute("colunas", List.of("Livro", "Prazo"));
        model.addAttribute("linhas", EMPRESTIMOS.getOrDefault(usuario.sub(), List.of()));
        model.addAttribute("vazio", "Você não tem livros emprestados.");
        return "lista";
    }

    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/acervo/gerenciar")
    public String gerenciarAcervo(Model model) {
        model.addAttribute("app", props);
        model.addAttribute("titulo", "Gerenciar acervo");
        model.addAttribute("colunas", List.of("Livro", "Exemplares", "Situação"));
        model.addAttribute("linhas", List.of(
                List.of("Dom Casmurro", "3", "1 emprestado"),
                List.of("O Alienista", "2", "1 emprestado"),
                List.of("Pedagogia da Autonomia", "1", "1 emprestado")));
        model.addAttribute("vazio", "");
        return "lista";
    }
}
