package br.edu.ufvjm.locadora.security;

import br.edu.ufvjm.locadora.funcionario.Funcionario;
import br.edu.ufvjm.locadora.funcionario.Perfil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Principal guardado na sessão HTTP depois do login. Não é a entidade JPA: só os dados
// que a API precisa (id para registrar aluguel, nome/email/perfil para /api/auth/me).
// O hash da senha só existe até a autenticação terminar — o ProviderManager chama
// eraseCredentials() e ele não fica na sessão. No lugar dele a sessão guarda um "carimbo"
// (versaoCredenciais): o SHA-256 de perfil + hash da senha, que o SessaoRevalidacaoFilter
// compara com o banco a cada requisição para derrubar sessões de credencial alterada.
public final class UsuarioAutenticado implements UserDetails, CredentialsContainer {

    private final Integer id;
    private final String nome;
    private final String email;
    private final Perfil perfil;
    private final String versaoCredenciais;
    private String senha;

    private UsuarioAutenticado(Integer id, String nome, String email, Perfil perfil, String senha,
            String versaoCredenciais) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.senha = senha;
        this.versaoCredenciais = versaoCredenciais;
    }

    public static UsuarioAutenticado from(Funcionario funcionario) {
        return new UsuarioAutenticado(
                funcionario.getId(),
                funcionario.getPrimeiroNome() + " " + funcionario.getSobrenome(),
                funcionario.getEmail(),
                funcionario.getPerfil(),
                funcionario.getSenha(),
                versaoDasCredenciais(funcionario.getPerfil(), funcionario.getSenha()));
    }

    // Muda quando o perfil ou a senha mudam. É um hash do hash BCrypt: dá para comparar, mas
    // não revela a senha nem o hash, então pode ficar na sessão.
    public static String versaoDasCredenciais(Perfil perfil, String senhaHash) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256")
                    .digest((perfil.name() + ":" + senhaHash).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e); // todo JDK tem
        }
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

    public String getVersaoCredenciais() {
        return versaoCredenciais;
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
