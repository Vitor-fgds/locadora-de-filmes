package br.edu.ufvjm.locadora.exemplar.dto;

// RF09: resultado do JOIN exemplar_filme x filme. Montado direto pela consulta JPQL
// (constructor expression), sem carregar entidades.
public record ExemplarDisponivelResponse(Integer idExemplar, Integer idFilme, String nomeFilme) {
}
