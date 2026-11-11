-- ==================================================
-- SCRIPT ORIGINAL DO TRABALHO DE BD1 (Locadora de Filmes)
-- Autores: Catarina Cotta Lannes, Isabella Baracho Motta,
--          Vitor Rocha Fagundes, João Marcos Campelo
-- Fonte: Trabalho_BD_1.pdf, seção 5. Não editar: base da migration V1.
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

-- ==================================================
-- DADOS DE EXEMPLO (vão para o seed do perfil dev, com senhas em BCrypt)
-- ==================================================
INSERT INTO ator (primeiro_nome, sobrenome) VALUES
    ('Tom', 'Hanks'), ('Leonardo', 'DiCaprio'), ('Keanu', 'Reeves'),
    ('Scarlett', 'Johansson'), ('Robert', 'Downey Jr');

INSERT INTO cliente (primeiro_nome, sobrenome, rua, bairro, cidade, email, telefone) VALUES
    ('Vitor', 'Rocha', 'Rua das Palmeiras', 'Centro', 'Teofilo Otoni', 'vitor@gmail.com', '33999990001'),
    ('Arthur', 'Silva', 'Rua das Flores', 'Bela Vista', 'Diamantina', 'arthur@gmail.com', '33999990002'),
    ('Gabriel', 'Oliveira', 'Rua A', 'Jardim', 'Mucuri', 'gabriel@gmail.com', '33999990003'),
    ('Maria', 'Souza', 'Rua B', 'Centro', 'Mucuri', 'maria@gmail.com', '33999990004'),
    ('Ana', 'Costa', 'Rua C', 'Industrial', 'Diamantina', 'ana@gmail.com', '33999990005');

-- Senhas originais em texto puro (admin123 / func123): o seed deve gravá-las em BCrypt.
INSERT INTO funcionario (primeiro_nome, sobrenome, rua, bairro, cidade, email, telefone, senha) VALUES
    ('Carlos', 'Ferreira', 'Rua Central', 'Centro', 'Mucuri', 'carlos@locadora.com', '33988880001', 'admin123'),
    ('Fernanda', 'Lima', 'Rua Nova', 'Industrial', 'Mucuri', 'fernanda@locadora.com', '33988880002', 'func123');

INSERT INTO filme (nome, descricao, duracao_minutos, data_lancamento) VALUES
    ('Forrest Gump', 'Drama sobre a vida de um homem que presencia eventos historicos', 142, '1994-07-06'),
    ('Titanic', 'Romance ambientado no naufragio do Titanic', 194, '1997-12-19'),
    ('Matrix', 'Filme de ficcao cientifica sobre simulacao', 136, '1999-03-31'),
    ('Vingadores Ultimato', 'Filme de super herois', 181, '2019-04-26');

INSERT INTO filme_ator (id_filme, id_ator) VALUES (1,1), (2,2), (3,3), (4,4), (4,5);

-- status: TRUE = alugado, FALSE = disponível (ver consulta 4)
INSERT INTO exemplar_filme (id_filme, status) VALUES
    (1, TRUE), (1, FALSE), (2, TRUE), (2, FALSE), (3, TRUE), (3, FALSE), (4, FALSE), (4, FALSE);

INSERT INTO aluguel (id_exemplar, id_cliente, id_funcionario, data_aluguel, data_entrega, valor_aluguel, valor_multa) VALUES
    (1, 1, 1, '2026-06-01', '2026-06-05', 12.90, 0.00),
    (3, 2, 2, '2026-06-08', '2026-06-12', 15.50, 3.00),
    (5, 3, 1, '2026-06-10', '2026-06-14', 10.00, 0.00);

-- ==================================================
-- CONSULTAS DO BD1 (candidatas a relatórios da API)
-- ==================================================
-- 1  Nome, email e telefone dos clientes
-- 2  Filmes lançados após 2000
-- 3  Filmes por duração (desc)
-- 4  Exemplares disponíveis (status = FALSE)
-- 5  Clientes e datas dos aluguéis (JOIN cliente-aluguel)
-- 6  Filme e status de cada exemplar (JOIN filme-exemplar)
-- 7  Aluguel, funcionário e cliente (JOIN triplo)
-- 8  Quantidade de exemplares por filme (JOIN + GROUP BY)
-- 9  Total arrecadado por funcionário (JOIN + SUM)
-- 10 Filmes com mais de um ator (JOIN + GROUP BY + HAVING)
