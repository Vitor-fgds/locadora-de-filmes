package br.edu.ufvjm.locadora.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ufvjm.locadora.TestcontainersConfiguration;
import br.edu.ufvjm.locadora.funcionario.Funcionario;
import br.edu.ufvjm.locadora.funcionario.FuncionarioRepository;
import br.edu.ufvjm.locadora.funcionario.Perfil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Segurança ponta a ponta contra um Postgres real (Testcontainers): login por email +
// BCrypt, sessão, 401/403 em ProblemDetail e CSRF.
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, AuthIntegrationTest.RotaAdminDeTeste.class})
class AuthIntegrationTest {

    private static final String SENHA = "senha-de-teste";

    // Ainda não existe rota ADMIN real (vem na Fase 4a): esta só serve para provar que
    // @PreAuthorize responde 403 ao ATENDENTE.
    @RestController
    static class RotaAdminDeTeste {
        @GetMapping("/api/teste/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String soAdmin() {
            return "ok";
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void criaFuncionarios() {
        funcionarioRepository.deleteAll();
        funcionarioRepository.save(funcionario("admin@teste.com", Perfil.ADMIN));
        funcionarioRepository.save(funcionario("atendente@teste.com", Perfil.ATENDENTE));
    }

    @Test
    void loginOkDevolveUsuarioSemSenhaEMantemSessao() throws Exception {
        MvcResult resultado = mockMvc.perform(comCsrf(login("atendente@teste.com", SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("atendente@teste.com"))
                .andExpect(jsonPath("$.perfil").value("ATENDENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        mockMvc.perform(get("/api/auth/me").session(sessaoDe(resultado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("atendente@teste.com"));
    }

    @Test
    void senhaErradaEEmailInexistenteDaoMesmaMensagemGenerica() throws Exception {
        mockMvc.perform(comCsrf(login("atendente@teste.com", "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas"));

        mockMvc.perform(comCsrf(login("naoexiste@teste.com", SENHA)))
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
        MockHttpSession atendente = sessaoDe(mockMvc
                .perform(comCsrf(login("atendente@teste.com", SENHA))).andReturn());
        mockMvc.perform(get("/api/teste/admin").session(atendente))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));

        MockHttpSession admin = sessaoDe(mockMvc
                .perform(comCsrf(login("admin@teste.com", SENHA))).andReturn());
        mockMvc.perform(get("/api/teste/admin").session(admin))
                .andExpect(status().isOk());
    }

    @Test
    void postSemCsrfERejeitadoMesmoComCredenciaisValidas() throws Exception {
        mockMvc.perform(login("admin@teste.com", SENHA))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Token CSRF ausente ou inválido"));
    }

    // Contrato do SPA: o cookie XSRF-TOKEN sozinho não basta (um site malicioso também
    // faz o navegador enviá-lo); a requisição precisa repetir o valor no header.
    @Test
    void cookieCsrfSemHeaderERejeitado() throws Exception {
        Cookie cookie = pedeCookieCsrf();

        mockMvc.perform(login("admin@teste.com", SENHA).cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Token CSRF ausente ou inválido"));
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        MockHttpSession sessao = sessaoDe(mockMvc
                .perform(comCsrf(login("admin@teste.com", SENHA))).andReturn());

        mockMvc.perform(comCsrf(post("/api/auth/logout").session(sessao)))
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

    // Faz o que o front faz: GET /api/auth/csrf → cookie XSRF-TOKEN → reenvia o valor no
    // header X-XSRF-TOKEN. Não usa o csrf() do spring-security-test: ele troca o
    // CsrfTokenRepository do filtro compartilhado e esconderia o contrato real do cookie.
    private MockHttpServletRequestBuilder comCsrf(MockHttpServletRequestBuilder requisicao) throws Exception {
        Cookie cookie = pedeCookieCsrf();
        return requisicao.cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue());
    }

    private Cookie pedeCookieCsrf() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn();
        Cookie cookie = resultado.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        return cookie;
    }

    private static MockHttpServletRequestBuilder login(String email, String senha) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha));
    }

    private static MockHttpSession sessaoDe(MvcResult resultado) {
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }
}
