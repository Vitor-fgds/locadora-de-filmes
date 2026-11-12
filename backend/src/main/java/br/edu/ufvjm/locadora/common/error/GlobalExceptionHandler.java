package br.edu.ufvjm.locadora.common.error;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// Traduz exceções em ProblemDetail (RFC 9457). Estende ResponseEntityExceptionHandler para
// que erros do próprio Spring MVC (JSON malformado, método não suportado, id não numérico)
// virem 4xx em ProblemDetail em vez de cair no handler genérico (500). O mapeamento de
// regra de negócio da stored procedure (P0001 -> 422) usará RegraNegocioException na Fase 4b.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setTitle("Dados inválidos");
        problema.setDetail("Um ou mais campos não passaram na validação.");
        problema.setProperty("erros", ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> Map.of("campo", erro.getField(),
                        "mensagem", String.valueOf(erro.getDefaultMessage())))
                .toList());
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ProblemDetail handleNaoEncontrado(Exception ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problema.setTitle("Recurso não encontrado");
        problema.setDetail(ex.getMessage());
        return problema;
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail handleRegraNegocio(RegraNegocioException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problema.setTitle("Regra de negócio violada");
        problema.setDetail(ex.getMessage());
        return problema;
    }

    // RN03: o banco recusa (FK sem ON DELETE) e a API responde 409 com mensagem amigável,
    // escolhida pelo nome da constraint que o PostgreSQL informa.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegridade(DataIntegrityViolationException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problema.setTitle("Violação de integridade");
        problema.setDetail(mensagemDaConstraint(ex));
        return problema;
    }

    private static String mensagemDaConstraint(Throwable ex) {
        String nome = null;
        String texto = "";
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (nome == null && t instanceof ConstraintViolationException cve) {
                nome = cve.getConstraintName();
            }
            texto = String.valueOf(t.getMessage());
        }
        String alvo = ((nome != null ? nome : "") + " " + texto).toLowerCase();
        if (alvo.contains("fk_aluguel_")) {
            return "Não é possível excluir: há aluguéis registrados para este registro (RN03).";
        }
        if (alvo.contains("fk_exemplar_filme")) {
            return "Não é possível excluir: o filme possui exemplares cadastrados.";
        }
        if (alvo.contains("email_key")) {
            return "Já existe um cadastro com este email.";
        }
        return "A operação viola uma restrição do banco (chave única ou referência existente).";
    }

    // ?sort=campoInexistente: erro do cliente, não do servidor.
    @ExceptionHandler({PropertyReferenceException.class, InvalidDataAccessApiUsageException.class})
    public ProblemDetail handleOrdenacaoInvalida(Exception ex) {
        boolean ordenacao = false;
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof PropertyReferenceException) {
                ordenacao = true;
                break;
            }
        }
        if (!ordenacao) {
            return handleGenerico(ex);
        }
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setTitle("Parâmetro inválido");
        problema.setDetail("Campo de ordenação inexistente.");
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
