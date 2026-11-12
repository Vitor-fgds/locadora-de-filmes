package br.edu.ufvjm.locadora.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// email: VARCHAR(50) no banco. senha: o BCrypt só considera os primeiros 72 bytes.
public record LoginRequest(
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Size(max = 72) String senha) {

    // Evita que a senha apareça em log ou mensagem de erro via toString() do record.
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", senha=***]";
    }
}
