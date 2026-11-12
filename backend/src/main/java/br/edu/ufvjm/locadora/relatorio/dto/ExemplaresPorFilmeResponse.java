package br.edu.ufvjm.locadora.relatorio.dto;

// Montado direto pelo JPQL de FilmeRepository (constructor expression): count() devolve Long.
public record ExemplaresPorFilmeResponse(Integer idFilme, String nomeFilme, Long totalExemplares,
        Long disponiveis) {
}
