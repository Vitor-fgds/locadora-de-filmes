package br.edu.ufvjm.locadora.funcionario.dto;

import br.edu.ufvjm.locadora.funcionario.Perfil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

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

    // @Size conta caracteres, mas o BCrypt só aceita 72 BYTES: "ç" ocupa 2 em UTF-8.
    @JsonIgnore
    @AssertTrue(message = "deve ter no máximo 72 bytes (acentos contam 2)")
    public boolean isSenhaDentroDoLimiteDoBcrypt() {
        return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "FuncionarioUpdateRequest[email=" + email + ", perfil=" + perfil + ", senha=***]";
    }
}
