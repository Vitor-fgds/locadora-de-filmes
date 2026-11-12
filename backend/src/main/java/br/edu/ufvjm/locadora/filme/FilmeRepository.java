package br.edu.ufvjm.locadora.filme;

import br.edu.ufvjm.locadora.relatorio.dto.ExemplaresPorFilmeResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FilmeRepository extends JpaRepository<Filme, Integer> {

    // [ROTEIRO-FW-MAPEAMENTO] Query derivada: o Spring Data lê o nome do método e gera
    //   SELECT ... FROM filme WHERE upper(nome) LIKE upper(?) ORDER BY id_filme FETCH FIRST ? ROWS ONLY
    //   (o SELECT count(*) do total só roda se a página vier cheia). Busca por nome de RF02.
    Page<Filme> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // [ROTEIRO-2b-JUNCAO][ROTEIRO-1c-CONSULTA] RF11: exemplares por filme. LEFT JOIN filme x
    //   exemplar_filme (filme sem exemplar aparece com 0) + GROUP BY + count(). O JPQL liga as
    //   duas entidades por "on e.filme = f" porque Filme não tem coleção de exemplares.
    @Query("""
            select new br.edu.ufvjm.locadora.relatorio.dto.ExemplaresPorFilmeResponse(
                f.id, f.nome, count(e.id), count(case when e.alugado = false then e.id end))
            from Filme f left join ExemplarFilme e on e.filme = f
            group by f.id, f.nome
            order by f.nome, f.id""")
    List<ExemplaresPorFilmeResponse> exemplaresPorFilme();
}
