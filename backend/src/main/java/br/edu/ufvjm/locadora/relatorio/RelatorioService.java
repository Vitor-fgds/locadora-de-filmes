package br.edu.ufvjm.locadora.relatorio;

import br.edu.ufvjm.locadora.aluguel.AluguelRepository;
import br.edu.ufvjm.locadora.filme.FilmeAtorRepository;
import br.edu.ufvjm.locadora.filme.FilmeRepository;
import br.edu.ufvjm.locadora.relatorio.dto.ExemplaresPorFilmeResponse;
import br.edu.ufvjm.locadora.relatorio.dto.FaturamentoFuncionarioResponse;
import br.edu.ufvjm.locadora.relatorio.dto.FilmeComElencoResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF11: os três relatórios do BD1. O SQL de cada um está nos repositories (marcas 2b-JUNCAO).
@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final AluguelRepository aluguelRepository;
    private final FilmeRepository filmeRepository;
    private final FilmeAtorRepository filmeAtorRepository;

    public RelatorioService(AluguelRepository aluguelRepository, FilmeRepository filmeRepository,
            FilmeAtorRepository filmeAtorRepository) {
        this.aluguelRepository = aluguelRepository;
        this.filmeRepository = filmeRepository;
        this.filmeAtorRepository = filmeAtorRepository;
    }

    public List<FaturamentoFuncionarioResponse> faturamentoPorFuncionario() {
        return aluguelRepository.faturamentoPorFuncionario().stream()
                .map(FaturamentoFuncionarioResponse::from).toList();
    }

    public List<ExemplaresPorFilmeResponse> exemplaresPorFilme() {
        return filmeRepository.exemplaresPorFilme();
    }

    public List<FilmeComElencoResponse> filmesComMaisDeUmAtor() {
        return filmeAtorRepository.findFilmesComMaisDeUmAtor();
    }
}
