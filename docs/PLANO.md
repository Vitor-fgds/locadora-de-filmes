# Plano de desenvolvimento com Claude Code

Cada fase = **uma sessão limpa** (`/clear` antes). Comece cada fase em **Plan Mode**
(`Shift+Tab` até aparecer "plan mode"), leia o plano, ajuste e só então aprove.
Ao final, revise o diff, rode os testes e faça commit você mesmo.

**Modelo**: planejar/revisar com Opus; implementar com Sonnet (`/model`).
**Prazo**: apresentação em 01/12. Datas sugeridas entre parênteses.

---

## [ ] Fase 0 — Preparação (11–12/11, manual)

1. Instale: JDK 25 (Temurin, LTS), Node 22+ LTS, Docker, Git, Claude Code atualizado.
2. Crie o repositório, copie este kit para a raiz e faça o primeiro commit.
3. Rode `bash setup-skills.sh` (ou os comandos dele, um a um, no Windows).
4. Abra `claude` na raiz e confira: `/plugin` (aba Installed), `/skills`, `/context`.
5. Revise `docs/REQUISITOS.md` com o grupo (valor da multa, perfis).

---

## [ ] Fase 1 — Banco de dados (13/11)

```
Leia CLAUDE.md, docs/ROTEIRO.md e docs/bd1/script_original.sql. Use a skill roteiro-bd2.

Crie a camada de banco, sem backend ainda:
1. docker-compose.yml (só o serviço postgres:17-alpine, porta 127.0.0.1:55432
   — não a padrão 5432, para não conflitar com um Postgres local ou outro projeto —
   volume nomeado, healthcheck pg_isready, credenciais via .env) e .env.example.
   .gitignore com .env.
2. Migrations em backend/src/main/resources/db/migration:
   - V1__schema_bd1.sql: script do BD1 (só DDL), idêntico ao original.
   - V2__ajustes_bd2.sql: funcionario.senha → VARCHAR(100) (BCrypt);
     coluna perfil (ADMIN|ATENDENTE, CHECK, default ATENDENTE);
     aluguel.data_devolucao DATE NULL; CHECKs de valores e datas (RN04);
     índices nas FKs; ON DELETE CASCADE só em filme_ator;
     COMMENT ON COLUMN exemplar_filme.status explicando TRUE = alugado.
   - V3__procedures.sql: sp_registrar_devolucao(p_id_aluguel INT, p_data_devolucao DATE,
     INOUT p_valor_multa NUMERIC) seguindo RN02, com SELECT ... FOR UPDATE, erros via
     RAISE EXCEPTION ERRCODE 'P0001' (aluguel inexistente ou já devolvido).
3. Seed só para o perfil dev em db/seed/R__dados_demo.sql (repeatable, idempotente):
   dados do BD1, senhas com crypt(..., gen_salt('bf')) do pgcrypto
   (CREATE EXTENSION IF NOT EXISTS pgcrypto), Carlos = ADMIN, Fernanda = ATENDENTE.
Marque tudo com [ROTEIRO-xx] e preencha as seções 3 e 6 de docs/BANCO_DE_DADOS.md.
Não crie o projeto Spring ainda.
```

Verifique: `docker compose up -d` e aplique os scripts com `psql` para testar a procedure.

---

## [ ] Fase 2 — Esqueleto do backend e conexão (14–15/11)

```
Use as skills spring-api-padroes e roteiro-bd2. Consulte o Context7 para Spring Boot 4.1.

Crie o projeto em backend/ (Maven wrapper, Java 25, pacote br.edu.ufvjm.locadora) com:
web, data-jpa, security, validation, flyway (+ flyway-database-postgresql), postgresql,
testcontainers. application.yml lendo DB_URL/DB_USER/DB_PASSWORD sem fallback,
escapeSyntaxCallMode=callIfNoReturn na URL, ddl-auto=validate, open-in-view=false.
Perfil dev: log de SQL com parâmetros e flyway.locations incluindo db/seed.
Crie as entidades JPA de todas as tabelas, repositories e o GlobalExceptionHandler
(ProblemDetail). Segurança provisória: tudo autenticado (será feita na Fase 3).
Teste de integração com Testcontainers que sobe o contexto e valida o schema.
Preencha a seção 2 (Conexão) e 8 de docs/BANCO_DE_DADOS.md.
```

---

