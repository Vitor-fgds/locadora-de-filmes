package br.edu.ufvjm.locadora.exemplar;

import br.edu.ufvjm.locadora.exemplar.dto.ExemplarDisponivelResponse;
import br.edu.ufvjm.locadora.exemplar.dto.ExemplarRequest;
import br.edu.ufvjm.locadora.exemplar.dto.ExemplarResponse;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Listar: ATENDENTE (e ADMIN). Criar, trocar filme e excluir: só ADMIN.
@RestController
@RequestMapping("/api")
public class ExemplarController {

    private final ExemplarService exemplarService;

    public ExemplarController(ExemplarService exemplarService) {
        this.exemplarService = exemplarService;
    }

    @GetMapping("/filmes/{idFilme}/exemplares")
    @PreAuthorize("hasRole('ATENDENTE')")
    public PagedModel<ExemplarResponse> listar(@PathVariable Integer idFilme,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(exemplarService.listarPorFilme(idFilme, pageable));
    }

    @GetMapping("/filmes/{idFilme}/exemplares/disponiveis")
    @PreAuthorize("hasRole('ATENDENTE')")
    public List<ExemplarDisponivelResponse> disponiveis(@PathVariable Integer idFilme) {
        return exemplarService.listarDisponiveis(idFilme);
    }

    @PostMapping("/filmes/{idFilme}/exemplares")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ExemplarResponse criar(@PathVariable Integer idFilme) {
        return exemplarService.criar(idFilme);
    }

    @PutMapping("/exemplares/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ExemplarResponse trocarFilme(@PathVariable Integer id, @Valid @RequestBody ExemplarRequest req) {
        return exemplarService.trocarFilme(id, req.idFilme());
    }

    @DeleteMapping("/exemplares/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        exemplarService.excluir(id);
    }
}
