package br.edu.ufvjm.locadora.filme;

import br.edu.ufvjm.locadora.ator.Ator;
import br.edu.ufvjm.locadora.ator.AtorRepository;
import br.edu.ufvjm.locadora.ator.dto.AtorResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF03: vincular/desvincular atores a filmes (tabela filme_ator).
@Service
@Transactional(readOnly = true)
public class ElencoService {

    private final FilmeRepository filmeRepository;
    private final AtorRepository atorRepository;
    private final FilmeAtorRepository filmeAtorRepository;

    public ElencoService(FilmeRepository filmeRepository, AtorRepository atorRepository,
            FilmeAtorRepository filmeAtorRepository) {
        this.filmeRepository = filmeRepository;
        this.atorRepository = atorRepository;
        this.filmeAtorRepository = filmeAtorRepository;
    }

    public List<AtorResponse> listar(Integer idFilme) {
        if (!filmeRepository.existsById(idFilme)) {
            throw new EntityNotFoundException("Filme " + idFilme + " não encontrado");
        }
        return filmeAtorRepository.findElencoByFilme(idFilme).stream().map(AtorResponse::from).toList();
    }

    @Transactional
    public void vincular(Integer idFilme, Integer idAtor) {
        Filme filme = filmeRepository.findById(idFilme)
                .orElseThrow(() -> new EntityNotFoundException("Filme " + idFilme + " não encontrado"));
        Ator ator = atorRepository.findById(idAtor)
                .orElseThrow(() -> new EntityNotFoundException("Ator " + idAtor + " não encontrado"));
        FilmeAtorId id = new FilmeAtorId(idFilme, idAtor);
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-SELECT] existsById → SELECT count(*) FROM filme_ator
        //   WHERE (id_ator, id_filme) = (?, ?): só confere se o vínculo existe, sem carregar a linha.
        if (filmeAtorRepository.existsById(id)) {
            throw new DataIntegrityViolationException("Ator já vinculado ao filme");
        }
        FilmeAtor vinculo = new FilmeAtor();
        vinculo.setId(id);
        vinculo.setFilme(filme);
        vinculo.setAtor(ator);
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-INSERT] A chave composta já vem preenchida, então o
        //   Spring Data trata a entidade como "existente" e faz merge(): um SELECT em filme_ator
        //   WHERE (id_ator, id_filme) IN ((?,?)) (não acha nada) e depois
        //   INSERT INTO filme_ator (id_ator, id_filme) VALUES (?,?).
        filmeAtorRepository.save(vinculo);
    }

    @Transactional
    public void desvincular(Integer idFilme, Integer idAtor) {
        FilmeAtorId id = new FilmeAtorId(idFilme, idAtor);
        if (!filmeAtorRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Vínculo entre filme " + idFilme + " e ator " + idAtor + " não encontrado");
        }
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-DELETE] deleteById → o Hibernate carrega a linha
        //   (SELECT) e executa DELETE FROM filme_ator WHERE id_filme=? AND id_ator=?
        filmeAtorRepository.deleteById(id);
    }
}