## [ ] Fase 3 — Spring Security (16/11)

```
Use a skill spring-api-padroes (seção Segurança). Consulte o Context7 para Spring
Security 7 (CSRF para SPA e login JSON com sessão).
Implemente: POST /api/auth/login, POST /api/auth/logout, GET /api/auth/me,
UserDetailsService por email, BCrypt, @EnableMethodSecurity, 401/403 em ProblemDetail.
Testes: login ok, senha errada (mensagem genérica), 401 sem sessão, 403 ATENDENTE em
rota ADMIN, POST sem CSRF rejeitado.
```

---

## [ ] Fase 4a — Cadastros (17–19/11)

```
Implemente RF02–RF06 de docs/REQUISITOS.md seguindo spring-api-padroes:
DTOs record com validação compatível com os VARCHAR do banco, paginação,
@PreAuthorize por perfil, 409 em violação de FK (RN03).
Marque o primeiro uso de cada comando (save/insert, findAll/select, update por dirty
checking, deleteById/delete) com [ROTEIRO-FW-MAPEAMENTO] + [ROTEIRO-2d-*] e preencha a
seção 4 de docs/BANCO_DE_DADOS.md com o SQL real do log.
```

## [ ] Fase 4b — Aluguel, devolução e consultas (20–21/11)

```
Implemente RF07 (transação no service com lock pessimista no exemplar), RF08 (chamada
da sp_registrar_devolucao, convertendo P0001 em 422), RF09–RF11 (JPQL com JOIN e uma
consulta nativa para mostrar SQL puro). Testes de integração: aluguel ok, rollback
quando o exemplar já está alugado, devolução no prazo e com multa.
Preencha as seções 5, 6 e 7 de docs/BANCO_DE_DADOS.md, com diagrama Mermaid da
transação de aluguel.
```

---

## [ ] Fase 5 — Revisão de qualidade e segurança (22/11)

Rode em sessões separadas e peça correções só do que for real:

```
/security-review
```
```
Use a skill insecure-defaults para auditar backend/ e docker-compose.yml.
```
```
Use o agente code-simplifier nos pacotes aluguel e security; não mude comportamento.
```

---

## [ ] Fase 6a — Fundação do frontend e identidade visual (23–24/11)

```
Use a skill frontend-design. O projeto não tem identidade visual: proponha uma direção
sóbria e elegante para um sistema interno de locadora (paleta, tipografia, espaçamento)
como design tokens em CSS e me mostre antes de aplicar.
Depois crie frontend/ com Vite + React + TS strict, React Router, TanStack Query,
react-hook-form + zod, proxy /api → :8080, cliente HTTP que envia o header CSRF e trata
401 (vai ao login) e ProblemDetail (mensagens nos formulários).
Telas: login, layout com menu por perfil, página "Meu perfil".
```

## [ ] Fase 6b — Telas (25–27/11)

```
Use as skills frontend-design e vercel-react-best-practices.
Implemente as telas de RF02–RF11: listas paginadas com busca, formulários validados,
fluxo de aluguel (escolher filme → exemplar disponível → cliente) e de devolução
mostrando a multa calculada pela procedure, e página de relatórios.
Esconda ações que o perfil não pode executar (o backend continua sendo a barreira real).
```

---

## [ ] Fase 7 — Documentação e entrega (28–30/11)

```
Use a skill readme-didatico. Escreva README.md (raiz, índice curto), backend/README.md
e frontend/README.md. Revise docs/BANCO_DE_DADOS.md inteiro: confira cada arquivo:linha
e cada SQL contra o código e o log atuais; complete o checklist do roteiro.
```

Entrega: `git archive --format=zip -o NOME_DO_GRUPO.zip HEAD` (inclui as migrations, que
são as "estruturas do banco"). Ensaie a apresentação com `grep -rn "\[ROTEIRO" .` aberto.

---

## Dicas de economia de tokens

- `/clear` entre fases; `/compact` se uma fase ficar longa.
- `/context` mostra o que está ocupando a janela; `claude plugin details <nome>` mostra
  quanto cada plugin custa por turno. Desative o que não estiver usando na fase.
- Prompts curtos que apontam para arquivos (`docs/REQUISITOS.md`) gastam menos que colar texto.
- Peça para o Claude Code rodar testes com `-q` e mostrar só falhas.
- Não aceite planos que criem arquivos extras de resumo.
