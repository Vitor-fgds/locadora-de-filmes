package br.edu.ufvjm.locadora.filme.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// nome: VARCHAR(50). descricao: TEXT no banco; o limite de 2000 é da API.
public record FilmeRequest(
        @NotBlank @Size(max = 50) String nome,
        @NotBlank @Size(max = 2000) String descricao,
        @NotNull @Positive @Max(1000) Integer duracaoMinutos,
        @NotNull @PastOrPresent LocalDate dataLancamento) {
}
