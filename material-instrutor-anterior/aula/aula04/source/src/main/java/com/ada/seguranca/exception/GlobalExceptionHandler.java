package com.ada.seguranca.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

/**
 * Tratamento global de exceções (aula 07): cada exceção vira um status HTTP e um corpo padronizado.
 *
 * Estende ResponseEntityExceptionHandler para que os erros do próprio Spring MVC (JSON malformado,
 * método não permitido...) mantenham o status 4xx correto em vez de cair no handler genérico (500).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Senha errada, usuário inexistente OU conta desativada (BadCredentialsException,
    // DisabledException...). A MESMA mensagem nos três casos: dizer "usuário não existe" ou
    // "conta desativada" ajudaria um atacante a descobrir usuários válidos.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> naoAutenticado(AuthenticationException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
    }

    // Refresh token inválido, expirado, revogado ou reutilizado.
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErroResponse> tokenInvalido(TokenInvalidoException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Token inválido ou expirado.");
    }

    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<ErroResponse> jaExiste(UsuarioJaExisteException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Duas requisições simultâneas criando o mesmo username: a verificação "já existe?" passa nas
    // duas, e a segunda esbarra na constraint UNIQUE do banco (migration V1). Resposta: 409 também.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> violacaoDeIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "Não foi possível concluir: o registro já existe.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> semPermissao(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.");
    }

    // Rede de segurança: o detalhe técnico vai SÓ para o log; o cliente vê uma frase genérica.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    // Falha do @Valid: devolve 400 com a lista "campo: mensagem" (nunca o valor digitado, que
    // poderia ser uma senha).
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map((FieldError e) -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest().body(new ErroResponse(
                400, "Bad Request", "Dados inválidos.", Instant.now(), detalhes));
    }

    // Mesmo formato para os demais erros do Spring MVC tratados pela classe-base.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String erro = status != null ? status.getReasonPhrase() : "Erro";
        return ResponseEntity.status(statusCode).headers(headers).body(new ErroResponse(
                statusCode.value(), erro, mensagemGenerica(statusCode), Instant.now(), List.of()));
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
        return ResponseEntity.status(status).body(new ErroResponse(
                status.value(), status.getReasonPhrase(), mensagem, Instant.now(), List.of()));
    }
}
