package br.edu.ufvjm.locadora.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ufvjm.locadora.TestcontainersConfiguration;
import br.edu.ufvjm.locadora.funcionario.Funcionario;
import br.edu.ufvjm.locadora.funcionario.FuncionarioRepository;
import br.edu.ufvjm.locadora.funcionario.Perfil;
import br.edu.ufvjm.locadora.support.ApiSessao;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.web.servlet.MvcResult;

// Segurança ponta a ponta contra um Postgres real (Testcontainers): login por email +
// BCrypt, sessão, 401/403 em ProblemDetail e CSRF.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

    private static final String SENHA = "senha-de-teste";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiSessao api;

    @BeforeEach
    void criaFuncionarios() {
        ApiSessao.limparBanco(jdbcTemplate);
        api = new ApiSessao(mockMvc);
        funcionarioRepository.save(funcionario("admin@teste.com", Perfil.ADMIN));
        funcionarioRepository.save(funcionario("atendente@teste.com", Perfil.ATENDENTE));
    }

    @Test
    void loginOkDevolveUsuarioSemSenhaEMantemSessao() throws Exception {
        MvcResult resultado = mockMvc.perform(api.comCsrf(ApiSessao.login("atendente@teste.com", SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("atendente@teste.com"))
                .andExpect(jsonPath("$.perfil").value("ATENDENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        mockMvc.perform(get("/api/auth/me").session(ApiSessao.sessaoDe(resultado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("atendente@teste.com"));
    }

    @Test
    void senhaErradaEEmailInexistenteDaoMesmaMensagemGenerica() throws Exception {
        mockMvc.perform(api.comCsrf(ApiSessao.login("atendente@teste.com", "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas"));

        mockMvc.perform(api.comCsrf(ApiSessao.login("naoexiste@teste.com", SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas"));
    }

    @Test
    void semSessaoRetorna401EmProblemDetail() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void atendenteEmRotaAdminRecebe403EAdminPassa() throws Exception {
        MockHttpSession atendente = ApiSessao.sessaoDe(mockMvc
                .perform(api.comCsrf(ApiSessao.login("atendente@teste.com", SENHA))).andReturn());
        mockMvc.perform(get("/api/funcionarios").session(atendente))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));

        MockHttpSession admin = ApiSessao.sessaoDe(mockMvc
                .perform(api.comCsrf(ApiSessao.login("admin@teste.com", SENHA))).andReturn());
        mockMvc.perform(get("/api/funcionarios").session(admin))
                .andExpect(status().isOk());
    }

    @Test
    void postSemCsrfERejeitadoMesmoComCredenciaisValidas() throws Exception {
        mockMvc.perform(ApiSessao.login("admin@teste.com", SENHA))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Token CSRF ausente ou inválido"));
    }

    // Contrato do SPA: o cookie XSRF-TOKEN sozinho não basta (um site malicioso também
    // faz o navegador enviá-lo); a requisição precisa repetir o valor no header.
    @Test
    void cookieCsrfSemHeaderERejeitado() throws Exception {
        Cookie cookie = api.pedeCookieCsrf();

        mockMvc.perform(ApiSessao.login("admin@teste.com", SENHA).cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Token CSRF ausente ou inválido"));
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        MockHttpSession sessao = ApiSessao.sessaoDe(mockMvc
                .perform(api.comCsrf(ApiSessao.login("admin@teste.com", SENHA))).andReturn());

        mockMvc.perform(api.comCsrf(post("/api/auth/logout").session(sessao)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(sessao))
                .andExpect(status().isUnauthorized());
    }

    private Funcionario funcionario(String email, Perfil perfil) {
        Funcionario f = new Funcionario();
        f.setPrimeiroNome("Teste");
        f.setSobrenome(perfil.name());
        f.setRua("Rua A");
        f.setBairro("Centro");
        f.setCidade("Diamantina");
        f.setEmail(email);
        f.setTelefone("38999999999");
        f.setSenha(passwordEncoder.encode(SENHA));
        f.setPerfil(perfil);
        return f;
    }
}
