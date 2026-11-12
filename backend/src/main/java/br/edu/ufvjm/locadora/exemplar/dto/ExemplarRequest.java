package br.edu.ufvjm.locadora.exemplar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Usado só no PUT (trocar o filme do exemplar). O status (alugado) não é editável aqui:
// muda apenas pelo aluguel e pela devolução (RN01).
public record ExemplarRequest(@NotNull @Positive Integer idFilme) {
}
