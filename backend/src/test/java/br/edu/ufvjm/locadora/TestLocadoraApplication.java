package br.edu.ufvjm.locadora;

import org.springframework.boot.SpringApplication;

public class TestLocadoraApplication {

	public static void main(String[] args) {
		SpringApplication.from(LocadoraApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
