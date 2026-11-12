package br.edu.ufvjm.locadora.cadastro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ufvjm.locadora.TestcontainersConfiguration;
import br.edu.ufvjm.locadora.support.ApiSessao;
import br.edu.ufvjm.locadora.support.DadosDeTeste;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// CRUDs (RF02–RF06) ponta a ponta contra um PostgreSQL real: perfis, validação (400),
// integridade referencial (409, RN03) e regras (422). Só o que não é trivial.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CadastrosIntegrationTest {

    private static final String SENHA = DadosDeTeste.SENHA;
    private static final String CLIENTE_JSON = """
            {"primeiroNome":"Ana","sobrenome":"Costa","rua":"Rua A","bairro":"Centro",
             "cidade":"Diamantina","email":"%s","telefone":"38999999999"}""";
    private static final String FILME_JSON = """
            {"nome":"Matrix","descricao":"Ficção científica","duracaoMinutos":136,
             "dataLancamento":"1999-03-31"}""";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private ApiSessao api;
    private DadosDeTeste dados;
    private MockHttpSession admin;
    private MockHttpSession atendente;
    private int idAdmin;

    @BeforeEach
    void preparaBancoESessoes() throws Exception {
        ApiSessao.limparBanco(jdbc);
        api = new ApiSessao(mockMvc);
        dados = new DadosDeTeste(jdbc, passwordEncoder);
        idAdmin = dados.criaFuncionario("admin@teste.com", "ADMIN");
        dados.criaFuncionario("atendente@teste.com", "ATENDENTE");
        admin = api.logar("admin@teste.com", SENHA);
        atendente = api.logar("atendente@teste.com", SENHA);
    }

    @Test
    void atendenteCriaClienteMasNaoExclui() throws Exception {
        String resposta = mockMvc.perform(json(post("/api/clientes"), atendente, CLIENTE_JSON.formatted("ana@teste.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        int id = Integer.parseInt(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(api.comCsrf(delete("/api/clientes/" + id).session(atendente)))
                .andExpect(status().isForbidden());
        mockMvc.perform(api.comCsrf(delete("/api/clientes/" + id).session(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void atendenteNaoEscreveNoAcervoMasLe() throws Exception {
        mockMvc.perform(json(post("/api/filmes"), atendente, FILME_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(json(post("/api/filmes"), admin, FILME_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/filmes").session(atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Matrix"));
    }

    @Test
    void validacaoRetorna400ComListaDeCampos() throws Exception {
        String invalido = """
                {"primeiroNome":"%s","sobrenome":"Costa","rua":"Rua A","bairro":"Centro",
                 "cidade":"Diamantina","email":"nao-e-email","telefone":"123"}""".formatted("x".repeat(51));
        mockMvc.perform(json(post("/api/clientes"), atendente, invalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.erros[?(@.campo=='primeiroNome')]").exists())
                .andExpect(jsonPath("$.erros[?(@.campo=='email')]").exists())
                .andExpect(jsonPath("$.erros[?(@.campo=='telefone')]").exists());
    }

    @Test
    void jsonMalformadoEOrdenacaoInvalidaSao400() throws Exception {
        mockMvc.perform(json(post("/api/clientes"), atendente, "{não é json"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/clientes?sort=campoQueNaoExiste").session(atendente))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        mockMvc.perform(json(post("/api/clientes"), atendente, CLIENTE_JSON.formatted("dup@teste.com")))
                .andExpect(status().isCreated());
        mockMvc.perform(json(post("/api/clientes"), atendente, CLIENTE_JSON.formatted("dup@teste.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um cadastro com este email."));
    }

    // RN03: o banco recusa excluir quem tem aluguel; a API traduz a FK em 409.
    @Test
    void excluirClienteFuncionarioOuExemplarComAluguelRetorna409() throws Exception {
        int filme = dados.criaFilme();
        int exemplar = dados.criaExemplar(filme, true);
        int cliente = dados.criaCliente("c1@teste.com");
        dados.criaAluguel(exemplar, cliente, idAdmin, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5), 10);

        mockMvc.perform(api.comCsrf(delete("/api/clientes/" + cliente).session(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("RN03")));
        mockMvc.perform(api.comCsrf(delete("/api/exemplares/" + exemplar).session(admin)))
                .andExpect(status().isConflict());

        int outroAdmin = dados.criaFuncionario("admin2@teste.com", "ADMIN");
        jdbc.update("UPDATE aluguel SET id_funcionario = ?", outroAdmin);
        mockMvc.perform(api.comCsrf(delete("/api/funcionarios/" + outroAdmin).session(admin)))
                .andExpect(status().isConflict());
    }

    @Test
    void excluirFilmeComExemplarRetorna409ESemExemplarApagaOElencoEmCascata() throws Exception {
        int filme = dados.criaFilme();
        int ator = dados.criaAtor();
        dados.vincula(filme, ator);
        int exemplar = dados.criaExemplar(filme, false);

        mockMvc.perform(api.comCsrf(delete("/api/filmes/" + filme).session(admin)))
                .andExpect(status().isConflict());

        jdbc.update("DELETE FROM exemplar_filme WHERE id_exemplar = ?", exemplar);
        mockMvc.perform(api.comCsrf(delete("/api/filmes/" + filme).session(admin)))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM filme_ator", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ator", Integer.class)).isEqualTo(1);
    }

    @Test
    void elencoVinculaListaEDesvincula() throws Exception {
        int filme = dados.criaFilme();
        int ator = dados.criaAtor();

        mockMvc.perform(api.comCsrf(post("/api/filmes/%d/atores/%d".formatted(filme, ator)).session(admin)))
                .andExpect(status().isNoContent());
        mockMvc.perform(api.comCsrf(post("/api/filmes/%d/atores/%d".formatted(filme, ator)).session(admin)))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/filmes/%d/atores".formatted(filme)).session(atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ator));
        mockMvc.perform(api.comCsrf(delete("/api/filmes/%d/atores/%d".formatted(filme, ator)).session(admin)))
                .andExpect(status().isNoContent());
        mockMvc.perform(api.comCsrf(delete("/api/filmes/%d/atores/%d".formatted(filme, ator)).session(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarFilmePorDirtyCheckingPersisteNoBanco() throws Exception {
        int filme = dados.criaFilme();
        mockMvc.perform(json(put("/api/filmes/" + filme), admin, FILME_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Matrix"));
        assertThat(jdbc.queryForObject("SELECT nome FROM filme WHERE id_filme = ?", String.class, filme))
                .isEqualTo("Matrix");
    }

    @Test
    void funcionarioCriadoNaoExpoeSenhaGravaBcryptEConsegueLogar() throws Exception {
        String novo = """
                {"primeiroNome":"Novo","sobrenome":"Func","rua":"Rua B","bairro":"Centro","cidade":"Mucuri",
                 "email":"novo@teste.com","telefone":"33988880000","senha":"outra-senha-1","perfil":"ATENDENTE"}""";
        mockMvc.perform(json(post("/api/funcionarios"), admin, novo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("ATENDENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        String hash = jdbc.queryForObject("SELECT senha FROM funcionario WHERE email = 'novo@teste.com'", String.class);
        assertThat(hash).startsWith("$2").isNotEqualTo("outra-senha-1");
        assertThat(api.logar("novo@teste.com", "outra-senha-1")).isNotNull();

        mockMvc.perform(get("/api/funcionarios").session(atendente)).andExpect(status().isForbidden());
    }

    // 40 × "ç" = 40 caracteres mas 80 bytes: o BCrypt só aceita 72 bytes → 400, não 500.
    @Test
    void senhaDeFuncionarioComMaisDe72BytesRetorna400() throws Exception {
        String novo = """
                {"primeiroNome":"Novo","sobrenome":"Func","rua":"Rua B","bairro":"Centro","cidade":"Mucuri",
                 "email":"longa@teste.com","telefone":"33988880000","senha":"%s","perfil":"ATENDENTE"}"""
                .formatted("ç".repeat(40));
        mockMvc.perform(json(post("/api/funcionarios"), admin, novo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminNaoExcluiNemRebaixaASiMesmo() throws Exception {
        mockMvc.perform(api.comCsrf(delete("/api/funcionarios/" + idAdmin).session(admin)))
                .andExpect(status().isUnprocessableEntity());

        String rebaixar = """
                {"primeiroNome":"Teste","sobrenome":"ADMIN","rua":"Rua A","bairro":"Centro","cidade":"Diamantina",
                 "email":"admin@teste.com","telefone":"38999999999","perfil":"ATENDENTE"}""";
        mockMvc.perform(json(put("/api/funcionarios/" + idAdmin), admin, rebaixar))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarFuncionarioSemSenhaMantemOHashAtual() throws Exception {
        int id = dados.criaFuncionario("outro@teste.com", "ATENDENTE");
        String antes = jdbc.queryForObject("SELECT senha FROM funcionario WHERE id_funcionario = ?", String.class, id);
        String corpo = """
                {"primeiroNome":"Renomeado","sobrenome":"ATENDENTE","rua":"Rua A","bairro":"Centro",
                 "cidade":"Diamantina","email":"outro@teste.com","telefone":"38999999999","perfil":"ADMIN"}""";
        mockMvc.perform(json(put("/api/funcionarios/" + id), admin, corpo)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT senha FROM funcionario WHERE id_funcionario = ?", String.class, id))
                .isEqualTo(antes);
    }

    @Test
    void exemplarAlugadoNaoTrocaDeFilmeEDisponivelTroca() throws Exception {
        int filmeA = dados.criaFilme();
        int filmeB = dados.criaFilme();
        int alugado = dados.criaExemplar(filmeA, true);
        int livre = dados.criaExemplar(filmeA, false);
        String paraB = "{\"idFilme\":" + filmeB + "}";

        mockMvc.perform(json(put("/api/exemplares/" + alugado), admin, paraB))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(json(put("/api/exemplares/" + livre), admin, paraB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idFilme").value(filmeB))
                .andExpect(jsonPath("$.alugado").value(false));
    }

    @Test
    void novoExemplarNasceDisponivel() throws Exception {
        int filme = dados.criaFilme();
        mockMvc.perform(api.comCsrf(post("/api/filmes/" + filme + "/exemplares").session(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alugado").value(false));
        mockMvc.perform(get("/api/filmes/" + filme + "/exemplares").session(atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void tamanhoDePaginaELimitadoA50() throws Exception {
        jdbc.update("INSERT INTO ator (primeiro_nome, sobrenome) SELECT 'Ator ' || g, 'Teste' FROM generate_series(1, 60) g");
        mockMvc.perform(get("/api/atores?size=500").session(atendente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(50))
                .andExpect(jsonPath("$.page.size").value(50))
                .andExpect(jsonPath("$.page.totalElements").value(60));
    }

    @Test
    void registroInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/filmes/9999").session(atendente))
                .andExpect(status().isNotFound());
    }

    // ---------- apoio ----------

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, MockHttpSession sessao, String corpo)
            throws Exception {
        return api.comCsrf(req.session(sessao).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
