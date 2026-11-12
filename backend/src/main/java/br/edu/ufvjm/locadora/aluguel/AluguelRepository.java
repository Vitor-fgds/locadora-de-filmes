package br.edu.ufvjm.locadora.aluguel;

import br.edu.ufvjm.locadora.aluguel.dto.AluguelResponse;
import br.edu.ufvjm.locadora.relatorio.dto.FaturamentoFuncionarioView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AluguelRepository extends JpaRepository<Aluguel, Integer> {

    // [ROTEIRO-2b-JUNCAO][ROTEIRO-1c-CONSULTA] RF10: histórico com JOIN de 5 tabelas
    //   aluguel x cliente x funcionario x exemplar_filme x filme, numa consulta só (sem N+1).
    //   Filtros opcionais: cliente e "somente em aberto" (data_devolucao IS NULL).
    @Query(value = """
            select new br.edu.ufvjm.locadora.aluguel.dto.AluguelResponse(
                a.id, a.dataAluguel, a.dataEntrega, a.dataDevolucao, a.valorAluguel, a.valorMulta,
                c.id, concat(c.primeiroNome, ' ', c.sobrenome),
                fu.id, concat(fu.primeiroNome, ' ', fu.sobrenome),
                e.id, fi.id, fi.nome)
            from Aluguel a
                join a.cliente c
                join a.funcionario fu
                join a.exemplar e
                join e.filme fi
            where (:idCliente is null or c.id = :idCliente)
              and (:emAberto = false or a.dataDevolucao is null)
            order by a.id desc""",
            countQuery = """
            select count(a) from Aluguel a
            where (:idCliente is null or a.cliente.id = :idCliente)
              and (:emAberto = false or a.dataDevolucao is null)""")
    Page<AluguelResponse> historico(@Param("idCliente") Integer idCliente,
            @Param("emAberto") boolean emAberto, Pageable pageable);

    // [ROTEIRO-2b-JUNCAO] Mesmo JOIN do histórico, para um aluguel só (resposta do POST).
    @Query("""
            select new br.edu.ufvjm.locadora.aluguel.dto.AluguelResponse(
                a.id, a.dataAluguel, a.dataEntrega, a.dataDevolucao, a.valorAluguel, a.valorMulta,
                c.id, concat(c.primeiroNome, ' ', c.sobrenome),
                fu.id, concat(fu.primeiroNome, ' ', fu.sobrenome),
                e.id, fi.id, fi.nome)
            from Aluguel a
                join a.cliente c
                join a.funcionario fu
                join a.exemplar e
                join e.filme fi
            where a.id = :id""")
    Optional<AluguelResponse> buscarResposta(@Param("id") Integer id);

    // [ROTEIRO-2b-JUNCAO][ROTEIRO-1c-CONSULTA] RF11 — CONSULTA NATIVA (SQL puro): este texto vai
    //   ao PostgreSQL exatamente como está escrito, sem tradução do Hibernate. JOIN funcionario x
    //   aluguel + GROUP BY + SUM = faturamento (aluguéis e multas) por funcionário. Os apelidos
    //   entre aspas casam com os getters da projeção.
    @Query(value = """
            select f.id_funcionario                          as "idFuncionario",
                   f.primeiro_nome || ' ' || f.sobrenome     as "nomeFuncionario",
                   count(a.id_aluguel)                       as "totalAlugueis",
                   coalesce(sum(a.valor_aluguel), 0)         as "valorAlugueis",
                   coalesce(sum(a.valor_multa), 0)           as "valorMultas"
            from funcionario f
                join aluguel a on a.id_funcionario = f.id_funcionario
            group by f.id_funcionario, f.primeiro_nome, f.sobrenome
            order by sum(a.valor_aluguel) desc, f.id_funcionario""", nativeQuery = true)
    List<FaturamentoFuncionarioView> faturamentoPorFuncionario();
}
