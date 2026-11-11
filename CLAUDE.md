# Locadora de Filmes — Trabalho de Banco de Dados II (UFVJM 2026/2)

Sistema web de locadora: React (frontend) + Spring Boot (API REST) + PostgreSQL (Docker).
Objetivo acadêmico: demonstrar programação em banco de dados (conexão, consultas,
junções, transações, stored procedure, CRUD) conforme `docs/ROTEIRO.md`.
Apresentação: 01–02/12/2026. Escopo pequeno, código robusto.

## Estrutura

```
/                     docker-compose.yml, .env.example, README.md (índice)
/backend              Spring Boot (Maven wrapper)
/frontend             React + Vite + TypeScript
/docs                 PLANO.md, REQUISITOS.md, BANCO_DE_DADOS.md, ROTEIRO.md
```

## Stack (fixa — não trocar sem pedir)

- Java 25 (LTS), Spring Boot 4.1.x, Maven wrapper — todo o grupo usa JDK 25
- Spring Web, Spring Data JPA, Spring Security, Bean Validation, Flyway
  (`spring-boot-starter-flyway` + `org.flywaydb:flyway-database-postgresql`)
- PostgreSQL 17 (`postgres:17-alpine`) — **só o banco** roda em Docker
- Testes: JUnit 5, MockMvc, Testcontainers (PostgreSQL) com `@ServiceConnection`
- Frontend: React 19, Vite, TypeScript strict, React Router, TanStack Query,
  react-hook-form + zod, CSS Modules com design tokens (variáveis CSS)
- Em dúvida sobre API de versão nova (Boot 4, Security 7, React 19): consulte o Context7
  antes de escrever código. Não use APIs depreciadas.

## Regras de domínio

- O modelo vem do trabalho de BD1. `V1` reproduz o script original **sem alterações**;
  toda mudança de schema é uma nova migration (`V2`, `V3`…). Nunca editar migration aplicada.
- `exemplar_filme.status`: `TRUE` = alugado, `FALSE` = disponível (semântica do BD1).
  No Java, mapear como `boolean alugado` com `@Column(name = "status")`.
- Login = `funcionario.email` + `funcionario.senha` (hash BCrypt). Perfis: `ADMIN`, `ATENDENTE`.
- Aluguel: `@Transactional` no Java (lock no exemplar → INSERT aluguel → UPDATE exemplar).
- Devolução: stored procedure `sp_registrar_devolucao` (parâmetros, multa, transação no banco).

## Convenções obrigatórias

- **Comentários do roteiro**: todo trecho ligado a um item do roteiro recebe a marca
  `[ROTEIRO-xx]` (ver skill `roteiro-bd2`). `grep -rn "\[ROTEIRO" .` deve listar tudo.
- **Documentação viva**: qualquer tarefa que crie/altere acesso a dados (migration,
  repository, query, procedure, transação, conexão) atualiza `docs/BANCO_DE_DADOS.md`
  na mesma tarefa. Sem exceção.
- Entidades JPA nunca saem do service: a API recebe e devolve **DTOs (records)**.
- Seguir a skill `spring-api-padroes` no backend e `readme-didatico` nos READMEs.
- Segredos só via variáveis de ambiente / `.env` (no `.gitignore`). Nada de senha em código.

## Comandos

```bash
docker compose up -d                       # sobe o PostgreSQL
cd backend && ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev
cd backend && ./mvnw -q verify             # testes (Testcontainers exige Docker)
cd frontend && npm run dev                 # Vite com proxy /api -> :8080
cd frontend && npm run build && npm run lint
```

## Economia de tokens (seguir sempre)

- Respostas curtas. Não repita código já escrito nem faça resumos longos ao final.
- Navegue com LSP (definição/referências) e `grep` antes de ler arquivos inteiros.
- Builds e testes em modo silencioso (`-q`); mostre só as falhas relevantes.
- Não crie arquivos fora do pedido (nada de `NOTES.md`, `SUMMARY.md` etc.).
- Ao fim de cada fase, marque o checkbox correspondente em `docs/PLANO.md`.
