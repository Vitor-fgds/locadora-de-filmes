package br.edu.ufvjm.locadora.security;

import br.edu.ufvjm.locadora.security.dto.LoginRequest;
import br.edu.ufvjm.locadora.security.dto.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Login por JSON com sessão HTTP. O logout (POST /api/auth/logout) não está aqui: é o
// LogoutFilter do Spring Security, configurado em SecurityConfig.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy contextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public AuthController(AuthenticationManager authenticationManager,
                          SessionAuthenticationStrategy sessionAuthenticationStrategy,
                          SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
    }

    // O SPA chama isto ao carregar: gera o token e devolve o cookie XSRF-TOKEN, que o
    // front reenvia no header X-XSRF-TOKEN em todo POST/PUT/DELETE (inclusive no login).
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest login,
                                 HttpServletRequest request, HttpServletResponse response) {
        // Senha errada ou email inexistente → BadCredentialsException → 401 genérico.
        Authentication autenticacao = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(login.email(), login.senha()));

        // Troca o id da sessão (contra session fixation) e gera outro token CSRF.
        sessionAuthenticationStrategy.onAuthentication(autenticacao, request, response);

        SecurityContext contexto = contextHolderStrategy.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        contextHolderStrategy.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);

        // O token CSRF novo é gerado sob demanda: lê-lo aqui faz o cookie novo já vir
        // na resposta do login, sem o front precisar chamar /csrf de novo.
        CsrfToken novoToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (novoToken != null) {
            novoToken.getToken();
        }

        return UsuarioResponse.from((UsuarioAutenticado) autenticacao.getPrincipal());
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return UsuarioResponse.from(usuario);
    }
}
