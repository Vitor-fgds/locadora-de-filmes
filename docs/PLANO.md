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

## [x] Fase 1 — Banco de dados (13/11)

```
Leia CLAUDE.md, docs/ROTEIRO.md e docs/bd1/script_original.sql. Use a skill roteiro-bd2.

Crie a camada de banco, sem backend ainda:
1. docker-compose.yml (só o serviço postgres:17-alpine, porta 127.0.0.1:5432,
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

## [x] Fase 2 — Esqueleto do backend e conexão (14–15/11)

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

## [x] Fase 3 — Spring Security (16/11)

```
Use a skill spring-api-padroes (seção Segurança). Consulte o Context7 para Spring
Security 7 (CSRF para SPA e login JSON com sessão).
Implemente: POST /api/auth/login, POST /api/auth/logout, GET /api/auth/me,
UserDetailsService por email, BCrypt, @EnableMethodSecurity, 401/403 em ProblemDetail.
Testes: login ok, senha errada (mensagem genérica), 401 sem sessão, 403 ATENDENTE em
rota ADMIN, POST sem CSRF rejeitado.
```

---

## [x] Fase 4a — Cadastros (17–19/11)

```
Implemente RF02–RF06 de docs/REQUISITOS.md seguindo spring-api-padroes:
DTOs record com validação compatível com os VARCHAR do banco, paginação,
@PreAuthorize por perfil, 409 em violação de FK (RN03).
Marque o primeiro uso de cada comando (save/insert, findAll/select, update por dirty
checking, deleteById/delete) com [ROTEIRO-FW-MAPEAMENTO] + [ROTEIRO-2d-*] e preencha a
seção 4 de docs/BANCO_DE_DADOS.md com o SQL real do log.
```

## [x] Fase 4b — Aluguel, devolução e consultas (20–21/11)

```
Implemente RF07 (transação no service com lock pessimista no exemplar), RF08 (chamada
da sp_registrar_devolucao, convertendo P0001 em 422), RF09–RF11 (JPQL com JOIN e uma
consulta nativa para mostrar SQL puro). Testes de integração: aluguel ok, rollback
quando o exemplar já está alugado, devolução no prazo e com multa.
Preencha as seções 5, 6 e 7 de docs/BANCO_DE_DADOS.md, com diagrama Mermaid da
transação de aluguel.
```

---

## [x] Fase 5 — Revisão de qualidade e segurança (22/11)

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

## [x] Fase 6a — Fundação do frontend e identidade visual (23–24/11)

```
Use a skill frontend-design. O projeto não tem identidade visual: proponha uma direção
sóbria e elegante para um sistema interno de locadora (paleta, tipografia, espaçamento)
como design tokens em CSS e me mostre antes de aplicar.
Depois crie frontend/ com Vite + React + TS strict, React Router, TanStack Query,
react-hook-form + zod, proxy /api → :8080, cliente HTTP que envia o header CSRF e trata
401 (vai ao login) e ProblemDetail (mensagens nos formulários).
Telas: login, layout com menu por perfil, página "Meu perfil".

Responsividade é requisito, não acabamento: siga a seção "Frontend responsivo" do
CLAUDE.md. Construa mobile-first: tokens de breakpoint, tipografia fluida com clamp(),
layout com menu lateral no desktop que vira menu recolhível (drawer) no celular.
Crie desde já os componentes base responsivos (container, grid, tabela que vira lista
de cartões em telas estreitas, formulário em uma coluna no celular).
```

Verifique: DevTools do navegador (`F12` → ícone de dispositivo) em 360, 768, 1024 e
1440 px de largura, retrato e paisagem. Nenhuma rolagem horizontal, nada cortado.

## [x] Fase 6b — Telas (25–26/11)

```
Use as skills frontend-design e vercel-react-best-practices.
Implemente as telas de RF02–RF11: listas paginadas com busca, formulários validados,
fluxo de aluguel (escolher filme → exemplar disponível → cliente) e de devolução
mostrando a multa calculada pela procedure, e página de relatórios.
Esconda ações que o perfil não pode executar (o backend continua sendo a barreira real).
Toda tela usa os componentes responsivos da Fase 6a e cumpre a seção "Frontend
responsivo" do CLAUDE.md. Relatórios com tabelas largas: lista de cartões no celular.
Ao final, revise cada tela nas larguras 360, 768, 1024 e 1440 px e corrija o que quebrar.
```

---

## [x] Fase 7 — Aplicação completa em Docker, numa branch (27/11)

Objetivo: qualquer computador com **apenas o Docker** roda o sistema inteiro.
Antes de colar o prompt, confirme que a `main` está limpa (`git status`) e atualizada.

```
Crie a branch feature/docker-app a partir da main (git switch -c feature/docker-app)
e trabalhe só nela. Consulte o Context7 para as imagens e o Spring Boot 4.1.

