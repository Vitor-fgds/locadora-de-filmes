package br.edu.ufvjm.locadora.security.dto;

import br.edu.ufvjm.locadora.funcionario.Perfil;
import br.edu.ufvjm.locadora.security.UsuarioAutenticado;

// Dados do funcionário logado devolvidos pelo login e por /api/auth/me. Sem senha.
public record UsuarioResponse(Integer id, String nome, String email, Perfil perfil) {

    public static UsuarioResponse from(UsuarioAutenticado usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
