package br.edu.ufvjm.locadora.exemplar.dto;

import br.edu.ufvjm.locadora.exemplar.ExemplarFilme;

public record ExemplarResponse(Integer id, Integer idFilme, boolean alugado) {

    public static ExemplarResponse from(ExemplarFilme exemplar) {
        return new ExemplarResponse(exemplar.getId(), exemplar.getFilme().getId(), exemplar.isAlugado());
    }
}
