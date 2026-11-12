package br.edu.ufvjm.locadora.aluguel;

import br.edu.ufvjm.locadora.aluguel.dto.AluguelRequest;
import br.edu.ufvjm.locadora.aluguel.dto.AluguelResponse;
import br.edu.ufvjm.locadora.aluguel.dto.DevolucaoRequest;
import br.edu.ufvjm.locadora.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// ATENDENTE e ADMIN registram aluguel/devolução e consultam o histórico.
@RestController
@RequestMapping("/api/alugueis")
@PreAuthorize("hasRole('ATENDENTE')")
public class AluguelController {

    private final AluguelService aluguelService;

    public AluguelController(AluguelService aluguelService) {
        this.aluguelService = aluguelService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AluguelResponse registrar(@Valid @RequestBody AluguelRequest req,
            @AuthenticationPrincipal UsuarioAutenticado logado) {
        return aluguelService.registrar(req, logado.getId());
    }

    @PostMapping("/{id}/devolucao")
    public AluguelResponse devolver(@PathVariable Integer id,
            @Valid @RequestBody(required = false) DevolucaoRequest req) {
        return aluguelService.devolver(id, req == null ? null : req.dataDevolucao());
    }

    @GetMapping
    public PagedModel<AluguelResponse> historico(@RequestParam(required = false) Integer idCliente,
            @RequestParam(defaultValue = "false") boolean emAberto,
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(aluguelService.historico(idCliente, emAberto, pageable));
    }

    @GetMapping("/{id}")
    public AluguelResponse buscar(@PathVariable Integer id) {
        return aluguelService.buscar(id);
    }
}
