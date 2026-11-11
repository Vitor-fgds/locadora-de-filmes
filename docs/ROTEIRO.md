# Roteiro do trabalho — Banco de Dados II (2026/2)

Transcrição do roteiro oficial, para referência do Claude Code e do grupo.

## Apresentação (01 e 02/12/2026)

- **1a** Apresentar os requisitos funcionais do sistema → `docs/REQUISITOS.md`
- **1b** Demonstrar o sistema em execução
- **1c** Explicitar no código:
  - trechos de **conexão** com a base de dados
  - trechos de **chamadas de consultas e stored procedures**

## Requisitos do código SQL

- **2a** Usar **transações** nas funcionalidades compostas por mais de 1 comando
- **2b** Pelo menos uma consulta com **junção**
- **2c** Pelo menos uma funcionalidade com chamada a **stored procedure** com pelo menos 1 parâmetro
- **2d** Uso de todos os comandos básicos: **SELECT, UPDATE, DELETE, INSERT**

## Outras exigências

- Entregar código da aplicação **e das estruturas do banco** no Classroom, compactado,
  com o nome do grupo.
- O sistema deve manipular **mais de uma estrutura de armazenamento** (mais de 1 tabela).
- Grupos que usam frameworks que mapeiam SQL implicitamente devem **indicar os comandos
  do framework que realizam esse mapeamento** → marcas `[ROTEIRO-FW-MAPEAMENTO]` e a
  tabela "Mapa de abstrações" em `docs/BANCO_DE_DADOS.md`.
