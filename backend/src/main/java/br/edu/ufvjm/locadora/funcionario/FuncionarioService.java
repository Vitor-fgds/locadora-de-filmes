package br.edu.ufvjm.locadora.funcionario;

import br.edu.ufvjm.locadora.common.error.RegraNegocioException;
import br.edu.ufvjm.locadora.funcionario.dto.FuncionarioCreateRequest;
import br.edu.ufvjm.locadora.funcionario.dto.FuncionarioResponse;
import br.edu.ufvjm.locadora.funcionario.dto.FuncionarioUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF06. A senha é gravada como hash BCrypt (feito no Java, não no banco). O ADMIN logado
// não pode excluir a si mesmo nem rebaixar o próprio perfil (evita ficar sem acesso).
@Service
@Transactional(readOnly = true)
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, PasswordEncoder passwordEncoder) {
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<FuncionarioResponse> listar(Pageable pageable) {
        return funcionarioRepository.findAll(pageable).map(FuncionarioResponse::from);
    }

    public FuncionarioResponse buscar(Integer id) {
        return FuncionarioResponse.from(obter(id));
    }

    @Transactional
    public FuncionarioResponse criar(FuncionarioCreateRequest req) {
        Funcionario f = new Funcionario();
        f.setPrimeiroNome(req.primeiroNome().trim());
        f.setSobrenome(req.sobrenome().trim());
        f.setRua(req.rua().trim());
        f.setBairro(req.bairro().trim());
        f.setCidade(req.cidade().trim());
        f.setEmail(req.email().trim().toLowerCase());
        f.setTelefone(req.telefone());
        f.setSenha(passwordEncoder.encode(req.senha()));
        f.setPerfil(req.perfil());
        return FuncionarioResponse.from(funcionarioRepository.save(f));
    }

    @Transactional
    public FuncionarioResponse atualizar(Integer id, FuncionarioUpdateRequest req, Integer idLogado) {
        Funcionario f = obter(id);
        if (id.equals(idLogado) && req.perfil() != f.getPerfil()) {
            throw new RegraNegocioException("Você não pode alterar o seu próprio perfil.");
        }
        f.setPrimeiroNome(req.primeiroNome().trim());
        f.setSobrenome(req.sobrenome().trim());
        f.setRua(req.rua().trim());
        f.setBairro(req.bairro().trim());
        f.setCidade(req.cidade().trim());
        f.setEmail(req.email().trim().toLowerCase());
        f.setTelefone(req.telefone());
        f.setPerfil(req.perfil());
        if (req.senha() != null) {
            f.setSenha(passwordEncoder.encode(req.senha()));
        }
        return FuncionarioResponse.from(f); // dirty checking: UPDATE funcionario no commit
    }

    @Transactional
    public void excluir(Integer id, Integer idLogado) {
        if (id.equals(idLogado)) {
            throw new RegraNegocioException("Você não pode excluir a sua própria conta.");
        }
        // Funcionário com aluguéis registrados: a FK fk_aluguel_funcionario recusa → 409 (RN03).
        funcionarioRepository.delete(obter(id));
    }

    private Funcionario obter(Integer id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário " + id + " não encontrado"));
    }
}
