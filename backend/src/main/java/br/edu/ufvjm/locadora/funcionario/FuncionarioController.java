package br.edu.ufvjm.locadora.funcionario;

import br.edu.ufvjm.locadora.funcionario.dto.FuncionarioCreateRequest;
import br.edu.ufvjm.locadora.funcionario.dto.FuncionarioResponse;
import br.edu.ufvjm.locadora.funcionario.dto.FuncionarioUpdateRequest;
import br.edu.ufvjm.locadora.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

// Tudo aqui é só do ADMIN.
@RestController
@RequestMapping("/api/funcionarios")
@PreAuthorize("hasRole('ADMIN')")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public PagedModel<FuncionarioResponse> listar(@RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(funcionarioService.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable Integer id) {
        return funcionarioService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponse criar(@Valid @RequestBody FuncionarioCreateRequest req) {
        return funcionarioService.criar(req);
    }

    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(@PathVariable Integer id,
            @Valid @RequestBody FuncionarioUpdateRequest req,
            @AuthenticationPrincipal UsuarioAutenticado logado) {
        return funcionarioService.atualizar(id, req, logado.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado logado) {
        funcionarioService.excluir(id, logado.getId());
    }
}
