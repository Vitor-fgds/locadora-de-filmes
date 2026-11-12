package br.edu.ufvjm.locadora.filme;

import br.edu.ufvjm.locadora.filme.dto.FilmeRequest;
import br.edu.ufvjm.locadora.filme.dto.FilmeResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FilmeService {

    private final FilmeRepository filmeRepository;

    public FilmeService(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    public Page<FilmeResponse> listar(String nome, Pageable pageable) {
        if (nome == null || nome.isBlank()) {
            // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-SELECT] findAll(pageable) → o Hibernate
            //   executa SELECT ... FROM filme ORDER BY id_filme OFFSET ? ROWS FETCH FIRST ? ROWS ONLY
            //   e, para o total da página, SELECT count(*) FROM filme.
            return filmeRepository.findAll(pageable).map(FilmeResponse::from);
        }
        return filmeRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable)
                .map(FilmeResponse::from);
    }

    public FilmeResponse buscar(Integer id) {
        return FilmeResponse.from(obter(id));
    }

    @Transactional
    public FilmeResponse criar(FilmeRequest req) {
        Filme filme = new Filme();
        preencher(filme, req);
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-INSERT] save() com id nulo (entidade nova) →
        //   INSERT INTO filme (data_lancamento, descricao, duracao_minutos, nome) VALUES (?,?,?,?)
        //   O id vem da coluna IDENTITY do PostgreSQL (RETURNING id_filme).
        return FilmeResponse.from(filmeRepository.save(filme));
    }

    @Transactional
    public FilmeResponse atualizar(Integer id, FilmeRequest req) {
        Filme filme = obter(id);
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-UPDATE] Dirty checking: não há chamada a save().
        //   O Hibernate guarda uma cópia do estado ao carregar o filme; no COMMIT compara e,
        //   se algo mudou, executa UPDATE filme SET data_lancamento=?, descricao=?,
        //   duracao_minutos=?, nome=? WHERE id_filme=?
        preencher(filme, req);
        return FilmeResponse.from(filme);
    }

    @Transactional
    public void excluir(Integer id) {
        Filme filme = obter(id);
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-DELETE] delete() → DELETE FROM filme WHERE id_filme=?
        //   As linhas de filme_ator somem pelo ON DELETE CASCADE do V2; se o filme tiver
        //   exemplares, a FK fk_exemplar_filme recusa e a API responde 409 (RN03).
        filmeRepository.delete(filme);
    }

    private Filme obter(Integer id) {
        return filmeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Filme " + id + " não encontrado"));
    }

    private static void preencher(Filme filme, FilmeRequest req) {
        filme.setNome(req.nome().trim());
        filme.setDescricao(req.descricao().trim());
        filme.setDuracaoMinutos(req.duracaoMinutos());
        filme.setDataLancamento(req.dataLancamento());
    }
}
