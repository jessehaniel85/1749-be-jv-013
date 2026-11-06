package com.ada.seguranca.jwt;

// ObjectMapper é o "tradutor" Java <-> JSON do Spring (biblioteca Jackson).
// TypeReference nos deixa dizer ao Jackson: "leia este JSON como um Map<String, Object>".
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// @Value lê um valor do application.yml (ou de variável de ambiente) e injeta em um parâmetro.
import org.springframework.beans.factory.annotation.Value;
// @Service marca a classe como um "bean" do Spring: ele cria uma instância e entrega a quem pedir.
import org.springframework.stereotype.Service;

// Mac = o objeto do Java que calcula HMAC (assinatura com chave secreta).
import javax.crypto.Mac;
// SecretKeySpec = embrulha os bytes da nossa chave no formato que o Mac exige.
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
// MessageDigest.isEqual compara dois arrays de bytes em tempo constante (explicado mais abaixo).
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
// LinkedHashMap mantém a ORDEM de inserção; assim o JSON sai com "alg" antes de "typ", etc.
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Um JWT (HS256) construído À MÃO, para a turma ver que não há mágica:
 *
 *     JWT = base64url(header) + "." + base64url(payload) + "." + base64url(assinatura)
 *     assinatura = HMAC-SHA256( base64url(header) + "." + base64url(payload), chave )
 *
 * Na aula 08 a geração e a validação passam a usar uma biblioteca (java-jwt); aqui o
 * objetivo é entender o que ela faz por baixo.
 *
 * ROTEIRO PARA IMPLEMENTAR COM A TURMA (ordem sugerida):
 *   1. Esqueleto da classe, o construtor e a chave                      (Bloco 2)
 *   2. gerar(): header, payload e assinatura                            (Bloco 2 e 3)
 *   3. decodificar(): ler o token SEM chave, para provar que não é segredo (Bloco 3)
 *   4. validar(): a ordem das verificações é o ponto mais importante    (Bloco 4)
 *   5. Os métodos de apoio (base64Url, hmac, JSON) podem ser colados prontos: não são o foco.
 *
 * Perguntas para a turma ao longo do código estão marcadas com "PERGUNTE".
 */
// @Service: o Spring cria UMA instância desta classe e a entrega a quem a pedir no construtor
// (o JwtController, o JwtBiblioteca e o DesafioController).
@Service
public class JwtManual {

    // O "iss" (issuer = emissor) que vai dentro de todo token nosso. Na validação conferimos
    // que o token foi emitido por NÓS, e não por outro sistema que usa a mesma chave por acaso.
    public static final String EMISSOR = "ada-seguranca";

    // O único algoritmo que aceitamos. HS256 = HMAC com SHA-256 (chave SIMÉTRICA: a mesma
    // chave assina e confere). Guardamos aqui para gerar e validar usarem o MESMO valor.
    private static final String ALGORITMO = "HS256";

    /**
     * Resultado de uma validação: o motivo explica por que o token foi aceito ou recusado.
     * "record" é uma classe imutável com construtor e getters prontos: Resultado.valido(),
     * Resultado.motivo() e Resultado.payload().
     */
    public record Resultado(boolean valido, String motivo, Map<String, Object> payload) {
    }

    // Constante que "descreve" o tipo Map<String, Object> para o Jackson. O "new ... {}" com
    // chaves vazias é um truque do Java para o tipo genérico não se perder em tempo de execução.
    private static final TypeReference<Map<String, Object>> MAPA = new TypeReference<>() {
    };

    // O tradutor de JSON (injetado pelo Spring).
    private final ObjectMapper json;

    // A chave secreta, em BYTES (é isso que o HMAC consome).
    private final byte[] chavePadrao;

    // A chave vem da configuração (application.yml / variável de ambiente), não do código.
    // PERGUNTE: "por que NÃO escrevemos a chave direto aqui, entre aspas?"
    //   Resposta: o código vai para o Git; quem lê o repositório leria a chave e forjaria tokens.
    // ${app.demo.jwt-secret} é o caminho da propriedade no application.yml.
    public JwtManual(ObjectMapper json, @Value("${app.demo.jwt-secret}") String segredo) {
        this.json = json;
        // String -> bytes, sempre com a codificação explícita (UTF-8) para não depender do
        // computador onde roda.
        this.chavePadrao = segredo.getBytes(StandardCharsets.UTF_8);
    }

    // Devolve a chave para o JwtBiblioteca usar a MESMA chave. É por isso que um token gerado
    // por uma classe é aceito pela outra.
    public byte[] chavePadrao() {
        return chavePadrao;
    }

    // ---------------------------------------------------------------- gerar

    // Versão curta: usa a chave padrão da aplicação. É a que o login chama.
    public String gerar(String usuario, String perfil, long segundosDeValidade) {
        return gerar(chavePadrao, usuario, perfil, segundosDeValidade);
    }

