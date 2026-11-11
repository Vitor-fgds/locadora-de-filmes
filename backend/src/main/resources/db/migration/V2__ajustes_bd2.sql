-- =====================================================================
-- V2__ajustes_bd2.sql — Ajustes do BD2 sobre o schema original do BD1.
-- V1 não é alterado (migration já aplicada); tudo aqui é incremental.
-- =====================================================================

-- 1) Login (RF01): senha passa a guardar hash BCrypt (60 caracteres) em vez de texto puro.
ALTER TABLE funcionario ALTER COLUMN senha TYPE VARCHAR(100);

-- 2) Perfis de acesso (RF06): ADMIN ou ATENDENTE, padrão ATENDENTE.
ALTER TABLE funcionario
    ADD COLUMN perfil VARCHAR(10) NOT NULL DEFAULT 'ATENDENTE'
    CONSTRAINT ck_funcionario_perfil CHECK (perfil IN ('ADMIN', 'ATENDENTE'));

-- 3) Devolução (RF08): preenchida pela stored procedure sp_registrar_devolucao;
--    NULL = aluguel ainda em aberto.
ALTER TABLE aluguel ADD COLUMN data_devolucao DATE NULL;

-- 4) RN04 — validações de data e valor.
ALTER TABLE aluguel
    ADD CONSTRAINT ck_aluguel_datas CHECK (data_entrega >= data_aluguel);
ALTER TABLE aluguel
    ADD CONSTRAINT ck_aluguel_devolucao_data
        CHECK (data_devolucao IS NULL OR data_devolucao >= data_aluguel);
ALTER TABLE aluguel
    ADD CONSTRAINT ck_aluguel_valor_aluguel CHECK (valor_aluguel >= 0);
ALTER TABLE aluguel
    ADD CONSTRAINT ck_aluguel_valor_multa CHECK (valor_multa IS NULL OR valor_multa >= 0);

-- 5) RN03 — só filme_ator (tabela puramente associativa) remove em cascata.
--    As demais 4 FKs continuam sem ON DELETE (RESTRICT padrão do PostgreSQL):
--    excluir cliente/funcionário/exemplar com aluguéis falha no banco (HTTP 409 na API).
ALTER TABLE filme_ator DROP CONSTRAINT fk_filme_ator_filme;
ALTER TABLE filme_ator
    ADD CONSTRAINT fk_filme_ator_filme
        FOREIGN KEY (id_filme) REFERENCES filme(id_filme) ON DELETE CASCADE;

ALTER TABLE filme_ator DROP CONSTRAINT fk_filme_ator_ator;
ALTER TABLE filme_ator
    ADD CONSTRAINT fk_filme_ator_ator
        FOREIGN KEY (id_ator) REFERENCES ator(id_ator) ON DELETE CASCADE;

-- 6) Índices nas FKs (PostgreSQL não cria automaticamente).
--    filme_ator.id_filme já é coberto pela PK composta (id_filme, id_ator) — um
--    índice B-tree atende buscas que começam pela primeira coluna da chave.
--    filme_ator.id_ator é a SEGUNDA coluna da PK: sozinho, não é usado por esse
--    índice composto, então precisa de um índice próprio para "filmes de um ator".
CREATE INDEX idx_filme_ator_ator ON filme_ator (id_ator);
CREATE INDEX idx_exemplar_filme_filme ON exemplar_filme (id_filme);
CREATE INDEX idx_aluguel_exemplar ON aluguel (id_exemplar);
CREATE INDEX idx_aluguel_cliente ON aluguel (id_cliente);
CREATE INDEX idx_aluguel_funcionario ON aluguel (id_funcionario);

-- 7) RN01 — documenta a semântica do booleano direto no catálogo do banco.
COMMENT ON COLUMN exemplar_filme.status IS 'TRUE = exemplar alugado; FALSE = disponível (RN01).';
