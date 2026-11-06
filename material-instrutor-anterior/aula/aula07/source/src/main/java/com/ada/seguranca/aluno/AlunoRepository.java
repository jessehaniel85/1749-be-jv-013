package com.ada.seguranca.aluno;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AlunoRepository {

    private static final RowMapper<Aluno> MAPPER = (rs, rowNum) ->
            new Aluno(rs.getLong("id"), rs.getString("nome"), rs.getString("email"));

    private final JdbcTemplate jdbc;

    public AlunoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Aluno> findAll() {
        return jdbc.query("SELECT * FROM alunos", MAPPER);
    }

    public Optional<Aluno> findById(Long id) {
        return jdbc.query("SELECT * FROM alunos WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    // VULNERÁVEL a SQL Injection: o texto recebido é concatenado na consulta.
    // Ataque: nome = x' OR '1'='1   (devolve todos os alunos)
    public List<Aluno> pesquisaVulneravel(String nome) {
        String sql = "SELECT * FROM alunos WHERE nome = '" + nome + "'";
        return jdbc.query(sql, MAPPER);
    }

    // CORRIGIDO: instrução preparada. O SQL e o dado seguem por caminhos separados.
    public List<Aluno> pesquisa(String nome) {
        return jdbc.query("SELECT * FROM alunos WHERE nome = ?", MAPPER, nome);
    }
}
