package br.edu.ufvjm.locadora.aluguel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// RF10: aluguel com cliente, funcionário e filme. Montado direto pelo JOIN de
// AluguelRepository (constructor expression), por isso a ordem dos campos importa.
public record AluguelResponse(
        Integer id,
        LocalDate dataAluguel,
        LocalDate dataEntrega,
        LocalDate dataDevolucao,
        BigDecimal valorAluguel,
        BigDecimal valorMulta,
        Integer idCliente,
        String nomeCliente,
        Integer idFuncionario,
        String nomeFuncionario,
        Integer idExemplar,
        Integer idFilme,
        String nomeFilme) {
}
