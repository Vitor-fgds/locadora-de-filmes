import type { Perfil } from './tipos'

export interface ItemMenu {
  rotulo: string
  caminho: string
  /** Perfil mínimo. ADMIN enxerga tudo. */
  minimo: Perfil
}

// Rotas das telas da Fase 6b já reservadas; até lá apontam para <EmBreve />.
export const itensMenu: readonly ItemMenu[] = [
  { rotulo: 'Novo aluguel', caminho: '/alugueis/novo', minimo: 'ATENDENTE' },
  { rotulo: 'Devolução', caminho: '/devolucoes', minimo: 'ATENDENTE' },
  { rotulo: 'Histórico', caminho: '/historico', minimo: 'ATENDENTE' },
  { rotulo: 'Clientes', caminho: '/clientes', minimo: 'ATENDENTE' },
  { rotulo: 'Filmes', caminho: '/filmes', minimo: 'ATENDENTE' },
  { rotulo: 'Atores', caminho: '/atores', minimo: 'ATENDENTE' },
  { rotulo: 'Funcionários', caminho: '/funcionarios', minimo: 'ADMIN' },
  { rotulo: 'Relatórios', caminho: '/relatorios', minimo: 'ADMIN' },
  { rotulo: 'Meu perfil', caminho: '/perfil', minimo: 'ATENDENTE' },
]
