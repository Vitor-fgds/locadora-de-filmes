package br.edu.ufvjm.locadora.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ufvjm.locadora.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

// Fase 7: o Spring serve o build do React (aqui, src/test/resources/static/index.html).
// Rotas do React Router voltam para o index; /api continua protegida.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SpaFallbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void raizERotasDoReactVoltamParaOIndexSemLogin() throws Exception {
        // "/" é a welcome page do Boot: faz forward para /index.html (MockMvc não segue forward).
        mockMvc.perform(get("/")).andExpect(forwardedUrl("index.html"));
        mockMvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("INDEX-DE-TESTE")));
        mockMvc.perform(get("/clientes/1/editar")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("INDEX-DE-TESTE")));
    }

    @Test
    void arquivoEstaticoInexistenteDa404EnaoOIndex() throws Exception {
        mockMvc.perform(get("/assets/nao-existe.js")).andExpect(status().isNotFound());
    }

    @Test
    void apiContinuaProtegidaEnaoCaiNoIndex() throws Exception {
        mockMvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void metodoQueAlteraEstadoForaDaApiENegado() throws Exception {
        mockMvc.perform(post("/clientes")).andExpect(status().isForbidden());
    }
}
