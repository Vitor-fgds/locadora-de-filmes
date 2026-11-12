package br.edu.ufvjm.locadora.relatorio.dto;

import java.math.BigDecimal;

// Projeção por interface da consulta nativa de faturamento: cada getter lê a coluna de
// mesmo apelido do SELECT.
public interface FaturamentoFuncionarioView {

    Integer getIdFuncionario();

    String getNomeFuncionario();

    Long getTotalAlugueis();

    BigDecimal getValorAlugueis();

    BigDecimal getValorMultas();
}