Objetivo: rodar o sistema completo só com Docker, em dois containers: banco e aplicação.
1. backend/Dockerfile multi-stage, com contexto na raiz do repositório:
   - estágio node (LTS, alpine): npm ci e build do frontend;
   - estágio eclipse-temurin:25-jdk: baixa dependências Maven em camada separada
     (cache), copia o build do frontend para os recursos estáticos e gera o jar
     (pule os testes aqui; eles rodam fora);
   - estágio final eclipse-temurin:25-jre: só o jar, usuário não-root, porta 8080.
   O Spring passa a servir o frontend na mesma origem da API: rotas que não são /api e
   não são arquivos voltam para index.html (rotas do React Router); o Spring Security
   libera os arquivos estáticos e mantém /api protegido. Nada muda no modo dev com Vite.
2. Healthcheck da aplicação: spring-boot-starter-actuator expondo só /actuator/health.
3. .dockerignore na raiz (node_modules, target, dist, .git, .env, .claude).
4. docker-compose.yml: mantenha o serviço do banco como está e adicione o serviço app
   no profile "app", com build do Dockerfile, depends_on do banco com
   condition: service_healthy, variáveis vindas do .env (DB_URL apontando para o host
   "db"), SPRING_PROFILES_ACTIVE configurável (padrão dev, para ter os dados de
   demonstração), porta 127.0.0.1:8080 e restart: unless-stopped.
   O fluxo de dev continua: "docker compose up -d" sobe só o banco;
   "docker compose --profile app up -d --build" sobe tudo.
5. Atualize .env.example e marque a configuração de conexão do container com
   [ROTEIRO-1c-CONEXAO]; registre na seção 2 de docs/BANCO_DE_DADOS.md como a conexão
   muda dentro do Docker (host db em vez de localhost).

Validação obrigatória antes de concluir:
- pare o ambiente de dev (docker compose stop) e teste num projeto isolado, para não
  apagar o banco de dev: docker compose -p locadora-teste --profile app up -d --build
  (banco novo do zero: todas as migrations e o seed precisam rodar);
- os dois containers ficam healthy (docker compose -p locadora-teste ps);
- curl em /actuator/health retorna UP; http://localhost:8080 abre o login;
- login com o usuário ADMIN de demonstração e um GET autenticado a /api funcionam;
- ./mvnw -q verify e npm run build continuam passando;
- no fim, docker compose -p locadora-teste --profile app down -v (só o projeto de teste).
Faça commits pequenos e descritivos na branch. Não faça merge nem push: ao terminar,
liste o que eu devo testar manualmente.
```

Teste você mesmo seguindo a lista que ele devolver (de preferência num computador de
um colega, só com Docker instalado). **Se tudo funcionar**, faça o merge pela pull request:

```bash
git push -u origin feature/docker-app
gh pr create --fill --base main
gh pr merge --merge --delete-branch
git switch main && git pull
```

Se algo falhar, continue na branch com `/clear` e descreva o erro ao Claude Code
(cole a saída de `docker compose --profile app logs app`).

---

## [x] Fase 8 — Documentação e entrega (28–30/11)

```
Use a skill readme-didatico. Escreva README.md (raiz, índice curto), backend/README.md
e frontend/README.md. No README da raiz, o "Início rápido" principal é o modo Docker
(docker compose --profile app up -d --build, só Docker necessário); o modo de
desenvolvimento (JDK + Node + Vite) vem em seguida. Revise docs/BANCO_DE_DADOS.md
inteiro: confira cada arquivo:linha e cada SQL contra o código e o log atuais;
complete o checklist do roteiro.
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
