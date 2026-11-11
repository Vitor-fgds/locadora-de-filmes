-- ==================================================
-- V1__schema_bd1.sql
-- Script original do trabalho de BD1 (Locadora de Filmes) — somente DDL.
-- Fonte: docs/bd1/script_original.sql. Reproduzido sem alterações (ver CLAUDE.md:
-- "V1 reproduz o script original sem alterações"). O DML de exemplo foi movido
-- para db/seed/R__dados_demo.sql.
-- ==================================================

-- TABELA: ATOR
CREATE TABLE ator (
    id_ator INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    primeiro_nome VARCHAR(50) NOT NULL,
    sobrenome VARCHAR(50) NOT NULL
);

-- TABELA: CLIENTE
CREATE TABLE cliente (
    id_cliente INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    primeiro_nome VARCHAR(50) NOT NULL,
    sobrenome VARCHAR(50) NOT NULL,
    rua VARCHAR(50) NOT NULL,
    bairro VARCHAR(50) NOT NULL,
    cidade VARCHAR(50) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    telefone VARCHAR(11) NOT NULL
);

-- TABELA: FUNCIONARIO
CREATE TABLE funcionario (
    id_funcionario INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    primeiro_nome VARCHAR(50) NOT NULL,
    sobrenome VARCHAR(50) NOT NULL,
    rua VARCHAR(50) NOT NULL,
    bairro VARCHAR(50) NOT NULL,
    cidade VARCHAR(50) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    telefone VARCHAR(11) NOT NULL,
    senha VARCHAR(20) NOT NULL
);

-- TABELA: FILME
CREATE TABLE filme (
    id_filme INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    descricao TEXT NOT NULL,
    duracao_minutos INT NOT NULL,
    data_lancamento DATE NOT NULL
);

-- TABELA ASSOCIATIVA: FILME_ATOR
CREATE TABLE filme_ator (
    id_filme INT,
    id_ator INT,
    PRIMARY KEY (id_filme, id_ator)
);

-- TABELA: EXEMPLAR_FILME
CREATE TABLE exemplar_filme (
    id_exemplar INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_filme INT NOT NULL,
    status BOOLEAN NOT NULL
);

-- TABELA: ALUGUEL
CREATE TABLE aluguel (
    id_aluguel INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_exemplar INT NOT NULL,
    id_cliente INT NOT NULL,
    id_funcionario INT NOT NULL,
    data_aluguel DATE NOT NULL,
    data_entrega DATE NOT NULL,
    valor_aluguel DECIMAL(10,2) NOT NULL,
    valor_multa DECIMAL(10,2)
);

-- CHAVES ESTRANGEIRAS
ALTER TABLE filme_ator ADD CONSTRAINT fk_filme_ator_filme
    FOREIGN KEY (id_filme) REFERENCES filme(id_filme);
ALTER TABLE filme_ator ADD CONSTRAINT fk_filme_ator_ator
    FOREIGN KEY (id_ator) REFERENCES ator(id_ator);
ALTER TABLE exemplar_filme ADD CONSTRAINT fk_exemplar_filme
    FOREIGN KEY (id_filme) REFERENCES filme(id_filme);
ALTER TABLE aluguel ADD CONSTRAINT fk_aluguel_exemplar
    FOREIGN KEY (id_exemplar) REFERENCES exemplar_filme(id_exemplar);
ALTER TABLE aluguel ADD CONSTRAINT fk_aluguel_cliente
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente);
ALTER TABLE aluguel ADD CONSTRAINT fk_aluguel_funcionario
    FOREIGN KEY (id_funcionario) REFERENCES funcionario(id_funcionario);
