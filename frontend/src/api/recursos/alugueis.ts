import { keepPreviousData, useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'
import { api } from '../http'
import { qs, TAMANHO_PAGINA } from '../recurso'
import type { Aluguel, AluguelDados, Pagina } from '../tipos'

interface FiltroHistorico {
  idCliente?: number
  emAberto: boolean
  pagina: number
}

/** RF10: histórico com cliente, funcionário e filme (JOIN de 5 tabelas no backend). */
export function useHistorico({ idCliente, emAberto, pagina }: FiltroHistorico) {
  return useQuery({
    queryKey: ['alugueis', 'lista', idCliente ?? null, emAberto, pagina],
    queryFn: ({ signal }) =>
      api<Pagina<Aluguel>>(`/api/alugueis${qs({ idCliente, emAberto, page: pagina - 1, size: TAMANHO_PAGINA })}`, {
        signal,
      }),
    placeholderData: keepPreviousData,
    staleTime: 0,
  })
}

// Aluguel e devolução mexem em aluguéis, na disponibilidade dos exemplares e nos relatórios.
function invalidarTudoDeAluguel(queryClient: QueryClient) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: ['alugueis'] }),
    queryClient.invalidateQueries({ queryKey: ['filmes'] }),
    queryClient.invalidateQueries({ queryKey: ['relatorios'] }),
  ])
}

/** RF07: transação no backend (lock no exemplar → INSERT aluguel → UPDATE exemplar). */
export function useRegistrarAluguel() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (dados: AluguelDados) => api<Aluguel>('/api/alugueis', { method: 'POST', body: dados }),
    onSettled: () => invalidarTudoDeAluguel(queryClient), // também no erro: a lista pode ter mudado
  })
}

/** RF08: chama a stored procedure sp_registrar_devolucao; a resposta traz a multa calculada. */
export function useDevolver() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (v: { id: number; dataDevolucao: string }) =>
      api<Aluguel>(`/api/alugueis/${v.id}/devolucao`, { method: 'POST', body: { dataDevolucao: v.dataDevolucao } }),
    onSettled: () => invalidarTudoDeAluguel(queryClient),
  })
}
