package br.edu.ufvjm.locadora.funcionario;

// Projeção por interface: só as duas colunas que a revalidação da sessão precisa
// (perfil e hash da senha), sem carregar a entidade inteira.
public interface FuncionarioCredenciais {

    Perfil getPerfil();

    String getSenha();
}
