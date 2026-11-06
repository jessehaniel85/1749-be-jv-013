package com.ada.seguranca.aluno;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    // Rate limiter da aula 01: a exceção RequestNotPermitted agora vira 429 (GlobalExceptionHandler).
    @GetMapping
    @RateLimiter(name = "alunoService")
    @ResponseStatus(HttpStatus.OK)
    public List<Aluno> consultaTodos() {
        return service.buscaTodos();
    }

    @GetMapping("/{id}")
    public Aluno buscaPorId(@PathVariable Long id) {
        return service.buscaPorId(id);
    }

    // POST existe para mostrar a proteção CSRF do Spring Security (passo 5 da autenticação básica).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Aluno cadastra(@RequestBody AlunoRequest request) {
        return service.cadastra(request.nome(), request.email());
    }

    public record AlunoRequest(String nome, String email) {
    }

    // SQL Injection: este endpoint concatena o parâmetro na consulta. Só para a demonstração.
    @GetMapping("/pesquisa/vulneravel")
    public List<Aluno> pesquisaVulneravel(@RequestParam String nome) {
        return service.pesquisaVulneravel(nome);
    }

    // Versão corrigida, com instrução preparada.
    @GetMapping("/pesquisa")
    public List<Aluno> pesquisa(@RequestParam String nome) {
        return service.pesquisa(nome);
    }

    // PASSO "tratamento no controller" (seção 06 do roteiro): descomente para tratar só aqui.
    // O tratamento global (GlobalExceptionHandler) faz o mesmo para todos os controllers.
    //
    // @ExceptionHandler(AlunoNaoEncontradoException.class)
    // public ResponseEntity<String> naoEncontrado(AlunoNaoEncontradoException ex) {
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    // }
}
