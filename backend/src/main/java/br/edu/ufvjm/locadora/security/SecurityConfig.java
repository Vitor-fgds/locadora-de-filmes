package br.edu.ufvjm.locadora.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Segurança provisória da Fase 2: tudo autenticado, sem UserDetailsService próprio —
// o Spring Boot cria um único usuário em memória com senha gerada no log de boot. A
// Fase 3 substitui isso por login por funcionario.email + BCrypt + sessão, sem precisar
// remover nada daqui: basta declarar um UserDetailsService real, que desativa o
// autogerado automaticamente.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // [ROTEIRO-1c-CONEXAO] Nenhum endpoint é público nesta fase.
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // httpBasic (não formLogin): devolve 401 + WWW-Authenticate em vez de
                // redirecionar para uma página HTML de login — compatível com a API
                // JSON que a Fase 3 constrói em POST /api/auth/login. CSRF fica ligado
                // no padrão; a configuração de CSRF para SPA é da Fase 3.
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
