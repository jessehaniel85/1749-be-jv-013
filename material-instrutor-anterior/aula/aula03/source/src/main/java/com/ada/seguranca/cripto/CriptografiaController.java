package com.ada.seguranca.cripto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoints só para a demonstração da aula 02. Em um sistema real, ninguém expõe
 * "cifrar" e "decifrar" para o mundo: a criptografia acontece dentro da aplicação.
 */
@RestController
@RequestMapping("/cripto")
public class CriptografiaController {

    public record TextoRequest(String texto) {
    }

    public record CifradoRequest(String cifrado) {
    }

    public record VerificacaoRequest(String texto, String assinatura) {
    }

    private final CriptografiaSimetrica simetrica;
    private final CriptografiaAssimetrica assimetrica;

    public CriptografiaController(CriptografiaSimetrica simetrica, CriptografiaAssimetrica assimetrica) {
        this.simetrica = simetrica;
        this.assimetrica = assimetrica;
    }

    // ---- simétrica: a MESMA chave cifra e decifra ----

    @PostMapping("/simetrica/cifrar")
    public CifradoRequest cifrarSimetrica(@RequestBody TextoRequest request) {
        return new CifradoRequest(simetrica.cifrar(texto(request)));
    }

    @PostMapping("/simetrica/decifrar")
    public TextoRequest decifrarSimetrica(@RequestBody CifradoRequest request) {
        return new TextoRequest(executar(() -> simetrica.decifrar(cifrado(request))));
    }

    // ---- assimétrica: a pública cifra, a privada decifra ----

    @GetMapping("/assimetrica/chave-publica")
    public ChavePublica chavePublica() {
        return new ChavePublica("RSA-2048", "X.509", assimetrica.chavePublicaBase64());
    }

    public record ChavePublica(String algoritmo, String formato, String chavePublica) {
    }

    @PostMapping("/assimetrica/cifrar")
    public CifradoRequest cifrarAssimetrica(@RequestBody TextoRequest request) {
        return new CifradoRequest(executar(() -> assimetrica.cifrar(texto(request))));
    }

    @PostMapping("/assimetrica/decifrar")
    public TextoRequest decifrarAssimetrica(@RequestBody CifradoRequest request) {
        return new TextoRequest(executar(() -> assimetrica.decifrar(cifrado(request))));
    }

    // ---- assinatura digital: a privada assina, a pública verifica ----

    @PostMapping("/assimetrica/assinar")
    public Assinatura assinar(@RequestBody TextoRequest request) {
        return new Assinatura(assimetrica.assinar(texto(request)));
    }

    public record Assinatura(String assinatura) {
    }

    @PostMapping("/assimetrica/verificar")
    public Verificacao verificar(@RequestBody VerificacaoRequest request) {
        if (request == null || request.texto() == null || request.assinatura() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe texto e assinatura");
        }
        return new Verificacao(assimetrica.verificar(request.texto(), request.assinatura()));
    }

    public record Verificacao(boolean valida) {
    }

    private String texto(TextoRequest request) {
        if (request == null || request.texto() == null || request.texto().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o texto");
        }
        return request.texto();
    }

    private String cifrado(CifradoRequest request) {
        if (request == null || request.cifrado() == null || request.cifrado().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o texto cifrado");
        }
        return request.cifrado();
    }

    // Traduz as falhas esperadas em 400, sem expor detalhes internos.
    private <T> T executar(java.util.function.Supplier<T> operacao) {
        try {
            return operacao.get();
        } catch (TextoCifradoInvalidoException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
