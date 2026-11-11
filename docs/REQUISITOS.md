# Requisitos funcionais — Locadora de Filmes

> Rascunho inicial. O grupo deve revisar antes da Fase 1. Cada requisito aponta para
> o item do roteiro que demonstra.

| ID | Requisito | Perfil | Comandos / recurso de BD | Roteiro |
|---|---|---|---|---|
| RF01 | Autenticar funcionário (login/logout) com email e senha | todos | SELECT funcionario | 1c-CONSULTA |
| RF02 | Manter filmes (cadastrar, listar, editar, excluir) | ADMIN (escrita) | INSERT/SELECT/UPDATE/DELETE | 2d |
| RF03 | Manter atores e vincular/desvincular atores a filmes | ADMIN (escrita) | INSERT/DELETE em filme_ator | 2d |
| RF04 | Manter exemplares de um filme | ADMIN (escrita) | INSERT/UPDATE/DELETE | 2d |
| RF05 | Manter clientes | ATENDENTE, ADMIN (excluir só ADMIN) | INSERT/SELECT/UPDATE/DELETE | 2d |
| RF06 | Manter funcionários e seus perfis | ADMIN | INSERT/UPDATE/DELETE, senha em BCrypt | 2d |
| RF07 | **Registrar aluguel**: valida exemplar disponível, cria aluguel e marca exemplar como alugado | ATENDENTE, ADMIN | SELECT … FOR UPDATE → INSERT → UPDATE em **transação** (`@Transactional`) | 2a |
| RF08 | **Registrar devolução**: grava data, calcula multa por atraso e libera o exemplar | ATENDENTE, ADMIN | **Stored procedure** `sp_registrar_devolucao(p_id_aluguel, p_data_devolucao)` | 2a, 2c |
| RF09 | Consultar exemplares disponíveis por filme | todos | SELECT com **JOIN** filme–exemplar | 2b |
| RF10 | Histórico de aluguéis com cliente, funcionário e filme | todos | **JOIN** aluguel–cliente–funcionário–exemplar–filme | 2b |
| RF11 | Relatórios do BD1: faturamento por funcionário, exemplares por filme, filmes com mais de um ator | ADMIN | JOIN + GROUP BY / SUM / HAVING | 2b |

## Regras de negócio

- RN01 Um exemplar alugado (`status = TRUE`) não pode ser alugado de novo.
- RN02 Multa = dias de atraso × **R$ 2,00** (valor a confirmar pelo grupo), mínimo 0.
- RN03 Não é possível excluir cliente, funcionário ou exemplar com aluguéis registrados (HTTP 409).
- RN04 `data_entrega` (prevista) ≥ `data_aluguel`; valores ≥ 0.
