package br.edu.ufvjm.locadora.ator;

import br.edu.ufvjm.locadora.ator.dto.AtorRequest;
import br.edu.ufvjm.locadora.ator.dto.AtorResponse;
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

// Leitura: ATENDENTE (e ADMIN). Escrita: só ADMIN.
@RestController
@RequestMapping("/api/atores")
@PreAuthorize("hasRole('ATENDENTE')")
public class AtorController {

    private final AtorService atorService;

    public AtorController(AtorService atorService) {
        this.atorService = atorService;
    }

    @GetMapping
    public PagedModel<AtorResponse> listar(@RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(atorService.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public AtorResponse buscar(@PathVariable Integer id) {
        return atorService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AtorResponse criar(@Valid @RequestBody AtorRequest req) {
        return atorService.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AtorResponse atualizar(@PathVariable Integer id, @Valid @RequestBody AtorRequest req) {
        return atorService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        atorService.excluir(id);
    }
}
