package br.edu.ufvjm.locadora.ator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtorRepository extends JpaRepository<Ator, Integer> {

    // [ROTEIRO-FW-MAPEAMENTO] Query derivada com OR (mesmo padrão da busca de clientes):
    //   SELECT ... FROM ator WHERE upper(primeiro_nome) LIKE upper(?) OR upper(sobrenome) LIKE upper(?)
    //   (paginado, mais SELECT count(*)). Busca por nome de RF03.
    Page<Ator> findByPrimeiroNomeContainingIgnoreCaseOrSobrenomeContainingIgnoreCase(
            String primeiroNome, String sobrenome, Pageable pageable);
}
