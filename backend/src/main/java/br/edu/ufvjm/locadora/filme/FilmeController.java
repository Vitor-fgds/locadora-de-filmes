package br.edu.ufvjm.locadora.filme;

import br.edu.ufvjm.locadora.ator.dto.AtorResponse;
import br.edu.ufvjm.locadora.filme.dto.FilmeRequest;
import br.edu.ufvjm.locadora.filme.dto.FilmeResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Leitura: ATENDENTE (e ADMIN, por herança). Escrita: só ADMIN.
@RestController
@RequestMapping("/api/filmes")
@PreAuthorize("hasRole('ATENDENTE')")
public class FilmeController {

    private final FilmeService filmeService;
    private final ElencoService elencoService;

    public FilmeController(FilmeService filmeService, ElencoService elencoService) {
        this.filmeService = filmeService;
        this.elencoService = elencoService;
    }

    @GetMapping
    public PagedModel<FilmeResponse> listar(@RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(filmeService.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public FilmeResponse buscar(@PathVariable Integer id) {
        return filmeService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public FilmeResponse criar(@Valid @RequestBody FilmeRequest req) {
        return filmeService.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FilmeResponse atualizar(@PathVariable Integer id, @Valid @RequestBody FilmeRequest req) {
        return filmeService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        filmeService.excluir(id);
    }

    @GetMapping("/{id}/atores")
    public List<AtorResponse> elenco(@PathVariable Integer id) {
        return elencoService.listar(id);
    }

    @PostMapping("/{id}/atores/{idAtor}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vincular(@PathVariable Integer id, @PathVariable Integer idAtor) {
        elencoService.vincular(id, idAtor);
    }

    @DeleteMapping("/{id}/atores/{idAtor}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincular(@PathVariable Integer id, @PathVariable Integer idAtor) {
        elencoService.desvincular(id, idAtor);
    }
}
