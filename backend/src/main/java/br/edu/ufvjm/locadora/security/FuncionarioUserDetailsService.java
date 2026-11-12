package br.edu.ufvjm.locadora.security;

import br.edu.ufvjm.locadora.funcionario.FuncionarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Usado pelo DaoAuthenticationProvider no login: carrega o funcionário pelo email e
// devolve o hash BCrypt para o PasswordEncoder comparar com a senha digitada.
@Service
@Transactional(readOnly = true)
public class FuncionarioUserDetailsService implements UserDetailsService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioUserDetailsService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    // [ROTEIRO-1c-CONSULTA] Login (RF01): SELECT ... FROM funcionario WHERE email = ?.
    //   Email inexistente lança UsernameNotFoundException, que o DaoAuthenticationProvider
    //   transforma em BadCredentialsException: a resposta não revela se o email existe.
    @Override
    public UserDetails loadUserByUsername(String email) {
        return funcionarioRepository.findByEmail(email)
                .map(UsuarioAutenticado::from)
                .orElseThrow(() -> new UsernameNotFoundException("Funcionário não encontrado"));
    }
}
