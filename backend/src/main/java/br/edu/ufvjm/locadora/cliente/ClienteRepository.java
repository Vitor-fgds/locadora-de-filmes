package br.edu.ufvjm.locadora.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // [ROTEIRO-FW-MAPEAMENTO] Query derivada com OR:
    //   SELECT ... FROM cliente WHERE upper(primeiro_nome) LIKE upper(?) OR upper(sobrenome) LIKE upper(?)
    //   (paginado, mais SELECT count(*)). Busca por nome de RF05.
    Page<Cliente> findByPrimeiroNomeContainingIgnoreCaseOrSobrenomeContainingIgnoreCase(
            String primeiroNome, String sobrenome, Pageable pageable);
}
