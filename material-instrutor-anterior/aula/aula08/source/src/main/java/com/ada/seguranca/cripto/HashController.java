package com.ada.seguranca.cripto; // pacote onde ficam as classes de criptografia desta aula

import org.springframework.http.HttpStatus;                     // códigos de status HTTP
import org.springframework.web.bind.annotation.PostMapping;     // mapeia uma rota POST
import org.springframework.web.bind.annotation.RequestBody;     // lê o JSON do corpo da requisição
import org.springframework.web.bind.annotation.RequestMapping;  // prefixo de rota do controller
import org.springframework.web.bind.annotation.RestController;  // controller que devolve JSON
import org.springframework.web.server.ResponseStatusException;  // devolve um erro HTTP com status

/**
 * Recebe um texto e devolve o hash dele. Só para a demonstração da aula 02.
 */
@RestController            // classe que atende requisições HTTP e responde em JSON
@RequestMapping("/cripto") // todas as rotas desta classe começam com /cripto
public class HashController {

    // corpo da requisição: {"texto": "..."}
    public record TextoRequest(String texto) {
    }

    // corpo da resposta: {"algoritmo": "SHA-256", "hash": "..."}
    public record HashResponse(String algoritmo, String hash) {
    }

    // quem calcula o hash; o Spring entrega a instância pelo construtor (injeção de dependência)
    private final FuncaoHash funcaoHash;

    public HashController(FuncaoHash funcaoHash) {
        this.funcaoHash = funcaoHash;
    }

    // POST /cripto/hash  com  {"texto": "Olá, Ada!"}
    // POST e não GET: o texto vai no corpo, e não na URL (URLs ficam em logs e no histórico)
    @PostMapping("/hash")
    public HashResponse hash(@RequestBody TextoRequest request) {
        // sem texto não há o que calcular: responde 400 (requisição inválida)
        if (request == null || request.texto() == null || request.texto().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o texto");
        }
        // devolve o nome do algoritmo junto, para quem consome saber como conferir o hash
        return new HashResponse(FuncaoHash.ALGORITMO, funcaoHash.sha256(request.texto()));
    }
}
