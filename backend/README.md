# Locadora de Filmes — Backend

API REST da locadora: Spring Boot 4 + PostgreSQL, com migrations Flyway, sessão em cookie e
proteção CSRF.

![Java](https://img.shields.io/badge/Java-25-ed8b00?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6db33f?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?style=flat-square)
![Flyway](https://img.shields.io/badge/Flyway-migrations-cc0200?style=flat-square)

## Sumário

- [Visão geral](#visão-geral)
- [Pré-requisitos](#pré-requisitos)
- [Início rápido](#início-rápido)
- [Configuração](#configuração)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Endpoints](#endpoints)
- [Banco de dados](#banco-de-dados)
- [Testes](#testes)
- [Solução de problemas](#solução-de-problemas)
- [Autores](#autores)

## Visão geral

- Camadas por assunto: `Controller` (HTTP) → `Service` (regras e transação) → `Repository`
  (Spring Data JPA). Entidades JPA **nunca** saem do service: a API recebe e devolve DTOs
  (`record`).
- Schema versionado pelo Flyway; o Hibernate só confere (`ddl-auto=validate`).
- Aluguel: `@Transactional` com lock pessimista no exemplar. Devolução: stored procedure
  `sp_registrar_devolucao` (multa calculada no banco).
- Erros em `ProblemDetail` (RFC 9457): 404, 409 (violação de FK/e-mail repetido), 422 (regra
  de negócio), 403 (perfil ou CSRF), 401 (sem sessão).
- Segurança: login por e-mail e senha (BCrypt), sessão em cookie `HttpOnly`/`SameSite=Lax`,
  token CSRF, perfil revalidado no banco a cada requisição autenticada.

```mermaid
flowchart LR
    A[Cliente HTTP] --> B[Controller<br/>DTO + validação]
    B --> C[Service<br/>@Transactional]
    C --> D[Repository<br/>JPA / JPQL / SQL nativo]
    D --> E[(PostgreSQL)]
```

## Pré-requisitos

| Ferramenta | Versão | Como verificar |
|---|---|---|
| JDK | 25 | `java -version` |
| Docker (com Compose v2) | 24 ou mais novo | `docker compose version` |

O Maven vem no projeto (`./mvnw`, no Windows `.\mvnw.cmd`).

## Início rápido

1. Na raiz do repositório, crie o `.env` (`cp .env.example .env`) e escreva `DB_PASSWORD`.
2. Suba o banco:

   ```bash
   docker compose up -d
   ```

   Você deve ver `locadora-postgres` com status `healthy` em `docker compose ps`.

3. Carregue o `.env` no terminal e inicie a API **de dentro de `backend/`**:

   ```bash
   set -a; . ./.env; set +a
   cd backend
   ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   ```powershell
   Get-Content .env | Where-Object { $_ -match '^\s*[^#\s][^=]*=' } | ForEach-Object {
     $k, $v = $_ -split '=', 2; Set-Item "Env:$($k.Trim())" $v
   }
   cd backend
   .\mvnw.cmd -q spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

   Você deve ver `Started LocadoraApplication` e, no log, o SQL gerado pelo Hibernate.

4. Confira:

   ```bash
   curl http://localhost:8080/actuator/health
   ```

   Você deve ver `{"groups":["liveness","readiness"],"status":"UP"}`.

Para rodar tudo (API + frontend + banco) só com Docker, veja o [README da raiz](../README.md).

## Configuração

| Nome | Para que serve | Exemplo |
|---|---|---|
| `DB_URL` | URL JDBC (**sem valor padrão**: se faltar, a API não inicia) | `jdbc:postgresql://localhost:55432/locadora?escapeSyntaxCallMode=callIfNoReturn` |
| `DB_USER` | Usuário do banco | `locadora` |
| `DB_PASSWORD` | Senha do banco | `uma-senha-sua` |

Perfis Spring:

| Perfil | Quando | O que muda |
|---|---|---|
| padrão | produção / CI | só as migrations do jar; cookie de sessão `Secure` (exige HTTPS); sem log de SQL |
| `dev` | desenvolvimento e container Docker | acrescenta o seed `../db/seed/R__dados_demo.sql`; cookie sem `Secure`; loga SQL e parâmetros |

> [!WARNING]
> `escapeSyntaxCallMode=callIfNoReturn` na URL é obrigatório: sem ele o driver traduz
> `{call sp_registrar_devolucao(...)}` para um `SELECT` e a devolução falha.

## Estrutura de pastas

```text
src/main/java/br/edu/ufvjm/locadora/
├── aluguel/       aluguel, devolução (procedure), histórico
├── ator/ cliente/ filme/ exemplar/ funcionario/   um pacote por assunto:
│                  Entidade, Repository, Service, Controller e dto/
├── relatorio/     faturamento, exemplares por filme, filmes com mais de um ator
├── security/      login, sessão, CSRF, @PreAuthorize por perfil, revalidação da sessão
├── common/error/  GlobalExceptionHandler (ProblemDetail) e RegraNegocioException
└── config/        SpaConfig (serve o frontend embutido no jar, só no modo Docker)
src/main/resources/
├── application.yml, application-dev.yml
└── db/migration/  V1__schema_bd1, V2__ajustes_bd2, V3__procedures
src/test/          testes de integração (Testcontainers)
```

## Endpoints

Todas as rotas `/api/**` exigem sessão, exceto `GET /api/auth/csrf` e `POST /api/auth/login`.
Os métodos que alteram dados (`POST`, `PUT`, `DELETE`) exigem o header `X-XSRF-TOKEN`
(valor do cookie `XSRF-TOKEN`). O perfil **ADMIN** pode tudo que o **ATENDENTE** pode.
Listas aceitam `?page=`, `?size=` (máximo 50) e, nos cadastros, `?nome=` para busca.

| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/auth/csrf` | público | Entrega o cookie `XSRF-TOKEN` (204) |
| POST | `/api/auth/login` | público | Entra com `email` e `senha` |
| GET | `/api/auth/me` | autenticado | Dados do usuário logado |
| POST | `/api/auth/logout` | autenticado | Encerra a sessão (204) |
| GET | `/api/filmes`, `/api/filmes/{id}` | ATENDENTE | Lista (com busca) e detalhe do filme |
| POST, PUT, DELETE | `/api/filmes`, `/api/filmes/{id}` | ADMIN | Cadastra, altera e exclui filme |
| GET | `/api/filmes/{id}/atores` | ATENDENTE | Elenco (junção `filme_ator` × `ator`) |
| POST, DELETE | `/api/filmes/{id}/atores/{idAtor}` | ADMIN | Vincula e desvincula ator |
| GET | `/api/filmes/{idFilme}/exemplares` | ATENDENTE | Exemplares do filme |
| GET | `/api/filmes/{idFilme}/exemplares/disponiveis` | ATENDENTE | Exemplares disponíveis (junção) |
| POST | `/api/filmes/{idFilme}/exemplares` | ADMIN | Cadastra exemplar (nasce disponível) |
| PUT, DELETE | `/api/exemplares/{id}` | ADMIN | Altera e exclui exemplar |
| GET | `/api/atores`, `/api/atores/{id}` | ATENDENTE | Lista (com busca) e detalhe |
| POST, PUT, DELETE | `/api/atores`, `/api/atores/{id}` | ADMIN | Cadastra, altera e exclui ator |
| GET, POST | `/api/clientes` | ATENDENTE | Lista (com busca) e cadastra cliente |
| GET, PUT | `/api/clientes/{id}` | ATENDENTE | Detalhe e alteração |
| DELETE | `/api/clientes/{id}` | ADMIN | Exclui (409 se houver aluguéis) |
| GET, POST | `/api/funcionarios` | ADMIN | Lista (com busca) e cadastra funcionário |
| GET, PUT, DELETE | `/api/funcionarios/{id}` | ADMIN | Detalhe, alteração (perfil, senha) e exclusão |
| POST | `/api/alugueis` | ATENDENTE | Registra aluguel (transação; 422 se o exemplar já está alugado) |
| POST | `/api/alugueis/{id}/devolucao` | ATENDENTE | Devolve via stored procedure; corpo opcional `{"dataDevolucao":"AAAA-MM-DD"}`; devolve a multa |
| GET | `/api/alugueis` | ATENDENTE | Histórico (junção de 5 tabelas); filtros `idCliente`, `emAberto` |
| GET | `/api/alugueis/{id}` | ATENDENTE | Um aluguel |
| GET | `/api/relatorios/faturamento-funcionarios` | ADMIN | Faturamento por funcionário (SQL nativo) |
| GET | `/api/relatorios/exemplares-por-filme` | ADMIN | Total e disponíveis por filme (`LEFT JOIN`) |
| GET | `/api/relatorios/filmes-com-mais-de-um-ator` | ADMIN | `GROUP BY` + `HAVING` |
| GET | `/actuator/health` | público | Healthcheck do container |

Exemplo de login com `curl` (guarda o cookie de sessão em `jar.txt`):

```bash
curl -s -c jar.txt http://localhost:8080/api/auth/csrf
TOKEN=$(awk '$6=="XSRF-TOKEN"{print $7}' jar.txt)
curl -s -b jar.txt -c jar.txt -X POST http://localhost:8080/api/auth/login \
  -H "X-XSRF-TOKEN: $TOKEN" -H 'Content-Type: application/json' \
  -d '{"email":"carlos@locadora.com","senha":"admin123"}'
```

Você deve ver `{"id":1,"nome":"Carlos Ferreira","email":"carlos@locadora.com","perfil":"ADMIN"}`.
Depois do login, releia o `TOKEN` (`GET /api/auth/csrf`) antes de enviar `POST`/`PUT`/`DELETE`.

## Banco de dados

Tudo sobre conexão, migrations, o SQL que cada método Java gera, transações, a stored
procedure e as junções está em [`docs/BANCO_DE_DADOS.md`](../docs/BANCO_DE_DADOS.md), com o
[checklist do roteiro](../docs/BANCO_DE_DADOS.md#1-checklist-do-roteiro). Regras:

- Toda mudança de schema é uma **nova** migration (`V4`, …). Nunca edite uma já aplicada.
- `exemplar_filme.status`: `TRUE` = alugado, `FALSE` = disponível (semântica do BD1).
- Trechos do roteiro levam a marca `[ROTEIRO-xx]`: `git grep -n "\[ROTEIRO" -- backend`.

## Testes

```bash
cd backend
./mvnw -q verify
```

São testes de integração com PostgreSQL real via Testcontainers (`@ServiceConnection`): o
**Docker precisa estar rodando**. Cobrem login e perfis, CRUDs, aluguel (rollback e aluguéis
simultâneos do mesmo exemplar), devolução com multa e o fallback do frontend.

## Solução de problemas

<details>
<summary>A API não inicia: "Could not resolve placeholder 'DB_URL'" (ou `DB_USER`, `DB_PASSWORD`)</summary>

As variáveis do `.env` não chegaram ao processo Java. Carregue-as no mesmo terminal em que
roda o `mvnw` (passo 3 do início rápido) ou configure-as na run configuration da IDE.
</details>

<details>
<summary>"Connection refused" ao conectar no banco</summary>

O PostgreSQL não está de pé ou a porta é outra. Rode `docker compose up -d` e confira que
`DB_URL` aponta para `localhost:55432`.
</details>

<details>
<summary>Flyway: "Migration checksum mismatch"</summary>

Uma migration já aplicada foi editada. Desfaça a edição e crie uma migration nova. Para
recomeçar do zero em desenvolvimento (**apaga os dados**): `docker compose down -v` e
`docker compose up -d`.
</details>

<details>
<summary>Usuários de demonstração ausentes (login não funciona)</summary>

O seed só roda no perfil `dev` e é lido de `../db/seed`, relativo ao diretório de trabalho.
Inicie o `mvnw` dentro de `backend/` e passe `-Dspring-boot.run.profiles=dev`.
</details>

<details>
<summary>Devolução falha com erro de SQL (500) ao chamar `sp_registrar_devolucao`</summary>

Confira se a `DB_URL` tem `escapeSyntaxCallMode=callIfNoReturn`. Copie a URL do `.env.example`.
</details>

<details>
<summary>403 em POST/PUT/DELETE</summary>

Falta o header `X-XSRF-TOKEN` ou o token está velho (ele muda depois do login). Chame
`GET /api/auth/csrf` de novo e reenvie o valor do cookie `XSRF-TOKEN`. Se o token estiver certo,
o 403 é de perfil: a rota exige `ADMIN`.
</details>

## Autores

- Vitor Rocha Fagundes
