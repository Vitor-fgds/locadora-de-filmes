package br.edu.ufvjm.locadora.funcionario.dto;

import br.edu.ufvjm.locadora.funcionario.Funcionario;
import br.edu.ufvjm.locadora.funcionario.Perfil;

// Nunca contém a senha (nem o hash).
public record FuncionarioResponse(Integer id, String primeiroNome, String sobrenome, String rua, String bairro,
        String cidade, String email, String telefone, Perfil perfil) {

    public static FuncionarioResponse from(Funcionario f) {
        return new FuncionarioResponse(f.getId(), f.getPrimeiroNome(), f.getSobrenome(), f.getRua(),
                f.getBairro(), f.getCidade(), f.getEmail(), f.getTelefone(), f.getPerfil());
    }
}
