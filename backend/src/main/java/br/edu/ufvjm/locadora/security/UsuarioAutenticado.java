package br.edu.ufvjm.locadora.security;

import br.edu.ufvjm.locadora.funcionario.Funcionario;
import br.edu.ufvjm.locadora.funcionario.Perfil;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Principal guardado na sessão HTTP depois do login. Não é a entidade JPA: só os dados
// que a API precisa (id para registrar aluguel, nome/email/perfil para /api/auth/me).
// O hash da senha só existe até a autenticação terminar — o ProviderManager chama
// eraseCredentials() e ele não fica na sessão.
public final class UsuarioAutenticado implements UserDetails, CredentialsContainer {

    private final Integer id;
    private final String nome;
    private final String email;
    private final Perfil perfil;
    private String senha;

    private UsuarioAutenticado(Integer id, String nome, String email, Perfil perfil, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.senha = senha;
    }

    public static UsuarioAutenticado from(Funcionario funcionario) {
        return new UsuarioAutenticado(
                funcionario.getId(),
                funcionario.getPrimeiroNome() + " " + funcionario.getSobrenome(),
                funcionario.getEmail(),
                funcionario.getPerfil(),
                funcionario.getSenha());
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    // Perfil ADMIN vira a authority ROLE_ADMIN, usada por hasRole('ADMIN').
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public void eraseCredentials() {
        senha = null;
    }
}
