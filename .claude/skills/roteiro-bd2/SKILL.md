---
name: roteiro-bd2
description: Convenções do trabalho de BD2. Use SEMPRE que criar ou alterar migrations, SQL, stored procedures, repositories, queries JPA/JPQL/nativas, @Transactional, configuração de datasource ou docs/BANCO_DE_DADOS.md, para marcar o código com [ROTEIRO-xx] e manter a documentação viva atualizada.
---

# Roteiro BD2 — marcação no código e documentação viva

O professor vai pedir para **mostrar no código** cada requisito do roteiro. Este projeto
torna isso trivial: todo trecho relevante leva uma marca padronizada, e
`docs/BANCO_DE_DADOS.md` explica cada um.

## 1. Marcas no código

Formato (Java/TS: `//`; SQL: `--`; YAML: `#`):

```
// [ROTEIRO-<código>] <O QUE É> — <por que / qual comando SQL resulta>
```

| Código | Item do roteiro | Onde aparece |
|---|---|---|
| `1c-CONEXAO` | Conexão com a base | `application.yml` (datasource/Hikari), `docker-compose.yml`, config de Flyway |
| `1c-CONSULTA` | Chamada de consulta | métodos de repository com `@Query` ou query derivada usados por funcionalidades |
| `1c-PROCEDURE` / `2c-PROCEDURE` | Stored procedure com parâmetro | `V3__procedures.sql` e o código Java que chama `CALL` |
| `2a-TRANSACAO` | Transação em funcionalidade com >1 comando | `@Transactional` do aluguel; corpo da procedure |
| `2b-JUNCAO` | Consulta com JOIN | queries JPQL/nativas com `JOIN` |
| `2d-SELECT` / `2d-INSERT` / `2d-UPDATE` / `2d-DELETE` | Comandos básicos | o ponto do service que dispara cada comando |
| `FW-MAPEAMENTO` | Comando do framework que gera SQL implícito | `save()`, `findById()`, `deleteById()`, dirty checking, queries derivadas |

Exemplos:

```java
// [ROTEIRO-2a-TRANSACAO] Registrar aluguel = 3 comandos atômicos:
//   SELECT ... FOR UPDATE (exemplar) → INSERT aluguel → UPDATE exemplar_filme.status
//   Se qualquer passo falhar, o Spring faz ROLLBACK de todos.
@Transactional
public AluguelResponse registrar(AluguelRequest req) { ... }
```

```java
// [ROTEIRO-FW-MAPEAMENTO][ROTEIRO-2d-INSERT] repository.save(novo) com id nulo
//   → Hibernate executa: INSERT INTO cliente (primeiro_nome, ...) VALUES (?, ...)
clienteRepository.save(novo);
```

```sql
-- [ROTEIRO-2c-PROCEDURE][ROTEIRO-2a-TRANSACAO] Recebe p_id_aluguel e p_data_devolucao.
-- Executa SELECT ... FOR UPDATE, UPDATE aluguel e UPDATE exemplar_filme na mesma transação.
CREATE OR REPLACE PROCEDURE sp_registrar_devolucao(...)
```

Regras: uma marca por trecho (não comente cada linha); comentário explica **o SQL que
resulta**, não o óbvio do Java; não marque código que não toca o banco.

## 2. Documentação viva — `docs/BANCO_DE_DADOS.md`

Ao terminar qualquer tarefa que toque acesso a dados, atualize a seção correspondente:

- **Conexão**: cadeia completa `docker-compose` → `.env` → `application.yml` → HikariCP → Flyway.
- **Migrations**: tabela `versão | arquivo | o que faz`.
- **Mapa de abstrações** (seção mais importante): tabela
  `Método/anotação no Java | Arquivo:linha | SQL efetivamente executado | Item do roteiro`.
  Copie o SQL real do log (`logging.level.org.hibernate.SQL=DEBUG` no perfil dev),
  não invente.
- **Transações**: para cada uma, sequência de comandos, ponto de COMMIT/ROLLBACK e o
  que acontece numa falha (ex.: exemplar já alugado → exceção → rollback).
- **Stored procedure**: assinatura, parâmetros, regra de multa, como o Java a chama e por quê.
- **Junções**: SQL, tabelas envolvidas, endpoint que usa.
- **Checklist do roteiro**: cada item com link para arquivo e marca.

Estilo: português claro e didático, frases curtas, um diagrama Mermaid quando ajudar
(fluxo de uma transação, ER). Nada de texto genérico sobre o que é SQL.

## 3. Pegadinhas conhecidas (PostgreSQL + JDBC)

- Procedures do PostgreSQL são chamadas com `CALL`. Com `{call ...}` do JDBC, adicione
  `escapeSyntaxCallMode=callIfNoReturn` na URL do datasource, ou o driver tenta `SELECT`
  de função. Documente essa escolha.
- Dentro de uma procedure chamada pelo Spring (já em transação), **não** use `COMMIT`:
  a procedure roda atômica dentro da transação do chamador. Erros de regra:
  `RAISE EXCEPTION ... USING ERRCODE = 'P0001'` e o Java converte em HTTP 422.
- O PostgreSQL não cria índice automático em FK: as migrations devem criá-los.
