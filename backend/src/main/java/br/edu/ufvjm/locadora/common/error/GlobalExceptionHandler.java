package br.edu.ufvjm.locadora.common.error;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Traduz exceções em ProblemDetail (RFC 9457). O mapeamento de regra de negócio da
// stored procedure (SQLSTATE P0001 -> 422) fica para a Fase 4b, quando existir uma
// exceção dedicada lançada pelo service de devolução depois de inspecionar o erro do
// CALL sp_registrar_devolucao — não há código algum chamando a procedure ainda.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacao(MethodArgumentNotValidException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setTitle("Dados inválidos");
        problema.setDetail("Um ou mais campos não passaram na validação.");
        problema.setProperty("erros", ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> Map.of("campo", erro.getField(), "mensagem", erro.getDefaultMessage()))
                .toList());
        return problema;
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ProblemDetail handleNaoEncontrado(Exception ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problema.setTitle("Recurso não encontrado");
        problema.setDetail(ex.getMessage());
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegridade(DataIntegrityViolationException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problema.setTitle("Violação de integridade");
        problema.setDetail("A operação viola uma restrição do banco (chave única ou referência existente).");
        return problema;
    }

    // Login: a mesma mensagem para email inexistente e senha errada (não revela qual dos
    // dois falhou). BadCredentialsException é subclasse de AuthenticationException, e o
    // Spring escolhe o handler mais específico.
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleCredenciaisInvalidas(BadCredentialsException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problema.setTitle("Não autenticado");
        problema.setDetail("Credenciais inválidas");
        return problema;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleNaoAutenticado(AuthenticationException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problema.setTitle("Não autenticado");
        problema.setDetail("Autenticação necessária");
        return problema;
    }

    @ExceptionHandler(CsrfException.class)
    public ProblemDetail handleCsrf(CsrfException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problema.setTitle("Acesso negado");
        problema.setDetail("Token CSRF ausente ou inválido");
        return problema;
    }

    // Inclui AuthorizationDeniedException (@PreAuthorize negado). CsrfException também é
    // AccessDeniedException, mas tem handler próprio acima.
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAcessoNegado(AccessDeniedException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problema.setTitle("Acesso negado");
        problema.setDetail("Você não tem permissão para esta operação");
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        log.error("Erro não tratado", ex);
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problema.setTitle("Erro interno");
        problema.setDetail("Ocorreu um erro inesperado.");
        return problema;
    }
}
