package br.edu.ufvjm.locadora.exemplar;

import br.edu.ufvjm.locadora.common.error.RegraNegocioException;
import br.edu.ufvjm.locadora.exemplar.dto.ExemplarDisponivelResponse;
import br.edu.ufvjm.locadora.exemplar.dto.ExemplarResponse;
import br.edu.ufvjm.locadora.filme.Filme;
import br.edu.ufvjm.locadora.filme.FilmeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF04. O DTO é montado aqui dentro (com a transação aberta): a associação filme é LAZY.
@Service
@Transactional(readOnly = true)
public class ExemplarService {

    private final ExemplarFilmeRepository exemplarRepository;
    private final FilmeRepository filmeRepository;

    public ExemplarService(ExemplarFilmeRepository exemplarRepository, FilmeRepository filmeRepository) {
        this.exemplarRepository = exemplarRepository;
        this.filmeRepository = filmeRepository;
    }

    public Page<ExemplarResponse> listarPorFilme(Integer idFilme, Pageable pageable) {
        if (!filmeRepository.existsById(idFilme)) {
            throw new EntityNotFoundException("Filme " + idFilme + " não encontrado");
        }
        return exemplarRepository.findByFilmeId(idFilme, pageable).map(ExemplarResponse::from);
    }

    // RF09: só os exemplares livres (JOIN exemplar x filme em ExemplarFilmeRepository).
    public List<ExemplarDisponivelResponse> listarDisponiveis(Integer idFilme) {
        if (!filmeRepository.existsById(idFilme)) {
            throw new EntityNotFoundException("Filme " + idFilme + " não encontrado");
        }
        return exemplarRepository.findDisponiveisPorFilme(idFilme);
    }

    // Todo exemplar novo nasce disponível (alugado = false, RN01).
    @Transactional
    public ExemplarResponse criar(Integer idFilme) {
        Filme filme = obterFilme(idFilme);
        ExemplarFilme exemplar = new ExemplarFilme();
        exemplar.setFilme(filme);
        exemplar.setAlugado(false);
        return ExemplarResponse.from(exemplarRepository.save(exemplar));
    }

    // Só troca o filme (corrige erro de cadastro). Exemplar alugado fica travado: há um
    // aluguel em aberto apontando para ele.
    @Transactional
    public ExemplarResponse trocarFilme(Integer id, Integer idFilme) {
        ExemplarFilme exemplar = obter(id);
        if (exemplar.isAlugado()) {
            throw new RegraNegocioException("Exemplar " + id + " está alugado e não pode mudar de filme.");
        }
        exemplar.setFilme(obterFilme(idFilme)); // dirty checking: UPDATE exemplar_filme SET id_filme=?
        return ExemplarResponse.from(exemplar);
    }

    // Exemplar com aluguéis registrados: a FK fk_aluguel_exemplar recusa → 409 (RN03).
    @Transactional
    public void excluir(Integer id) {
        exemplarRepository.delete(obter(id));
    }

    private ExemplarFilme obter(Integer id) {
        return exemplarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exemplar " + id + " não encontrado"));
    }

    private Filme obterFilme(Integer idFilme) {
        return filmeRepository.findById(idFilme)
                .orElseThrow(() -> new EntityNotFoundException("Filme " + idFilme + " não encontrado"));
    }
}
