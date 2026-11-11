-- =====================================================================
-- V3__procedures.sql
-- [ROTEIRO-1c-PROCEDURE][ROTEIRO-2c-PROCEDURE] RF08 — devolução de aluguel.
-- Chamada futura (Fase 4b): CALL sp_registrar_devolucao(?, ?, ?) via JDBC
-- {call ...}, com escapeSyntaxCallMode=callIfNoReturn na URL do datasource
-- (ver .env.example e skill roteiro-bd2, seção "pegadinhas").
-- =====================================================================
CREATE OR REPLACE PROCEDURE sp_registrar_devolucao(
    IN    p_id_aluguel     INT,
    IN    p_data_devolucao DATE,
    INOUT p_valor_multa    NUMERIC
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_aluguel     RECORD;
    v_dias_atraso INT;
BEGIN
    -- [ROTEIRO-2a-TRANSACAO][ROTEIRO-2d-SELECT] Trava a linha do aluguel: evita
    -- que duas devoluções concorrentes do mesmo id_aluguel calculem multa duas vezes.
    SELECT id_aluguel, id_exemplar, data_aluguel, data_entrega, data_devolucao
      INTO v_aluguel
      FROM aluguel
     WHERE id_aluguel = p_id_aluguel
     FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Aluguel % não encontrado', p_id_aluguel
            USING ERRCODE = 'P0001';
    END IF;

    IF v_aluguel.data_devolucao IS NOT NULL THEN
        RAISE EXCEPTION 'Aluguel % já foi devolvido em %', p_id_aluguel, v_aluguel.data_devolucao
            USING ERRCODE = 'P0001';
    END IF;

    IF p_data_devolucao < v_aluguel.data_aluguel THEN
        RAISE EXCEPTION 'Data de devolução % é anterior à data do aluguel %',
            p_data_devolucao, v_aluguel.data_aluguel
            USING ERRCODE = 'P0001';
    END IF;

    -- RN02: multa = dias de atraso x R$ 2,00, mínimo 0.
    v_dias_atraso := GREATEST(0, p_data_devolucao - v_aluguel.data_entrega);
    p_valor_multa := v_dias_atraso * 2.00;

    -- [ROTEIRO-2d-UPDATE] Grava devolução e multa.
    UPDATE aluguel
       SET data_devolucao = p_data_devolucao,
           valor_multa    = p_valor_multa
     WHERE id_aluguel = p_id_aluguel;

    -- [ROTEIRO-2d-UPDATE] Libera o exemplar (RN01).
    UPDATE exemplar_filme
       SET status = FALSE
     WHERE id_exemplar = v_aluguel.id_exemplar;

    -- Sem COMMIT: a procedure roda atômica dentro da transação do chamador
    -- (futuro @Transactional no service) e também funciona isolada via
    -- CALL direto no psql, que autocommita cada statement de topo por padrão.
END;
$$;
