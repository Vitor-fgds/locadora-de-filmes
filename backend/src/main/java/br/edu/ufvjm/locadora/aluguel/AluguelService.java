package br.edu.ufvjm.locadora.aluguel;

import br.edu.ufvjm.locadora.aluguel.dto.AluguelRequest;
import br.edu.ufvjm.locadora.aluguel.dto.AluguelResponse;
import br.edu.ufvjm.locadora.cliente.Cliente;
import br.edu.ufvjm.locadora.cliente.ClienteRepository;
import br.edu.ufvjm.locadora.common.error.RegraNegocioException;
import br.edu.ufvjm.locadora.exemplar.ExemplarFilme;
import br.edu.ufvjm.locadora.exemplar.ExemplarFilmeRepository;
import br.edu.ufvjm.locadora.funcionario.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF07 (aluguel), RF08 (devolução) e RF10 (histórico).
@Service
@Transactional(readOnly = true)
public class AluguelService {

    // SQLSTATE que a procedure usa em RAISE EXCEPTION ... USING ERRCODE = 'P0001' (V3).
    private static final String SQLSTATE_REGRA_NEGOCIO = "P0001";

    private final AluguelRepository aluguelRepository;
    private final ExemplarFilmeRepository exemplarRepository;
    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final JdbcTemplate jdbcTemplate;

    public AluguelService(AluguelRepository aluguelRepository, ExemplarFilmeRepository exemplarRepository,
            ClienteRepository clienteRepository, FuncionarioRepository funcionarioRepository,
            JdbcTemplate jdbcTemplate) {
        this.aluguelRepository = aluguelRepository;
        this.exemplarRepository = exemplarRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // [ROTEIRO-2a-TRANSACAO] Registrar aluguel = 3 comandos atômicos, numa transação só:
    //   1) SELECT ... FROM exemplar_filme WHERE id_exemplar=? FOR NO KEY UPDATE (trava o exemplar)
    //   2) INSERT INTO aluguel ...
    //   3) UPDATE exemplar_filme SET status=true WHERE id_exemplar=?      (dirty checking, no COMMIT)
    //   Se qualquer passo lançar exceção (ex.: exemplar já alugado → RegraNegocioException, uma
    //   RuntimeException), o Spring faz ROLLBACK de tudo: nenhuma linha fica pela metade.
    @Transactional
    public AluguelResponse registrar(AluguelRequest req, Integer idFuncionario) {
        ExemplarFilme exemplar = exemplarRepository.findByIdParaAluguel(req.idExemplar())
                .orElseThrow(() -> new EntityNotFoundException("Exemplar " + req.idExemplar() + " não encontrado"));
        if (exemplar.isAlugado()) { // RN01
            throw new RegraNegocioException("Exemplar " + exemplar.getId() + " já está alugado.");
        }
        Cliente cliente = clienteRepository.findById(req.idCliente())
                .orElseThrow(() -> new EntityNotFoundException("Cliente " + req.idCliente() + " não encontrado"));

        Aluguel aluguel = new Aluguel();
        aluguel.setExemplar(exemplar);
        aluguel.setCliente(cliente);
        // getReferenceById não faz SELECT: só guarda o id para a FK do INSERT.
        aluguel.setFuncionario(funcionarioRepository.getReferenceById(idFuncionario));
        aluguel.setDataAluguel(LocalDate.now());
        aluguel.setDataEntrega(req.dataEntrega());
        aluguel.setValorAluguel(req.valorAluguel());
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-INSERT] save() → INSERT INTO aluguel (...) VALUES (...)
        aluguelRepository.save(aluguel);
        // [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-UPDATE] Dirty checking: o exemplar foi carregado
        //   (com FOR NO KEY UPDATE) e alterado aqui; o Hibernate envia o UPDATE exemplar_filme antes do COMMIT.
        exemplar.setAlugado(true);

        return buscar(aluguel.getId());
    }

    // [ROTEIRO-1c-PROCEDURE][ROTEIRO-2c-PROCEDURE][ROTEIRO-2a-TRANSACAO] Devolução (RF08): quem
    //   grava a devolução e calcula a multa é a stored procedure sp_registrar_devolucao
    //   (V3__procedures.sql). Aqui o Java só abre a transação, chama CALL e converte o erro de
    //   regra (SQLSTATE P0001) em HTTP 422. A procedure roda DENTRO desta transação: o JdbcTemplate
    //   reaproveita a mesma conexão do JPA, e a procedure não faz COMMIT.
    @Transactional
    public AluguelResponse devolver(Integer idAluguel, LocalDate dataDevolucao) {
        // existsById não carrega a entidade: assim o contexto do Hibernate não guarda uma cópia
        // do aluguel que a procedure vai alterar por fora (ficaria desatualizada).
        if (!aluguelRepository.existsById(idAluguel)) {
            throw new EntityNotFoundException("Aluguel " + idAluguel + " não encontrado");
        }
        LocalDate data = dataDevolucao != null ? dataDevolucao : LocalDate.now();
        try {
            // {call ...} + escapeSyntaxCallMode=callIfNoReturn (URL do datasource) → o driver envia
            //   CALL sp_registrar_devolucao(?, ?, ?). O 3º parâmetro é INOUT: entra nulo, volta a multa.
            jdbcTemplate.execute("{call sp_registrar_devolucao(?, ?, ?)}", (CallableStatementCallback<Void>) cs -> {
                cs.setInt(1, idAluguel);
                cs.setObject(2, data);
                cs.setNull(3, Types.NUMERIC);
                cs.registerOutParameter(3, Types.NUMERIC);
                cs.execute();
                return null;
            });
        } catch (DataAccessException ex) {
            throw traduzErroDaProcedure(ex);
        }
        return buscar(idAluguel);
    }

    public Page<AluguelResponse> historico(Integer idCliente, boolean emAberto, Pageable pageable) {
        // A ordenação (id desc) está na própria consulta; só página e tamanho vêm do cliente.
        return aluguelRepository.historico(idCliente, emAberto,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
    }

    public AluguelResponse buscar(Integer id) {
        return aluguelRepository.buscarResposta(id)
                .orElseThrow(() -> new EntityNotFoundException("Aluguel " + id + " não encontrado"));
    }

    // P0001 (RAISE EXCEPTION da procedure) → 422 com a mensagem escrita no próprio RAISE.
    // Qualquer outro erro continua como veio (vira 500 no handler genérico).
    private static RuntimeException traduzErroDaProcedure(DataAccessException ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && SQLSTATE_REGRA_NEGOCIO.equals(sql.getSQLState())) {
                return new RegraNegocioException(mensagemDoRaise(sql.getMessage()));
            }
        }
        return ex;
    }

    // A mensagem do driver vem como "ERROR: <texto do RAISE>\n  Where: PL/pgSQL function ...".
    private static String mensagemDoRaise(String mensagem) {
        String texto = mensagem == null ? "" : mensagem.strip();
        if (texto.startsWith("ERROR:")) {
            texto = texto.substring("ERROR:".length()).strip();
        }
        int quebra = texto.indexOf('\n');
        return quebra >= 0 ? texto.substring(0, quebra).strip() : texto;
    }
}
