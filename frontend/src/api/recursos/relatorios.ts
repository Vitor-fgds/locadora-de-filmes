import { useQuery } from '@tanstack/react-query'
import { api } from '../http'
import type { ExemplaresPorFilme, FaturamentoFuncionario, FilmeComElenco } from '../tipos'

// RF11 (só ADMIN). Os três hooks são independentes, então a tela dispara as três consultas
// ao mesmo tempo, sem uma esperar a outra.
export const useFaturamento = () =>
  useQuery({
    queryKey: ['relatorios', 'faturamento'],
    queryFn: ({ signal }) => api<FaturamentoFuncionario[]>('/api/relatorios/faturamento-funcionarios', { signal }),
  })

export const useExemplaresPorFilme = () =>
  useQuery({
    queryKey: ['relatorios', 'exemplares-por-filme'],
    queryFn: ({ signal }) => api<ExemplaresPorFilme[]>('/api/relatorios/exemplares-por-filme', { signal }),
  })

export const useFilmesComElenco = () =>
  useQuery({
    queryKey: ['relatorios', 'filmes-com-mais-de-um-ator'],
    queryFn: ({ signal }) => api<FilmeComElenco[]>('/api/relatorios/filmes-com-mais-de-um-ator', { signal }),
  })
