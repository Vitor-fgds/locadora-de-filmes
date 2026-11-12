package br.edu.ufvjm.locadora;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	// [ROTEIRO-1c-CONEXAO] Mesma versão do docker-compose.yml (postgres:17-alpine) —
	// prova que V1/V2/V3 aplicam limpo contra o mesmo Postgres usado em dev/produção.
	// escapeSyntaxCallMode=callIfNoReturn: igual à DB_URL do .env, necessário para o
	// {call sp_registrar_devolucao(...)} do JDBC virar CALL (e não SELECT de função).
	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
				.withUrlParam("escapeSyntaxCallMode", "callIfNoReturn");
	}

}
