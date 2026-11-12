package br.edu.ufvjm.locadora.exemplar;

import br.edu.ufvjm.locadora.exemplar.dto.ExemplarDisponivelResponse;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExemplarFilmeRepository extends JpaRepository<ExemplarFilme, Integer> {

    // [ROTEIRO-FW-MAPEAMENTO] Query derivada sobre a associação (filme.id):
    //   SELECT ... FROM exemplar_filme WHERE id_filme = ? ORDER BY ... (usa idx_exemplar_filme_filme)
    Page<ExemplarFilme> findByFilmeId(Integer idFilme, Pageable pageable);

    // [ROTEIRO-2a-TRANSACAO][ROTEIRO-FW-MAPEAMENTO] Lock pessimista (RF07): @Lock faz o Hibernate
    //   acrescentar FOR NO KEY UPDATE OF ... ao SELECT (o lock de escrita do PostgreSQL;
    //   no Postgres é o que o dialeto escolhe para PESSIMISTIC_WRITE). Quem chega depois espera o COMMIT/ROLLBACK de quem
    //   chegou antes; ao ler a linha, já vê status = TRUE. É o que impede dois aluguéis do
    //   mesmo exemplar ao mesmo tempo (RN01). Só pode ser chamado dentro de uma transação.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ExemplarFilme e where e.id = :id")
    Optional<ExemplarFilme> findByIdParaAluguel(@Param("id") Integer id);

    // [ROTEIRO-2b-JUNCAO][ROTEIRO-1c-CONSULTA] RF09: exemplares disponíveis de um filme.
    //   JOIN exemplar_filme x filme; status = FALSE significa disponível (semântica do BD1).
    @Query("""
            select new br.edu.ufvjm.locadora.exemplar.dto.ExemplarDisponivelResponse(e.id, f.id, f.nome)
            from ExemplarFilme e join e.filme f
            where f.id = :idFilme and e.alugado = false
            order by e.id""")
    List<ExemplarDisponivelResponse> findDisponiveisPorFilme(@Param("idFilme") Integer idFilme);
}
