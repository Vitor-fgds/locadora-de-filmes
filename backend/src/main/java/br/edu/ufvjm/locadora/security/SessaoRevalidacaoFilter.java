package br.edu.ufvjm.locadora.security;

import br.edu.ufvjm.locadora.funcionario.FuncionarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

// A sessão guarda o perfil e a identidade de quem logou; sem este filtro, rebaixar, trocar a
// senha ou excluir um funcionário não afetaria as sessões que ele já tem abertas (autorização
// "velha"). A cada requisição autenticada, relê perfil e hash da senha do banco (1 SELECT de
// 2 colunas) e compara com o carimbo da sessão. Se a conta sumiu ou o perfil/senha mudaram,
// a sessão é invalidada e a requisição segue como anônima: o Spring Security responde 401
// em ProblemDetail e o front volta ao login.
//
// Não é @Component de propósito: o Spring Boot registraria o filtro também fora da cadeia de
// segurança (executando duas vezes). Ele é criado em SecurityConfig e entra só na cadeia.
public class SessaoRevalidacaoFilter extends OncePerRequestFilter {

    private final FuncionarioRepository funcionarioRepository;
    private final SecurityContextLogoutHandler encerraSessao = new SecurityContextLogoutHandler();

    public SessaoRevalidacaoFilter(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario
                && !credenciaisInalteradas(usuario)) {
            // Invalida a HttpSession e limpa o SecurityContext desta requisição.
            encerraSessao.logout(request, response, autenticacao);
        }
        chain.doFilter(request, response);
    }

    // [ROTEIRO-1c-CONSULTA] SELECT perfil, senha FROM funcionario WHERE id_funcionario = ?
    private boolean credenciaisInalteradas(UsuarioAutenticado usuario) {
        return funcionarioRepository.findCredenciaisById(usuario.getId())
                .map(atual -> UsuarioAutenticado.versaoDasCredenciais(atual.getPerfil(), atual.getSenha())
                        .equals(usuario.getVersaoCredenciais()))
                .orElse(false); // conta excluída
    }
}
