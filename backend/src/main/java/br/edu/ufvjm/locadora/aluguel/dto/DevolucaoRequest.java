package br.edu.ufvjm.locadora.aluguel.dto;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

// Corpo opcional: sem corpo (ou sem a data), a devolução é registrada com a data de hoje.
public record DevolucaoRequest(@PastOrPresent LocalDate dataDevolucao) {
}
