import { criarRecurso } from '../recurso'
import type {
  Ator,
  AtorDados,
  Cliente,
  ClienteDados,
  Filme,
  FilmeDados,
  Funcionario,
  FuncionarioDados,
} from '../tipos'

// RF02, RF03, RF05 e RF06: mesmo formato de API, então todos saem da mesma fábrica.
export const clientes = criarRecurso<Cliente, ClienteDados>('/api/clientes', 'clientes')
export const filmes = criarRecurso<Filme, FilmeDados>('/api/filmes', 'filmes')
export const atores = criarRecurso<Ator, AtorDados>('/api/atores', 'atores')
export const funcionarios = criarRecurso<Funcionario, FuncionarioDados>('/api/funcionarios', 'funcionarios')
