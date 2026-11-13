package br.edu.ufvjm.locadora.ator;

import br.edu.ufvjm.locadora.ator.dto.AtorRequest;
import br.edu.ufvjm.locadora.ator.dto.AtorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Mesmos mecanismos de FilmeService (save/findAll/dirty checking/delete); as marcas do
// roteiro ficam lá, no primeiro uso de cada comando.
@Service
@Transactional(readOnly = true)
public class AtorService {

    private final AtorRepository atorRepository;

    public AtorService(AtorRepository atorRepository) {
        this.atorRepository = atorRepository;
    }

    public Page<AtorResponse> listar(String nome, Pageable pageable) {
        if (nome == null || nome.isBlank()) {
            return atorRepository.findAll(pageable).map(AtorResponse::from);
        }
        String termo = nome.trim();
        return atorRepository
                .findByPrimeiroNomeContainingIgnoreCaseOrSobrenomeContainingIgnoreCase(termo, termo, pageable)
                .map(AtorResponse::from);
    }

    public AtorResponse buscar(Integer id) {
        return AtorResponse.from(obter(id));
    }

    @Transactional
    public AtorResponse criar(AtorRequest req) {
        Ator ator = new Ator();
        preencher(ator, req);
        return AtorResponse.from(atorRepository.save(ator));
    }

    @Transactional
    public AtorResponse atualizar(Integer id, AtorRequest req) {
        Ator ator = obter(id);
        preencher(ator, req); // dirty checking: UPDATE no commit
        return AtorResponse.from(ator);
    }

    // Excluir ator apaga também seus vínculos em filme_ator (ON DELETE CASCADE do V2).
    @Transactional
    public void excluir(Integer id) {
        atorRepository.delete(obter(id));
    }

    private Ator obter(Integer id) {
        return atorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ator " + id + " não encontrado"));
    }

    private static void preencher(Ator ator, AtorRequest req) {
        ator.setPrimeiroNome(req.primeiroNome().trim());
        ator.setSobrenome(req.sobrenome().trim());
    }
}
