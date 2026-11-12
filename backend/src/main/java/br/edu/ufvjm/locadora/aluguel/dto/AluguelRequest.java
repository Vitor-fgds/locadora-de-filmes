package br.edu.ufvjm.locadora.aluguel.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

// RN04: entrega prevista não pode ser no passado (e o banco ainda confere data_entrega >=
// data_aluguel); valor >= 0 e cabe em DECIMAL(10,2). Quem aluga é o funcionário logado e a
// data do aluguel é hoje: nenhum dos dois vem do cliente da API.
public record AluguelRequest(
        @NotNull @Positive Integer idExemplar,
        @NotNull @Positive Integer idCliente,
        @NotNull @FutureOrPresent LocalDate dataEntrega,
        @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal valorAluguel) {
}
