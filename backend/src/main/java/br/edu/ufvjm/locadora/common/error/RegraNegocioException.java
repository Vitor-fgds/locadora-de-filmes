package br.edu.ufvjm.locadora.common.error;

// Violação de regra de negócio (ex.: exemplar alugado não pode trocar de filme).
// Vira HTTP 422 no GlobalExceptionHandler; a Fase 4b também a usa para o P0001 da procedure.
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
