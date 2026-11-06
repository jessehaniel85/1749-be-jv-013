package com.ada.seguranca.xss;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/**
 * Demonstração de Reflected XSS. Uso exclusivo em sala, na máquina local.
 */
@RestController
public class BuscaController {

    private static final String HTML = "text/html;charset=UTF-8";

    // VULNERÁVEL: o texto digitado volta na resposta sem nenhum tratamento.
    // Ataque: /busca?q=<script>alert('XSS')</script>
    @GetMapping(value = "/busca", produces = HTML)
    public String busca(@RequestParam String q) {
        return "<h1>Resultados para: " + q + "</h1>";
    }

    // CORRIGIDO: a saída é escapada (< vira &lt;), o navegador mostra o texto em vez de executá-lo.
    @GetMapping(value = "/busca/segura", produces = HTML)
    public String buscaSegura(@RequestParam String q) {
        return "<h1>Resultados para: " + HtmlUtils.htmlEscape(q) + "</h1>";
    }
}
