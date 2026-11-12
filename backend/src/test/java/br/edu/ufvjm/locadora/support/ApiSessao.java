package br.edu.ufvjm.locadora.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// Ferramentas dos testes de API: faz o que o front faz (cookie XSRF-TOKEN + header
// X-XSRF-TOKEN, sessão por login) e zera o banco entre os testes.
public class ApiSessao {

    private final MockMvc mockMvc;

    public ApiSessao(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    // Apaga tudo (inclusive o seed, se o perfil dev estiver ativo) e reinicia os ids.
    public static void limparBanco(JdbcTemplate jdbc) {
        jdbc.execute("TRUNCATE TABLE aluguel, exemplar_filme, filme_ator, filme, ator, cliente, funcionario"
                + " RESTART IDENTITY CASCADE");
    }

    // GET /api/auth/csrf → cookie XSRF-TOKEN → reenvia o valor no header X-XSRF-TOKEN. Não usa
    // o csrf() do spring-security-test: ele troca o CsrfTokenRepository do filtro e esconderia
    // o contrato real do cookie.
    public MockHttpServletRequestBuilder comCsrf(MockHttpServletRequestBuilder requisicao) throws Exception {
        Cookie cookie = pedeCookieCsrf();
        return requisicao.cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue());
    }

    public Cookie pedeCookieCsrf() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn();
        Cookie cookie = resultado.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        return cookie;
    }

    // Login completo; devolve a sessão autenticada para usar em .session(...).
    public MockHttpSession logar(String email, String senha) throws Exception {
        return sessaoDe(mockMvc.perform(comCsrf(login(email, senha))).andReturn());
    }

    public static MockHttpServletRequestBuilder login(String email, String senha) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha));
    }

    public static MockHttpSession sessaoDe(MvcResult resultado) {
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }
}
