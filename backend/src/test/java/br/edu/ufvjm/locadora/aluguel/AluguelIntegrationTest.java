package br.edu.ufvjm.locadora.aluguel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ufvjm.locadora.TestcontainersConfiguration;
import br.edu.ufvjm.locadora.aluguel.dto.AluguelRequest;
import br.edu.ufvjm.locadora.common.error.RegraNegocioException;
import br.edu.ufvjm.locadora.support.ApiSessao;
import br.edu.ufvjm.locadora.support.DadosDeTeste;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// Aluguel (transação + lock), devolução (stored procedure) e consultas com JOIN, contra um
// PostgreSQL real. A procedure só existe de verdade no banco: por isso nada aqui é mockado.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AluguelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AluguelService aluguelService;

    private ApiSessao api;
    private DadosDeTeste dados;
    private MockHttpSession atendente;
    private MockHttpSession admin;
    private int idAtendente;
    private int idAdmin;
    private int cliente;
    private int filme;

    @BeforeEach
    void preparaCenario() throws Exception {
        ApiSessao.limparBanco(jdbc);
        api = new ApiSessao(mockMvc);
        dados = new DadosDeTeste(jdbc, passwordEncoder);
        idAdmin = dados.criaFuncionario("admin@teste.com", "ADMIN");
        idAtendente = dados.criaFuncionario("atendente@teste.com", "ATENDENTE");
        admin = api.logar("admin@teste.com", DadosDeTeste.SENHA);
        atendente = api.logar("atendente@teste.com", DadosDeTeste.SENHA);
        cliente = dados.criaCliente("cliente@teste.com");
        filme = dados.criaFilme("Matrix");
    }

    // ---------- RF07: aluguel ----------

    @Test
    void alugarGravaAluguelComFuncionarioLogadoEMarcaExemplarComoAlugado() throws Exception {
        int exemplar = dados.criaExemplar(filme, false);

        mockMvc.perform(json(post("/api/alugueis"), atendente, pedido(exemplar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idExemplar").value(exemplar))
                .andExpect(jsonPath("$.nomeFilme").value("Matrix"))
                .andExpect(jsonPath("$.idFuncionario").value(idAtendente))
                .andExpect(jsonPath("$.dataDevolucao").doesNotExist());

        assertThat(totalAlugueis()).isEqualTo(1);
        assertThat(exemplarAlugado(exemplar)).isTrue();
    }

    // Rollback: a regra falha depois do lock e nada é gravado.
    @Test
    void exemplarJaAlugadoRetorna422ESemEfeitoNoBanco() throws Exception {
        int exemplar = dados.criaExemplar(filme, true);

        mockMvc.perform(json(post("/api/alugueis"), atendente, pedido(exemplar)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Exemplar " + exemplar + " já está alugado."));

        assertThat(totalAlugueis()).isZero();
        assertThat(exemplarAlugado(exemplar)).isTrue();
    }

    // A API barra data de entrega no passado (400), mas o banco também: chamando o service direto
    // (sem a validação do DTO), o INSERT viola ck_aluguel_datas (RN04) e a transação inteira,
    // incluindo o FOR UPDATE do exemplar, é desfeita.
    @Test
    void falhaDoBancoNoInsertDesfazATransacaoInteira() {
        int exemplar = dados.criaExemplar(filme, false);
        AluguelRequest invalido = new AluguelRequest(exemplar, cliente, LocalDate.now().minusDays(1),
                new BigDecimal("10.00"));

        assertThatThrownBy(() -> aluguelService.registrar(invalido, idAtendente))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(totalAlugueis()).isZero();
        assertThat(exemplarAlugado(exemplar)).isFalse();
    }

    @Test
    void clienteInexistenteRetorna404EOExemplarContinuaLivre() throws Exception {
        int exemplar = dados.criaExemplar(filme, false);
        String corpo = """
                {"idExemplar":%d,"idCliente":9999,"dataEntrega":"%s","valorAluguel":10.00}"""
                .formatted(exemplar, LocalDate.now().plusDays(3));

        mockMvc.perform(json(post("/api/alugueis"), atendente, corpo))
                .andExpect(status().isNotFound());

        assertThat(totalAlugueis()).isZero();
        assertThat(exemplarAlugado(exemplar)).isFalse();
    }

    @Test
    void validacaoDoPedidoRetorna400() throws Exception {
        int exemplar = dados.criaExemplar(filme, false);
        String corpo = """
                {"idExemplar":%d,"idCliente":%d,"dataEntrega":"2020-01-01","valorAluguel":-5}"""
                .formatted(exemplar, cliente);

        mockMvc.perform(json(post("/api/alugueis"), atendente, corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[?(@.campo=='dataEntrega')]").exists())
                .andExpect(jsonPath("$.erros[?(@.campo=='valorAluguel')]").exists());
    }

    // O lock pessimista (SELECT ... FOR UPDATE) serializa as duas transações: a segunda só lê o
    // exemplar depois do COMMIT da primeira e já o vê alugado.
    @Test
    void doisAlugueisSimultaneosDoMesmoExemplarSoUmVence() throws Exception {
        int exemplar = dados.criaExemplar(filme, false);
        AluguelRequest req = new AluguelRequest(exemplar, cliente, LocalDate.now().plusDays(3),
                new BigDecimal("10.00"));
        int tentativas = 4;
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(tentativas);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (int i = 0; i < tentativas; i++) {
                Callable<Boolean> tarefa = () -> {
                    largada.await();
                    try {
                        aluguelService.registrar(req, idAtendente);
                        return true;
                    } catch (RegraNegocioException e) {
                        return false;
                    }
                };
                resultados.add(pool.submit(tarefa));
            }
            largada.countDown();
            long sucessos = 0;
            for (Future<Boolean> f : resultados) {
                if (f.get()) {
                    sucessos++;
                }
            }
            assertThat(sucessos).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
        assertThat(totalAlugueis()).isEqualTo(1);
        assertThat(exemplarAlugado(exemplar)).isTrue();
    }

    // ---------- RF08: devolução (stored procedure) ----------

    @Test
    void devolucaoNoPrazoNaoTemMultaELiberaOExemplar() throws Exception {
        int exemplar = dados.criaExemplar(filme, true);
        int aluguel = dados.criaAluguel(exemplar, cliente, idAdmin, LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5), 12.90);

        mockMvc.perform(json(post("/api/alugueis/" + aluguel + "/devolucao"), atendente,
                        "{\"dataDevolucao\":\"2026-06-05\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataDevolucao").value("2026-06-05"))
                .andExpect(jsonPath("$.valorMulta").value(0.0));

        assertThat(exemplarAlugado(exemplar)).isFalse();
    }

    @Test
    void devolucaoComAtrasoCobraDoisReaisPorDia() throws Exception {
        int exemplar = dados.criaExemplar(filme, true);
        int aluguel = dados.criaAluguel(exemplar, cliente, idAdmin, LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5), 12.90);

        mockMvc.perform(json(post("/api/alugueis/" + aluguel + "/devolucao"), atendente,
                        "{\"dataDevolucao\":\"2026-06-08\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorMulta").value(6.0));

        assertThat(jdbc.queryForObject("SELECT valor_multa FROM aluguel WHERE id_aluguel = ?",
                BigDecimal.class, aluguel)).isEqualByComparingTo("6.00");
        assertThat(exemplarAlugado(exemplar)).isFalse();
    }

    @Test
    void devolverDeNovoRetorna422ComAMensagemDaProcedure() throws Exception {
        int exemplar = dados.criaExemplar(filme, true);
        int aluguel = dados.criaAluguel(exemplar, cliente, idAdmin, LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5), 12.90);
        String corpo = "{\"dataDevolucao\":\"2026-06-05\"}";
        mockMvc.perform(json(post("/api/alugueis/" + aluguel + "/devolucao"), atendente, corpo))
                .andExpect(status().isOk());

        mockMvc.perform(json(post("/api/alugueis/" + aluguel + "/devolucao"), atendente, corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.startsWith("Aluguel " + aluguel + " já foi devolvido")));
    }

    @Test
    void devolucaoAnteriorAoAluguelRetorna422() throws Exception {
        int exemplar = dados.criaExemplar(filme, true);
        int aluguel = dados.criaAluguel(exemplar, cliente, idAdmin, LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 15), 12.90);

        mockMvc.perform(json(post("/api/alugueis/" + aluguel + "/devolucao"), atendente,
                        "{\"dataDevolucao\":\"2026-06-01\"}"))
                .andExpect(status().isUnprocessableEntity());

        assertThat(exemplarAlugado(exemplar)).isTrue();
    }

    @Test
    void devolucaoSemCorpoUsaHojeEAluguelInexistenteRetorna404() throws Exception {
        int exemplar = dados.criaExemplar(filme, true);
        int aluguel = dados.criaAluguel(exemplar, cliente, idAdmin, LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5), 12.90);

        mockMvc.perform(api.comCsrf(post("/api/alugueis/" + aluguel + "/devolucao").session(atendente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataDevolucao").value(LocalDate.now().toString()));
        mockMvc.perform(api.comCsrf(post("/api/alugueis/9999/devolucao").session(atendente)))
                .andExpect(status().isNotFound());
    }

    // ---------- RF09 – RF11: consultas com JOIN ----------

    @Test
    void disponiveisListaSoExemplaresLivresDoFilme() throws Exception {
        int livre = dados.criaExemplar(filme, false);
        dados.criaExemplar(filme, true);
        dados.criaExemplar(dados.criaFilme("Outro"), false);

        mockMvc.perform(get("/api/filmes/" + filme + "/exemplares/disponiveis").session(atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idExemplar").value(livre))
                .andExpect(jsonPath("$[0].nomeFilme").value("Matrix"));
    }

    @Test
    void historicoTrazClienteFuncionarioEFilmeEFiltraEmAbertoECliente() throws Exception {
        int exemplar1 = dados.criaExemplar(filme, true);
        int exemplar2 = dados.criaExemplar(filme, true);
        int outroCliente = dados.criaCliente("outro@teste.com");
        int devolvido = dados.criaAluguel(exemplar1, cliente, idAdmin, LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5), 10);
        int aberto = dados.criaAluguel(exemplar2, outroCliente, idAtendente, LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 6), 15);
        jdbc.update("UPDATE aluguel SET data_devolucao = '2026-06-05', valor_multa = 0 WHERE id_aluguel = ?", devolvido);

        mockMvc.perform(get("/api/alugueis").session(atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(aberto))
                .andExpect(jsonPath("$.content[0].nomeCliente").value("Cli Ente"))
                .andExpect(jsonPath("$.content[0].nomeFuncionario").value("Teste ATENDENTE"))
                .andExpect(jsonPath("$.content[0].nomeFilme").value("Matrix"));
        mockMvc.perform(get("/api/alugueis?emAberto=true").session(atendente))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(aberto));
        mockMvc.perform(get("/api/alugueis?idCliente=" + cliente).session(atendente))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(devolvido));
    }

    @Test
    void relatoriosSomamAgrupamEFiltramComHaving() throws Exception {
        int exemplar1 = dados.criaExemplar(filme, true);
        int exemplar2 = dados.criaExemplar(filme, false);
        int semExemplar = dados.criaFilme("Sem exemplar");
        dados.criaAluguel(exemplar1, cliente, idAdmin, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5), 10);
        dados.criaAluguel(exemplar2, cliente, idAdmin, LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 6), 5.50);
        jdbc.update("UPDATE aluguel SET valor_multa = 3 WHERE id_exemplar = ?", exemplar1);
        int ator1 = dados.criaAtor();
        int ator2 = dados.criaAtor();
        dados.vincula(filme, ator1);
        dados.vincula(filme, ator2);
        dados.vincula(semExemplar, ator1);

        mockMvc.perform(get("/api/relatorios/faturamento-funcionarios").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idFuncionario").value(idAdmin))
                .andExpect(jsonPath("$[0].totalAlugueis").value(2))
                .andExpect(jsonPath("$[0].valorAlugueis").value(15.5))
                .andExpect(jsonPath("$[0].valorMultas").value(3.0))
                .andExpect(jsonPath("$[0].valorTotal").value(18.5));
        mockMvc.perform(get("/api/relatorios/exemplares-por-filme").session(admin))
                .andExpect(jsonPath("$[?(@.nomeFilme=='Matrix')].totalExemplares").value(2))
                .andExpect(jsonPath("$[?(@.nomeFilme=='Matrix')].disponiveis").value(1))
                .andExpect(jsonPath("$[?(@.nomeFilme=='Sem exemplar')].totalExemplares").value(0));
        mockMvc.perform(get("/api/relatorios/filmes-com-mais-de-um-ator").session(admin))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomeFilme").value("Matrix"))
                .andExpect(jsonPath("$[0].totalAtores").value(2));
    }

    @Test
    void atendenteNaoAcessaRelatorios() throws Exception {
        mockMvc.perform(get("/api/relatorios/faturamento-funcionarios").session(atendente))
                .andExpect(status().isForbidden());
    }

    // ---------- apoio ----------

    private String pedido(int idExemplar) {
        return """
                {"idExemplar":%d,"idCliente":%d,"dataEntrega":"%s","valorAluguel":12.90}"""
                .formatted(idExemplar, cliente, LocalDate.now().plusDays(3));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, MockHttpSession sessao,
            String corpo) throws Exception {
        return api.comCsrf(req.session(sessao).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private int totalAlugueis() {
        return jdbc.queryForObject("SELECT count(*) FROM aluguel", Integer.class);
    }

    private boolean exemplarAlugado(int idExemplar) {
        return jdbc.queryForObject("SELECT status FROM exemplar_filme WHERE id_exemplar = ?", Boolean.class,
                idExemplar);
    }
}
