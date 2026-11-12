package br.edu.ufvjm.locadora.support;

import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

// Cria registros direto no banco (SQL puro), sem passar pela API: prepara o cenário de cada
// teste sem depender do código que está sendo testado. Todas as senhas são SENHA.
public class DadosDeTeste {

    public static final String SENHA = "senha-de-teste";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public DadosDeTeste(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    public int criaFuncionario(String email, String perfil) {
        return jdbc.queryForObject("""
                INSERT INTO funcionario (primeiro_nome, sobrenome, rua, bairro, cidade, email, telefone, senha, perfil)
                VALUES ('Teste', ?, 'Rua A', 'Centro', 'Diamantina', ?, '38999999999', ?, ?)
                RETURNING id_funcionario""", Integer.class, perfil, email, passwordEncoder.encode(SENHA), perfil);
    }

    public int criaCliente(String email) {
        return jdbc.queryForObject("""
                INSERT INTO cliente (primeiro_nome, sobrenome, rua, bairro, cidade, email, telefone)
                VALUES ('Cli', 'Ente', 'Rua A', 'Centro', 'Diamantina', ?, '38999999999')
                RETURNING id_cliente""", Integer.class, email);
    }

    public int criaFilme() {
        return criaFilme("Filme");
    }

    public int criaFilme(String nome) {
        return jdbc.queryForObject("""
                INSERT INTO filme (nome, descricao, duracao_minutos, data_lancamento)
                VALUES (?, 'Descrição', 100, '2000-01-01') RETURNING id_filme""", Integer.class, nome);
    }

    public int criaAtor() {
        return jdbc.queryForObject(
                "INSERT INTO ator (primeiro_nome, sobrenome) VALUES ('Ator', 'Teste') RETURNING id_ator", Integer.class);
    }

    public int criaExemplar(int idFilme, boolean alugado) {
        return jdbc.queryForObject(
                "INSERT INTO exemplar_filme (id_filme, status) VALUES (?, ?) RETURNING id_exemplar",
                Integer.class, idFilme, alugado);
    }

    public void vincula(int idFilme, int idAtor) {
        jdbc.update("INSERT INTO filme_ator (id_filme, id_ator) VALUES (?, ?)", idFilme, idAtor);
    }

    // Aluguel em aberto (sem devolução); não mexe no status do exemplar.
    public int criaAluguel(int idExemplar, int idCliente, int idFuncionario, LocalDate dataAluguel,
            LocalDate dataEntrega, double valor) {
        return jdbc.queryForObject("""
                INSERT INTO aluguel (id_exemplar, id_cliente, id_funcionario, data_aluguel, data_entrega, valor_aluguel)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING id_aluguel""", Integer.class,
                idExemplar, idCliente, idFuncionario, dataAluguel, dataEntrega, valor);
    }
}
