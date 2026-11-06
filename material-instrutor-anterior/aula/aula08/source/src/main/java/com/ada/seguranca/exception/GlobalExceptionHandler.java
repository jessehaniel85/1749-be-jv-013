package com.ada.seguranca.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

/**
 * Tratamento global de exceções.
 *
 * Estende ResponseEntityExceptionHandler para que as exceções do próprio Spring MVC (cookie ou
 * parâmetro ausente, método não permitido, corpo ilegível...) mantenham o status 4xx correto.
 * Sem isso, o handler genérico de Exception capturaria todas elas e devolveria 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(AlunoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Aula 08: senha errada ou usuário inexistente. A mesma mensagem nos dois casos, para não
    // revelar quais usuários existem.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> naoAutenticado(AuthenticationException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
    }

    // Aula 08: ResponseStatusException já traz o status e o motivo (ex.: 409 usuário já existe).
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> comStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        String mensagem = ex.getReason() != null ? ex.getReason() : "Não foi possível processar a requisição.";
        return ResponseEntity.status(ex.getStatusCode()).body(new ErroResponse(
                ex.getStatusCode().value(),
                status != null ? status.getReasonPhrase() : "Erro",
                mensagem,
                Instant.now()));
    }

    // O rate limiter da aula 01 lança esta exceção.
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErroResponse> muitasRequisicoes(RequestNotPermitted ex) {
        return resposta(HttpStatus.TOO_MANY_REQUESTS,
                "Muitas requisições. Tente novamente em instantes.");
    }

    // Rede de segurança: o detalhe técnico vai só para o log, o usuário vê uma frase genérica.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente mais tarde.");
    }

    // Formato único também para os erros do Spring MVC tratados pela classe-base.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String erro = status != null ? status.getReasonPhrase() : "Erro";
        ErroResponse corpo = new ErroResponse(
                statusCode.value(), erro, mensagemGenerica(statusCode), Instant.now());
        return ResponseEntity.status(statusCode).headers(headers).body(corpo);
    }

    private String mensagemGenerica(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "Requisição inválida.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método não permitido.";
            case 415 -> "Tipo de conteúdo não suportado.";
            default -> "Não foi possível processar a requisição.";
        };
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        ErroResponse corpo = new ErroResponse(
                status.value(), status.getReasonPhrase(), mensagem, Instant.now());
        return ResponseEntity.status(status).body(corpo);
    }
}
