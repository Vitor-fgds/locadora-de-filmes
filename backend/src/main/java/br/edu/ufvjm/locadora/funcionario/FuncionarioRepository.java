package br.edu.ufvjm.locadora.funcionario;

import java.util.Optional;
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
}
