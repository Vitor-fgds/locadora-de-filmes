package br.edu.ufvjm.locadora.security;

import br.edu.ufvjm.locadora.funcionario.FuncionarioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.web.servlet.HandlerExceptionResolver;

// Autenticação por sessão (cookie JSESSIONID HttpOnly), sem JWT. O login é JSON
// (AuthController); a autorização por perfil fica nos controllers com @PreAuthorize.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    FuncionarioRepository funcionarioRepository,
                                    CookieCsrfTokenRepository csrfTokenRepository,
                                    SecurityContextRepository securityContextRepository,
                                    @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        // 401/403 gerados nos filtros (sem sessão, CSRF inválido) são repassados ao
        // GlobalExceptionHandler, que responde em ProblemDetail como os demais erros.
        AuthenticationEntryPoint naoAutenticado =
                (request, response, ex) -> resolver.resolveException(request, response, null, ex);
        AccessDeniedHandler acessoNegado =
                (request, response, ex) -> resolver.resolveException(request, response, null, ex);

        http
                // CSRF ligado, no modo SPA: token no cookie XSRF-TOKEN (legível pelo JS) e
                // conferido no header X-XSRF-TOKEN em todo método que altera estado.
                .csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository))
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                // API JSON: nada de guardar a requisição na sessão para redirecionar depois.
                .requestCache(cache -> cache.disable())
                // Depois que a sessão carrega o usuário e antes de qualquer regra de acesso:
                // derruba a sessão de quem foi rebaixado, trocou a senha ou teve a conta excluída.
                .addFilterAfter(new SessaoRevalidacaoFilter(funcionarioRepository), SecurityContextHolderFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // /api inteira exige login; o resto do GET é o frontend (Fase 7): arquivos
                        // estáticos e rotas do React Router, públicos como qualquer site.
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/**").permitAll()
                        .anyRequest().denyAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(naoAutenticado)
                        .accessDeniedHandler(acessoNegado))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    // Lê o hash "$2a$..." gravado pelo seed (pgcrypto) e pelos cadastros da Fase 4a.
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    // Grava o SecurityContext na HttpSession; o mesmo bean é usado pelo filtro (para ler)
    // e pelo AuthController (para salvar após o login).
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("Lax"));
        return repository;
    }

    // Executada pelo AuthController no login: troca o id da sessão (session fixation) e
    // descarta o token CSRF anterior — o mesmo que o formLogin faria automaticamente.
    @Bean
    SessionAuthenticationStrategy sessionAuthenticationStrategy(CookieCsrfTokenRepository csrfTokenRepository) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(csrfTokenRepository)));
    }

    // ADMIN herda tudo que o ATENDENTE pode: @PreAuthorize("hasRole('ATENDENTE')") também
    // libera o ADMIN. Usado automaticamente pelo @EnableMethodSecurity e pelas regras de URL.
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("ATENDENTE")
                .build();
    }
}
