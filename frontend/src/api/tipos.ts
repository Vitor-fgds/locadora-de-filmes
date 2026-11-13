import type { Perfil } from '../auth/tipos'

// Espelho dos DTOs do backend. BigDecimal chega como number no JSON; LocalDate como "AAAA-MM-DD".

/** PagedModel do Spring Data: `page.number` começa em 0. */
export interface Pagina<T> {
  content: T[]
  page: { size: number; number: number; totalElements: number; totalPages: number }
}

export interface Cliente {
  id: number
  primeiroNome: string
  sobrenome: string
  rua: string
  bairro: string
  cidade: string
  email: string
  telefone: string
}
export type ClienteDados = Omit<Cliente, 'id'>

export interface Filme {
  id: number
  nome: string
  descricao: string
  duracaoMinutos: number
  dataLancamento: string
}
export type FilmeDados = Omit<Filme, 'id'>

export interface Ator {
  id: number
  primeiroNome: string
  sobrenome: string
}
export type AtorDados = Omit<Ator, 'id'>

export interface Funcionario {
  id: number
  primeiroNome: string
  sobrenome: string
  rua: string
  bairro: string
  cidade: string
  email: string
  telefone: string
  perfil: Perfil
}
/** `senha` é obrigatória ao criar e opcional ao editar (vazia/ausente mantém a atual). */
export type FuncionarioDados = Omit<Funcionario, 'id'> & { senha?: string }

export interface Exemplar {
  id: number
  idFilme: number
  /** true = alugado, false = disponível (semântica do BD1). */
  alugado: boolean
}

export interface ExemplarDisponivel {
  idExemplar: number
  idFilme: number
  nomeFilme: string
}

export interface Aluguel {
  id: number
  dataAluguel: string
  dataEntrega: string
  dataDevolucao: string | null
  valorAluguel: number
  /** Calculada pela stored procedure sp_registrar_devolucao; 0 enquanto não devolvido. */
  valorMulta: number
  idCliente: number
  nomeCliente: string
  idFuncionario: number
  nomeFuncionario: string
  idExemplar: number
  idFilme: number
  nomeFilme: string
}

export interface AluguelDados {
  idExemplar: number
  idCliente: number
  dataEntrega: string
  valorAluguel: number
}

export interface FaturamentoFuncionario {
  idFuncionario: number
  nomeFuncionario: string
  totalAlugueis: number
  valorAlugueis: number
  valorMultas: number
  valorTotal: number
}

export interface ExemplaresPorFilme {
  idFilme: number
  nomeFilme: string
  totalExemplares: number
  disponiveis: number
}

export interface FilmeComElenco {
  idFilme: number
  nomeFilme: string
  totalAtores: number
}
