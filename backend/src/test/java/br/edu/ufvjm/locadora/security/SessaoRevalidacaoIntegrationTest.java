package br.edu.ufvjm.locadora.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ufvjm.locadora.TestcontainersConfiguration;
import br.edu.ufvjm.locadora.support.ApiSessao;
import br.edu.ufvjm.locadora.support.DadosDeTeste;
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

// Autorização "velha": mudar perfil/senha ou excluir um funcionário derruba as sessões que ele
// já tinha abertas (SessaoRevalidacaoFilter), em vez de valerem até expirar.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SessaoRevalidacaoIntegrationTest {

    private static final String CORPO_BASE = """
            "primeiroNome":"Func","sobrenome":"Teste","rua":"Rua A","bairro":"Centro","cidade":"Mucuri",
            "email":"%s","telefone":"33988880000\"""";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private ApiSessao api;
    private DadosDeTeste dados;
    private int idAdmin;
    private int idOutro;
    private MockHttpSession admin;
    private MockHttpSession outro;

    @BeforeEach
    void preparaDoisAdminsLogados() throws Exception {
        ApiSessao.limparBanco(jdbc);
        api = new ApiSessao(mockMvc);
        dados = new DadosDeTeste(jdbc, passwordEncoder);
        idAdmin = dados.criaFuncionario("admin@teste.com", "ADMIN");
        idOutro = dados.criaFuncionario("outro@teste.com", "ADMIN");
        admin = api.logar("admin@teste.com", DadosDeTeste.SENHA);
        outro = api.logar("outro@teste.com", DadosDeTeste.SENHA);
    }

    @Test
    void sessaoSemMudancaContinuaValida() throws Exception {
        mockMvc.perform(get("/api/funcionarios").session(outro)).andExpect(status().isOk());
        mockMvc.perform(get("/api/funcionarios").session(outro)).andExpect(status().isOk());
    }

    @Test
    void funcionarioRebaixadoPerdeAsPermissoesDeAdminNaProximaRequisicao() throws Exception {
        String rebaixar = "{" + CORPO_BASE.formatted("outro@teste.com") + ",\"perfil\":\"ATENDENTE\"}";
        mockMvc.perform(api.comCsrf(put("/api/funcionarios/" + idOutro).session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(rebaixar)))
                .andExpect(status().isOk());

        // A sessão antiga ainda dizia ADMIN: agora é derrubada (401), não vira 403 nem passa.
        mockMvc.perform(get("/api/funcionarios").session(outro)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").session(outro)).andExpect(status().isUnauthorized());
    }

    @Test
    void senhaAlteradaDerrubaASessaoAntiga() throws Exception {
        String novaSenha = "{" + CORPO_BASE.formatted("outro@teste.com")
                + ",\"perfil\":\"ADMIN\",\"senha\":\"nova-senha-123\"}";
        mockMvc.perform(api.comCsrf(put("/api/funcionarios/" + idOutro).session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(novaSenha)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/funcionarios").session(outro)).andExpect(status().isUnauthorized());
        // Quem trocou continua logado, e a senha nova funciona num login novo.
        mockMvc.perform(get("/api/funcionarios").session(admin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/auth/me").session(api.logar("outro@teste.com", "nova-senha-123")))
                .andExpect(status().isOk());
    }

    @Test
    void contaExcluidaNaoOperaMaisComASessaoAberta() throws Exception {
        mockMvc.perform(api.comCsrf(delete("/api/funcionarios/" + idOutro).session(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/funcionarios").session(outro)).andExpect(status().isUnauthorized());
    }

    @Test
    void alterarOutrosDadosNaoDerrubaASessao() throws Exception {
        String renomear = "{" + CORPO_BASE.formatted("admin@teste.com") + ",\"perfil\":\"ADMIN\"}";
        mockMvc.perform(api.comCsrf(put("/api/funcionarios/" + idAdmin).session(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(renomear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primeiroNome").value("Func"));

        mockMvc.perform(get("/api/funcionarios").session(admin)).andExpect(status().isOk());
    }
}
