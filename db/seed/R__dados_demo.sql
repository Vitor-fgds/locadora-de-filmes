-- =====================================================================
-- R__dados_demo.sql — Dados de demonstração (Flyway repeatable, SÓ perfil dev).
-- Nunca deve ser aplicado em produção: na Fase 2, flyway.locations do perfil
-- dev inclui este diretório; o perfil de produção não inclui.
--
-- Idempotente: TRUNCATE ... RESTART IDENTITY CASCADE no início garante que
-- rodar este script várias vezes (Flyway reaplica repeatable quando o
-- checksum muda) nunca duplica dados nem deixa ids desencontrados.
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

TRUNCATE TABLE aluguel, exemplar_filme, filme_ator, filme, cliente, funcionario, ator
    RESTART IDENTITY CASCADE;

INSERT INTO ator (primeiro_nome, sobrenome) VALUES
    ('Tom', 'Hanks'), ('Leonardo', 'DiCaprio'), ('Keanu', 'Reeves'),
    ('Scarlett', 'Johansson'), ('Robert', 'Downey Jr');

INSERT INTO cliente (primeiro_nome, sobrenome, rua, bairro, cidade, email, telefone) VALUES
    ('Vitor', 'Rocha', 'Rua das Palmeiras', 'Centro', 'Teofilo Otoni', 'vitor@gmail.com', '33999990001'),
    ('Arthur', 'Silva', 'Rua das Flores', 'Bela Vista', 'Diamantina', 'arthur@gmail.com', '33999990002'),
    ('Gabriel', 'Oliveira', 'Rua A', 'Jardim', 'Mucuri', 'gabriel@gmail.com', '33999990003'),
    ('Maria', 'Souza', 'Rua B', 'Centro', 'Mucuri', 'maria@gmail.com', '33999990004'),
    ('Ana', 'Costa', 'Rua C', 'Industrial', 'Diamantina', 'ana@gmail.com', '33999990005');

-- Senhas em texto puro só nos comentários (admin123 / func123); gravadas em
-- BCrypt via pgcrypto (prefixo $2a$, compatível com Spring Security BCryptPasswordEncoder).
INSERT INTO funcionario (primeiro_nome, sobrenome, rua, bairro, cidade, email, telefone, senha, perfil) VALUES
    ('Carlos', 'Ferreira', 'Rua Central', 'Centro', 'Mucuri', 'carlos@locadora.com', '33988880001',
        crypt('admin123', gen_salt('bf')), 'ADMIN'),
    ('Fernanda', 'Lima', 'Rua Nova', 'Industrial', 'Mucuri', 'fernanda@locadora.com', '33988880002',
        crypt('func123', gen_salt('bf')), 'ATENDENTE');

INSERT INTO filme (nome, descricao, duracao_minutos, data_lancamento) VALUES
    ('Forrest Gump', 'Drama sobre a vida de um homem que presencia eventos historicos', 142, '1994-07-06'),
    ('Titanic', 'Romance ambientado no naufragio do Titanic', 194, '1997-12-19'),
    ('Matrix', 'Filme de ficcao cientifica sobre simulacao', 136, '1999-03-31'),
    ('Vingadores Ultimato', 'Filme de super herois', 181, '2019-04-26');

INSERT INTO filme_ator (id_filme, id_ator) VALUES (1,1), (2,2), (3,3), (4,4), (4,5);

-- status: TRUE = alugado, FALSE = disponível (RN01)
INSERT INTO exemplar_filme (id_filme, status) VALUES
    (1, TRUE), (1, FALSE), (2, TRUE), (2, FALSE), (3, TRUE), (3, FALSE), (4, FALSE), (4, FALSE);

INSERT INTO aluguel (id_exemplar, id_cliente, id_funcionario, data_aluguel, data_entrega, valor_aluguel, valor_multa) VALUES
    (1, 1, 1, '2026-06-01', '2026-06-05', 12.90, 0.00),
    (3, 2, 2, '2026-06-08', '2026-06-12', 15.50, 3.00),
    (5, 3, 1, '2026-06-10', '2026-06-14', 10.00, 0.00);