    // Versão completa: recebe a chave como parâmetro. Existe para o DesafioController conseguir
    // gerar um token com OUTRA chave (a fraca) sem mexer na chave de verdade.
    public String gerar(byte[] chave, String usuario, String perfil, long segundosDeValidade) {
        // ---- PARTE 1 DO TOKEN: o HEADER -------------------------------------------------
        // Diz COMO o token foi assinado. Aqui: algoritmo HS256 e tipo JWT.
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", ALGORITMO);
        header.put("typ", "JWT");

        // ---- PARTE 2 DO TOKEN: o PAYLOAD ------------------------------------------------
        // As "claims" (afirmações): quem é o usuário, quem emitiu, quando e até quando vale.
        // Nomes curtos e padronizados (RFC 7519): iss, sub, iat, exp. "role" é nossa (customizada).

        // Epoch seconds = segundos desde 01/01/1970 (UTC). É o formato de data do JWT.
        long agora = Instant.now().getEpochSecond();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", EMISSOR);              // issuer: quem emitiu
        payload.put("sub", usuario);              // subject: de quem é o token
        payload.put("role", perfil);              // o perfil (USER, ADMIN): é isto que o /admin confere
        payload.put("iat", agora);                // issued at: quando foi emitido
        payload.put("exp", agora + segundosDeValidade);   // expiration: até quando vale
        // PERGUNTE: "o que impede alguém de trocar USER por ADMIN aí dentro?"
        //   Resposta: nada impede de ESCREVER; o que impede de FUNCIONAR é a assinatura (parte 3).
        // ATENÇÃO: o payload NÃO é secreto. Nunca coloque senha, CPF, cartão etc. aqui.

        // ---- PARTE 3 DO TOKEN: a ASSINATURA ---------------------------------------------
        // Primeiro monta "header.payload", cada um convertido para JSON e depois para Base64URL.
        // É exatamente este texto que será assinado.
        String cabecalhoEPayload = base64Url(paraJson(header)) + "." + base64Url(paraJson(payload));
        // Depois calcula o HMAC desse texto com a chave e junta tudo: header.payload.assinatura
        return cabecalhoEPayload + "." + base64Url(hmac(chave, cabecalhoEPayload));
        // IDEIA CENTRAL: se qualquer letra do header ou do payload mudar, o HMAC recalculado
        // muda por completo. Sem a chave, ninguém consegue produzir a assinatura certa.
    }

    // ---------------------------------------------------------- decodificar

    /**
     * Lê o header e o payload SEM conferir a assinatura: qualquer pessoa com o token consegue.
     * É o que o jwt.io faz na tela. Serve para provar que Base64 NÃO é criptografia.
     */
    public Map<String, Map<String, Object>> decodificar(String token) {
        // split("\\.") divide nos pontos. O -1 faz o Java manter partes vazias no fim
        // (ex.: um token "alg none" termina em ponto e tem a 3ª parte vazia).
        String[] partes = token.split("\\.", -1);
        // Um JWT sempre tem exatamente 3 partes: header, payload, assinatura.
        if (partes.length != 3) {
            throw new IllegalArgumentException("Um JWT tem 3 partes separadas por ponto");
        }
        Map<String, Map<String, Object>> resultado = new LinkedHashMap<>();
        // partes[0] = header, partes[1] = payload. Só decodificamos; não usamos chave nenhuma.
        resultado.put("header", doJson(partes[0]));
        resultado.put("payload", doJson(partes[1]));
        // partes[2] (a assinatura) é ignorada de propósito: aqui não validamos nada.
        return resultado;
    }

    // ------------------------------------------------------------- validar

    // Versão curta: valida com a chave padrão. É a que o /jwt/protegido usa.
    public Resultado validar(String token) {
        return validar(chavePadrao, token);
    }

