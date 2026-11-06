package com.ada.seguranca.jwt;

// HttpSession = a sessão do servidor (Tomcat). O Spring entrega a sessão do usuário atual
// como parâmetro do método; se ainda não existir, ele cria uma e manda o cookie JSESSIONID.
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Lançar ResponseStatusException devolve ao cliente o status HTTP escolhido (aqui, 401).
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Autenticação baseada em SESSÃO: o servidor GUARDA quem está logado (na memória) e entrega
 * ao navegador só o ID da sessão, em um cookie (JSESSIONID). Compare com o token (JwtController).
 *
 * ROTEIRO PARA IMPLEMENTAR COM A TURMA (Bloco 1):
 *   1. /sessao/login   -> confere a senha e GUARDA o usuário na sessão
 *   2. /sessao/perfil  -> lê o usuário DA SESSÃO (o cliente não manda quem ele é)
 *   3. /sessao/logout  -> o servidor "esquece" o usuário
 *
 * O QUE OBSERVAR NO NAVEGADOR (DevTools > Application > Cookies):
 *   - Aparece um cookie JSESSIONID com um valor longo, aleatório e SEM significado.
 *   - Os dados do usuário (ana) NÃO estão no cookie: estão na memória do servidor.
 *   - Reinicie a aplicação: o cookie continua no navegador, mas o servidor não conhece mais
 *     aquele ID -> /sessao/perfil volta a dar 401. A sessão "morreu" junto com o servidor.
 *
 * Diferença para o CookieController da aula 02: lá o cookie guardava o PRÓPRIO usuário
 * ("user-id=42") e qualquer um podia editá-lo. Aqui o cookie é só uma chave opaca; quem sabe
 * quem é o usuário é o servidor.
 */
// @RestController: cada método devolve dados (JSON), não uma página HTML.
@RestController
// Todos os caminhos desta classe começam com /sessao.
@RequestMapping("/sessao")
public class SessaoController {

    // Nome do atributo guardado na sessão. É como uma chave de um Map pertencente a cada sessão.
    private static final String ATRIBUTO = "usuario";

    // O "cadastro" de usuários (ana, bia, admin) para conferir login e senha.
    private final UsuariosDemo usuarios;

    public SessaoController(UsuariosDemo usuarios) {
        this.usuarios = usuarios;
    }

    // POST /sessao/login com {"username":"ana","password":"senha123"}
    // @RequestBody: o Spring lê o JSON do corpo e preenche o record LoginRequest.
    // HttpSession sessao: o Spring entrega a sessão deste cliente (criando uma se for o 1º acesso).
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request, HttpSession sessao) {
        // Confere usuário e senha. Se não conferir, 401 (não autenticado) com mensagem genérica,
        // igual para "usuário não existe" e "senha errada".
        UsuariosDemo.Usuario usuario = usuarios.autenticar(request.username(), request.password())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));
        // o servidor guarda o usuário NA SESSÃO (memória do servidor); o cliente recebe só o ID dela
        sessao.setAttribute(ATRIBUTO, usuario.username());
        // PERGUNTE: "se rodarmos 3 instâncias do servidor atrás de um balanceador, o que acontece
        //   quando o próximo pedido cair em OUTRA instância?"
        //   Resposta: ela não conhece essa sessão -> 401. É o problema que o token resolve
        //   (ou que exige sessão compartilhada, como um Redis).
        return Map.of("mensagem", "Sessão criada para " + usuario.username());
    }

    // GET /sessao/perfil: repare que NÃO enviamos nome nenhum. O servidor descobre quem somos
    // pelo cookie JSESSIONID que o navegador manda sozinho.
    @GetMapping("/perfil")
    public Map<String, String> perfil(HttpSession sessao) {
        // Procura na sessão o atributo gravado no login.
        Object usuario = sessao.getAttribute(ATRIBUTO);
        if (usuario == null) {
            // sessão nova (cookie ausente, apagado ou de uma sessão que o servidor não conhece mais)
            // Três situações caem aqui: nunca fez login, fez logout, ou o servidor reiniciou.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado.");
        }
        return Map.of("usuario", usuario.toString());
    }

    // POST /sessao/logout
    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession sessao) {
        sessao.invalidate();   // o servidor "esquece" o usuário
        // Vantagem da sessão: o logout é REAL. Apagado no servidor, o cookie roubado deixa de valer.
        // PERGUNTE: "com token isso é tão simples?" (Bloco 5: o token vale até expirar.)
        return Map.of("mensagem", "Sessão encerrada");
    }

    // O corpo esperado no login. "record" gera construtor e getters (request.username()).
    public record LoginRequest(String username, String password) {
    }
}
