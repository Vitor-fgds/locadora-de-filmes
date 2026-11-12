package br.edu.ufvjm.locadora.cliente;

import br.edu.ufvjm.locadora.cliente.dto.ClienteRequest;
import br.edu.ufvjm.locadora.cliente.dto.ClienteResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF05. Mesmos mecanismos de FilmeService; a exclusão de cliente com aluguéis é barrada
// pela FK fk_aluguel_cliente (409, RN03).
@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Page<ClienteResponse> listar(String nome, Pageable pageable) {
        if (nome == null || nome.isBlank()) {
            return clienteRepository.findAll(pageable).map(ClienteResponse::from);
        }
        String termo = nome.trim();
        return clienteRepository
                .findByPrimeiroNomeContainingIgnoreCaseOrSobrenomeContainingIgnoreCase(termo, termo, pageable)
                .map(ClienteResponse::from);
    }

    public ClienteResponse buscar(Integer id) {
        return ClienteResponse.from(obter(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest req) {
        Cliente cliente = new Cliente();
        preencher(cliente, req);
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Integer id, ClienteRequest req) {
        Cliente cliente = obter(id);
        preencher(cliente, req); // dirty checking: UPDATE no commit (email duplicado → 409)
        return ClienteResponse.from(cliente);
    }

    @Transactional
    public void excluir(Integer id) {
        clienteRepository.delete(obter(id));
    }

    private Cliente obter(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente " + id + " não encontrado"));
    }

    private static void preencher(Cliente c, ClienteRequest req) {
        c.setPrimeiroNome(req.primeiroNome().trim());
        c.setSobrenome(req.sobrenome().trim());
        c.setRua(req.rua().trim());
        c.setBairro(req.bairro().trim());
        c.setCidade(req.cidade().trim());
        c.setEmail(req.email().trim().toLowerCase());
        c.setTelefone(req.telefone());
    }
}
