package br.edu.ufvjm.locadora.filme;

import br.edu.ufvjm.locadora.ator.Ator;
import br.edu.ufvjm.locadora.relatorio.dto.FilmeComElencoResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FilmeAtorRepository extends JpaRepository<FilmeAtor, FilmeAtorId> {

    // [ROTEIRO-2b-JUNCAO] Elenco de um filme (RF03): JOIN filme_ator x ator.
    //   SQL: select a.* from filme_ator fa join ator a on a.id_ator = fa.id_ator
    //        where fa.id_filme = ? order by a.primeiro_nome, a.sobrenome, a.id_ator
    @Query("""
            select a from FilmeAtor fa join fa.ator a
            where fa.id.idFilme = :idFilme
            order by a.primeiroNome, a.sobrenome, a.id""")
    List<Ator> findElencoByFilme(@Param("idFilme") Integer idFilme);

    // [ROTEIRO-2b-JUNCAO][ROTEIRO-1c-CONSULTA] RF11: filmes com mais de um ator.
    //   JOIN filme_ator x filme + GROUP BY + HAVING count(*) > 1 (HAVING filtra depois de agrupar).
    @Query("""
            select new br.edu.ufvjm.locadora.relatorio.dto.FilmeComElencoResponse(f.id, f.nome, count(fa))
            from FilmeAtor fa join fa.filme f
            group by f.id, f.nome
            having count(fa) > 1
            order by count(fa) desc, f.nome""")
    List<FilmeComElencoResponse> findFilmesComMaisDeUmAtor();
}
