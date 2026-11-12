package br.edu.ufvjm.locadora.funcionario.dto;

import br.edu.ufvjm.locadora.funcionario.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// senha é opcional na edição: ausente (null) mantém o hash atual. Se vier, passa pelas
// mesmas regras do cadastro (8 a 72 caracteres).
public record FuncionarioUpdateRequest(
        @NotBlank @Size(max = 50) String primeiroNome,
        @NotBlank @Size(max = 50) String sobrenome,
        @NotBlank @Size(max = 50) String rua,
        @NotBlank @Size(max = 50) String bairro,
        @NotBlank @Size(max = 50) String cidade,
        @NotBlank @Email @Size(max = 50) String email,
        @NotBlank @Pattern(regexp = "\\d{10,11}", message = "deve ter 10 ou 11 dígitos (DDD + número)") String telefone,
        @Size(min = 8, max = 72) String senha,
        @NotNull Perfil perfil) {

    @Override
    public String toString() {
        return "FuncionarioUpdateRequest[email=" + email + ", perfil=" + perfil + ", senha=***]";
    }
}
