package br.edu.ufvjm.locadora.ator.dto;

import br.edu.ufvjm.locadora.ator.Ator;

public record AtorResponse(Integer id, String primeiroNome, String sobrenome) {

    public static AtorResponse from(Ator ator) {
        return new AtorResponse(ator.getId(), ator.getPrimeiroNome(), ator.getSobrenome());
    }
}
