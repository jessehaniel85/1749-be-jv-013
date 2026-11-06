package com.ada.seguranca.jwt;

// Reaproveitamos o SHA-256 que construímos na aula 02.
import com.ada.seguranca.cripto.FuncaoHash;
// @Component: bean genérico do Spring (como @Service, mas sem o "significado" de regra de negócio).
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Usuários fixos, só para a demonstração (não há banco ainda; o banco entra na aula 07).
 * As senhas não ficam em texto puro: guardamos o hash SHA-256 delas (da aula 02).
 * Em um sistema real o certo é um hash feito para senhas, como o BCrypt (aula 08).
 *
 * ROTEIRO PARA IMPLEMENTAR COM A TURMA:
 *   Esta classe é só o "cadastro" falso de usuários que o login das duas formas (sessão e
 *   token) consulta. Pode ser entregue pronta; o foco da aula é o que acontece DEPOIS do login.
 *
 * Usuários disponíveis na demonstração:
 *   ana   / senha123  -> perfil USER
 *   bia   / senha456  -> perfil USER
 *   admin / admin789  -> perfil ADMIN
 */
// @Component: o Spring cria uma instância e a entrega ao SessaoController e ao JwtController.
@Component
public class UsuariosDemo {

    /**
     * Um usuário: nome, o HASH da senha (nunca a senha) e o perfil.
     * "record" = classe imutável com construtor e getters prontos.
     */
    public record Usuario(String username, String hashDaSenha, String perfil) {
    }

    // A função de hash da aula 02, injetada pelo Spring.
    private final FuncaoHash hash;

    // Nome do usuário -> dados do usuário. É o nosso "banco de dados" em memória.
    private final Map<String, Usuario> usuarios;

    public UsuariosDemo(FuncaoHash hash) {
        this.hash = hash;
        // Map.of cria um mapa imutável. Calculamos o hash UMA vez, na subida da aplicação:
        // a senha em texto puro nunca fica guardada, só o hash dela.
        this.usuarios = Map.of(
                "ana", new Usuario("ana", hash.sha256("senha123"), "USER"),
                "bia", new Usuario("bia", hash.sha256("senha456"), "USER"),
                "admin", new Usuario("admin", hash.sha256("admin789"), "ADMIN"));
        // PERGUNTE: "se alguém roubar este 'banco', consegue ler as senhas?"
        //   Resposta: não diretamente (é hash), mas SHA-256 é rápido demais: dá para testar bilhões
        //   de palpites por segundo. Por isso, em produção, BCrypt (lento de propósito), na aula 08.
    }

    /** Devolve o usuário se o nome existe e a senha confere; vazio nos dois casos de erro. */
    public Optional<Usuario> autenticar(String username, String senha) {
        // Proteção contra JSON sem os campos (null): sem isso, o Map/hash lançaria NullPointerException.
        if (username == null || senha == null) {
            return Optional.empty();
        }
        // 1) procura o usuário pelo nome (ofNullable: se não existir, fica vazio)
        // 2) filter: só mantém se o hash da senha digitada for igual ao hash guardado
        // Repare: a SENHA NUNCA é comparada em texto puro; comparamos hash com hash.
        // E o resultado é igual para "usuário não existe" e "senha errada": o chamador não
        // consegue descobrir qual dos dois falhou (evita revelar quais usuários existem).
        return Optional.ofNullable(usuarios.get(username))
                .filter(u -> u.hashDaSenha().equals(hash.sha256(senha)));
    }
}
