package br.edu.ufvjm.locadora.relatorio;

import br.edu.ufvjm.locadora.relatorio.dto.ExemplaresPorFilmeResponse;
import br.edu.ufvjm.locadora.relatorio.dto.FaturamentoFuncionarioResponse;
import br.edu.ufvjm.locadora.relatorio.dto.FilmeComElencoResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Relatórios (incluindo faturamento): só ADMIN.
@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("hasRole('ADMIN')")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/faturamento-funcionarios")
    public List<FaturamentoFuncionarioResponse> faturamentoPorFuncionario() {
        return relatorioService.faturamentoPorFuncionario();
    }

    @GetMapping("/exemplares-por-filme")
    public List<ExemplaresPorFilmeResponse> exemplaresPorFilme() {
        return relatorioService.exemplaresPorFilme();
    }

    @GetMapping("/filmes-com-mais-de-um-ator")
    public List<FilmeComElencoResponse> filmesComMaisDeUmAtor() {
        return relatorioService.filmesComMaisDeUmAtor();
    }
}
