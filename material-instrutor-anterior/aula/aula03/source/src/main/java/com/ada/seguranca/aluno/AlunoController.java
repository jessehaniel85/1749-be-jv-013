package com.ada.seguranca.aluno;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final List<Aluno> alunos = List.of(
            new Aluno(1L, "Batman", "batman@ada.com"),
            new Aluno(2L, "Superman", "superman@ada.com"),
            new Aluno(3L, "Mulher Maravilha", "diana@ada.com"));

    // Demonstração de DoS: o rate limiter "alunoService" (application.yml) aceita
    // 1 chamada a cada 10 segundos. A segunda chamada lança RequestNotPermitted.
    @GetMapping
    @RateLimiter(name = "alunoService")
    @ResponseStatus(HttpStatus.OK)
    public List<Aluno> consultaTodos() {
        return alunos;
    }
}
