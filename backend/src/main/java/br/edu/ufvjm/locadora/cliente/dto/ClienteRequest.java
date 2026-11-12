package br.edu.ufvjm.locadora.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Tamanhos iguais aos VARCHAR do banco: textos 50, telefone 11 (só dígitos, DDD + número).
public record ClienteRequest(
        @NotBlank @Size(max = 50) String primeiroNome,
        @NotBlank @Size(max = 50) String sobrenome,
        @NotBlank @Size(max = 50) String rua,
        @NotBlank @Size(max = 50) String bairro,
        @NotBlank @Size(max = 50) String cidade,
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Pattern(regexp = "\\d{10,11}", message = "deve ter 10 ou 11 dígitos (DDD + número)") String telefone) {
}
