---
name: spring-api-padroes
description: Padrões de qualidade, segurança e desempenho do backend Spring Boot 4 deste projeto. Use ao criar ou revisar controllers, services, DTOs, entidades JPA, configuração do Spring Security, tratamento de erros ou testes no diretório backend/.
---

# Padrões do backend (Spring Boot 4.1 / Java 25)

## Organização por funcionalidade

`br.edu.ufvjm.locadora.<feature>` com `Entity`, `Repository`, `Service`, `Controller`
e `dto/`. Features: `filme`, `ator`, `exemplar`, `cliente`, `funcionario`, `aluguel`,
`relatorio`. Transversais: `config`, `security`, `common/error`.

## Camadas

- **Controller**: só HTTP. Recebe `@Valid` request DTO, chama service, devolve response
  DTO. Sem lógica, sem repository.
- **Service**: regras e transações. `@Transactional(readOnly = true)` na classe e
  `@Transactional` nos métodos que escrevem. Injeção por construtor (campos `final`).
- **Repository**: Spring Data JPA. Queries derivadas para casos simples; `@Query` (JPQL)
  para junções; `nativeQuery` só quando for didático mostrar SQL puro (marcar no roteiro).
- **DTOs**: `record`. Request com Bean Validation (`@NotBlank`, `@Size` igual ao
  VARCHAR do banco, `@Email`, `@Positive`, `@PastOrPresent`…). Response nunca contém
  `senha`. Conversão por método estático `from(Entity)` no record — sem MapStruct.

## JPA e desempenho

- `spring.jpa.hibernate.ddl-auto=validate` (Flyway é dono do schema).
- `spring.jpa.open-in-view=false`.
- Relacionamentos `@ManyToOne(fetch = LAZY)`. Evite N+1: `JOIN FETCH` ou projeção em
  DTO nas listagens.
- Listagens paginadas (`Pageable`, tamanho máximo 50) com ordenação estável.
- Lock pessimista (`@Lock(PESSIMISTIC_WRITE)`) ao ler o exemplar no aluguel, para evitar
  dois aluguéis simultâneos do mesmo exemplar.
- Perfil `dev`: `logging.level.org.hibernate.SQL=DEBUG` e
  `org.hibernate.orm.jdbc.bind=TRACE` para mostrar o SQL gerado na apresentação.

## Segurança (Spring Security 7)

- Autenticação por **sessão** (cookie `JSESSIONID` HttpOnly, SameSite=Lax), não JWT:
  menos código, nada de token em `localStorage`.
- Endpoints: `POST /api/auth/login` (JSON), `POST /api/auth/logout`, `GET /api/auth/me`.
  O login autentica via `AuthenticationManager` e salva o contexto com
  `HttpSessionSecurityContextRepository`. Proteção contra session fixation ativa (padrão).
- CSRF **ligado**, configurado para SPA (token em cookie legível pelo front, enviado em
  header `X-XSRF-TOKEN`). Confirme a API exata da versão no Context7.
- `UserDetailsService` lendo `funcionario` por email. `BCryptPasswordEncoder`.
- Autorização com `@EnableMethodSecurity` + `@PreAuthorize`:
  - `ADMIN`: tudo, incluindo funcionários, exclusões e relatórios de faturamento.
  - `ATENDENTE`: consultar acervo, manter clientes, registrar aluguel e devolução.
- Respostas 401/403 em JSON (`ProblemDetail`), nunca redirect para página de login.
- Sem CORS: em dev o Vite faz proxy de `/api` para `:8080` (mesma origem).
- Mensagem de login inválido genérica ("Credenciais inválidas").
- Nada de segredo hardcoded nem valor padrão inseguro em `application.yml`:
  `${DB_PASSWORD}` sem fallback.

## Erros

`@RestControllerAdvice` devolvendo `ProblemDetail` (RFC 9457):
validação → 400 com lista de campos; não encontrado → 404; violação de integridade
(FK, unique) → 409 com mensagem amigável; regra de negócio / `P0001` da procedure → 422;
genérico → 500 sem stack trace na resposta.

## Testes (mínimo que garante robustez)

- Integração com Testcontainers + `@ServiceConnection` (mesmas migrations do real).
- Cobrir: transação do aluguel (sucesso e rollback quando exemplar já alugado),
  procedure de devolução (com e sem multa), segurança (401 sem login, 403 ATENDENTE em
  rota ADMIN), validação de DTO (400).
- Não testar getters, nem cada CRUD trivial.