    /**
     * Valida o token. A ORDEM das verificações importa:
     *   1) formato  ->  2) algoritmo  ->  3) assinatura  ->  4) emissor  ->  5) expiração
     * Só olhamos o conteúdo (claims) DEPOIS de provar que a assinatura é autêntica: antes
     * disso o conteúdo pode ter sido escrito por qualquer um.
     */
    public Resultado validar(byte[] chave, String token) {
        try {
            // ---- 1) FORMATO ---------------------------------------------------------
            String[] partes = token.split("\\.", -1);
            if (partes.length != 3) {
                return recusado("formato inválido: esperado header.payload.assinatura");
            }

            // ---- 2) ALGORITMO -------------------------------------------------------
            // O servidor usa o que ELE espera, nunca o que o token diz.
            // Tokens com "alg": "none" (sem assinatura) são recusados aqui.
            // PERGUNTE: "e se confiássemos no alg do token? o que um atacante escreveria?"
            //   Resposta: "none". O token diria "não precisa de assinatura" e entraria sem chave.
            Map<String, Object> header = doJson(partes[0]);
            if (!ALGORITMO.equals(header.get("alg"))) {
                return recusado("algoritmo não permitido: " + header.get("alg"));
            }

            // ---- 3) ASSINATURA ------------------------------------------------------
            // Recalcula o HMAC de "header.payload" com a NOSSA chave e compara com o que veio.
            // Usamos as partes ORIGINAIS (partes[0] e partes[1]) como chegaram, sem
            // decodificar e recodificar, porque a assinatura foi calculada sobre esse texto.
            byte[] esperada = hmac(chave, partes[0] + "." + partes[1]);
            byte[] recebida = Base64.getUrlDecoder().decode(partes[2]);
            // MessageDigest.isEqual compara em tempo constante: um "equals" comum para no
            // primeiro byte diferente, e a diferença de tempo poderia vazar pistas da assinatura.
            // PERGUNTE: "o atacante muda USER para ADMIN no payload. O que acontece aqui?"
            //   Resposta: o HMAC recalculado sobre o payload novo é diferente do que veio -> recusado.
            if (!MessageDigest.isEqual(esperada, recebida)) {
                return recusado("assinatura inválida: o token foi alterado ou assinado com outra chave");
            }

            // ---- 4) EMISSOR ---------------------------------------------------------
            // Só agora é seguro confiar no payload: a assinatura provou que fomos nós que o criamos.
            Map<String, Object> payload = doJson(partes[1]);
            if (!EMISSOR.equals(payload.get("iss"))) {
                return recusado("emissor inválido");
            }

            // ---- 5) EXPIRAÇÃO -------------------------------------------------------
            // "exp" vem do JSON como número. Se faltar ou não for número, recusamos (fail-secure:
            // na dúvida, nega). Se o instante atual já alcançou o exp, o token venceu.
            Object exp = payload.get("exp");
            if (!(exp instanceof Number) || Instant.now().getEpochSecond() >= ((Number) exp).longValue()) {
                return recusado("token expirado");
            }

            // Passou por tudo: devolvemos o payload para o chamador saber QUEM é e o PERFIL.
            return new Resultado(true, "token válido", payload);
        } catch (IllegalArgumentException e) {
            // Base64 ou JSON malformado (ex.: alguém colou lixo no lugar do token).
            // Em vez de estourar um erro 500, tratamos como "token recusado".
            return recusado("token malformado");
        }
    }

    // Atalho para montar um Resultado negativo (sem payload).
    private Resultado recusado(String motivo) {
        return new Resultado(false, motivo, null);
    }

    // -------------------------------------------------------------- apoio
    // Métodos auxiliares: podem ser entregues prontos à turma; o foco é gerar() e validar().

    // Texto -> bytes UTF-8 -> Base64URL.
    private String base64Url(String texto) {
        return base64Url(texto.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] bytes) {
        // Base64URL sem preenchimento ("="): é o formato do JWT
        // (Base64URL troca "+" e "/" por "-" e "_", para o token caber em URLs e cabeçalhos).
        // PERGUNTE: "Base64 protege o conteúdo?" Resposta: não, é só uma forma de escrever bytes
        // como texto. Qualquer pessoa decodifica (rode: echo '<parte>' | base64 -d).
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Calcula o HMAC-SHA256 de um texto com a chave dada.
    private byte[] hmac(byte[] chave, String texto) {
        try {
            // Pede ao Java a implementação do HMAC com SHA-256.
            Mac mac = Mac.getInstance("HmacSHA256");
            // Informa a chave. O segundo argumento é o nome do algoritmo ao qual a chave se destina.
            mac.init(new SecretKeySpec(chave, "HmacSHA256"));
            // Calcula e devolve os 32 bytes da assinatura.
            return mac.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            // Todo JDK tem HmacSHA256; se faltar, o ambiente está quebrado (erro de programação).
            throw new IllegalStateException("HMAC-SHA256 indisponível", e);
        }
    }

    // Map -> texto JSON (ex.: {"alg":"HS256","typ":"JWT"}).
    private String paraJson(Map<String, Object> mapa) {
        try {
            return json.writeValueAsString(mapa);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    // Base64URL -> bytes -> JSON -> Map. É o caminho inverso de base64Url(paraJson(...)).
    // Se a parte não for Base64 válido, o decoder lança IllegalArgumentException;
    // se não for JSON válido, convertemos o erro para o mesmo tipo.
    // Os dois casos são capturados em validar() e viram "token malformado".
    private Map<String, Object> doJson(String parteBase64Url) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(parteBase64Url);
            return json.readValue(bytes, MAPA);
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("JSON inválido", e);
        }
    }
}
