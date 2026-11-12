package br.edu.ufvjm.locadora.cliente;

import br.edu.ufvjm.locadora.cliente.dto.ClienteRequest;
import br.edu.ufvjm.locadora.cliente.dto.ClienteResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// ATENDENTE (e ADMIN) mantém clientes; só o ADMIN exclui.
@RestController
@RequestMapping("/api/clientes")
@PreAuthorize("hasRole('ATENDENTE')")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public PagedModel<ClienteResponse> listar(@RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(clienteService.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Integer id) {
        return clienteService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@Valid @RequestBody ClienteRequest req) {
        return clienteService.criar(req);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequest req) {
        return clienteService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        clienteService.excluir(id);
    }
}
