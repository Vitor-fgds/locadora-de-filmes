package br.edu.ufvjm.locadora.cliente.dto;

import br.edu.ufvjm.locadora.cliente.Cliente;

public record ClienteResponse(Integer id, String primeiroNome, String sobrenome, String rua, String bairro,
        String cidade, String email, String telefone) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getPrimeiroNome(), c.getSobrenome(), c.getRua(),
                c.getBairro(), c.getCidade(), c.getEmail(), c.getTelefone());
    }
}
