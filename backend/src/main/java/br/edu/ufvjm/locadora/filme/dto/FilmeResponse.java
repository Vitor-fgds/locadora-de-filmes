package br.edu.ufvjm.locadora.filme.dto;

import br.edu.ufvjm.locadora.filme.Filme;
import java.time.LocalDate;

public record FilmeResponse(Integer id, String nome, String descricao, Integer duracaoMinutos,
        LocalDate dataLancamento) {

    public static FilmeResponse from(Filme filme) {
        return new FilmeResponse(filme.getId(), filme.getNome(), filme.getDescricao(),
                filme.getDuracaoMinutos(), filme.getDataLancamento());
    }
}
