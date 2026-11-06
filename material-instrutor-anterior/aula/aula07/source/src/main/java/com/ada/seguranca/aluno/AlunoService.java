package com.ada.seguranca.aluno;

import com.ada.seguranca.exception.AlunoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository repository;

    public AlunoService(AlunoRepository repository) {
        this.repository = repository;
    }

    public List<Aluno> buscaTodos() {
        return repository.findAll();
    }

    // Passo da aula: comece com repository.findById(id).get() (NoSuchElementException, vira 500)
    // e depois troque por orElseThrow com a exceção própria.
    public Aluno buscaPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException(id));
    }

    public List<Aluno> pesquisaVulneravel(String nome) {
        return repository.pesquisaVulneravel(nome);
    }

    public List<Aluno> pesquisa(String nome) {
        return repository.pesquisa(nome);
    }
}
