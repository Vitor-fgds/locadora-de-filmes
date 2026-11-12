package br.edu.ufvjm.locadora.ator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Nomes: VARCHAR(50) no banco.
public record AtorRequest(
        @NotBlank @Size(max = 50) String primeiroNome,
        @NotBlank @Size(max = 50) String sobrenome) {
}
