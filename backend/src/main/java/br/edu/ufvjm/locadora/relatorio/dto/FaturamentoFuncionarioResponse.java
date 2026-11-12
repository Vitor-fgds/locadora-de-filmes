package br.edu.ufvjm.locadora.relatorio.dto;

import java.math.BigDecimal;

public record FaturamentoFuncionarioResponse(Integer idFuncionario, String nomeFuncionario, long totalAlugueis,
        BigDecimal valorAlugueis, BigDecimal valorMultas, BigDecimal valorTotal) {

    public static FaturamentoFuncionarioResponse from(FaturamentoFuncionarioView v) {
        return new FaturamentoFuncionarioResponse(v.getIdFuncionario(), v.getNomeFuncionario(),
                v.getTotalAlugueis(), v.getValorAlugueis(), v.getValorMultas(),
                v.getValorAlugueis().add(v.getValorMultas()));
    }
}
