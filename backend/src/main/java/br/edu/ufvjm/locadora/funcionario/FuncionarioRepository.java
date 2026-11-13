package br.edu.ufvjm.locadora.funcionario;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    // [ROTEIRO-1c-CONSULTA][ROTEIRO-FW-MAPEAMENTO] Query derivada do nome do método: o
    //   Spring Data gera SELECT ... FROM funcionario f WHERE f.email = ? (usa o índice
    //   da UNIQUE de email). Usada no login (RF01) pelo FuncionarioUserDetailsService.
    Optional<Funcionario> findByEmail(String email);

    // [ROTEIRO-1c-CONSULTA][ROTEIRO-FW-MAPEAMENTO] Revalidação da sessão: query derivada com
    //   projeção — o Hibernate seleciona só as colunas da interface:
    //   SELECT f.perfil, f.senha FROM funcionario f WHERE f.id_funcionario = ?
    //   Roda a cada requisição autenticada (SessaoRevalidacaoFilter); vazio = conta excluída.
    Optional<FuncionarioCredenciais> findCredenciaisById(Integer id);

    // [ROTEIRO-FW-MAPEAMENTO] Query derivada com OR (mesmo padrão da busca de clientes):
    //   SELECT ... FROM funcionario WHERE upper(primeiro_nome) LIKE upper(?) OR upper(sobrenome) LIKE upper(?)
    //   (paginado, mais SELECT count(*)). Busca por nome de RF06.
    Page<Funcionario> findByPrimeiroNomeContainingIgnoreCaseOrSobrenomeContainingIgnoreCase(
            String primeiroNome, String sobrenome, Pageable pageable);
}
