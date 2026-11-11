package br.edu.ufvjm.locadora;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.ufvjm.locadora.ator.Ator;
import br.edu.ufvjm.locadora.ator.AtorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// [ROTEIRO-1c-CONEXAO] Sobe um Postgres 17 efêmero via Testcontainers e, com
// @ServiceConnection (em TestcontainersConfiguration), substitui o DataSource real só
// para este teste — prova que V1/V2/V3 aplicam limpo contra um Postgres real. Sem
// @ActiveProfiles: roda no perfil padrão, só classpath:db/migration, sem o seed dev.
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LocadoraApplicationTests {

	@Autowired
	private AtorRepository atorRepository;

	@Test
	void contextCarregaEMigrationsAplicamSemSeed() {
		assertThat(atorRepository.count()).isZero();
	}

	@Test
	void persisteEConsultaAtor() {
		Ator ator = new Ator();
		ator.setPrimeiroNome("Teste");
		ator.setSobrenome("Integração");

		Ator salvo = atorRepository.save(ator);

		assertThat(salvo.getId()).isNotNull();
		assertThat(atorRepository.findById(salvo.getId())).isPresent();
	}

}
