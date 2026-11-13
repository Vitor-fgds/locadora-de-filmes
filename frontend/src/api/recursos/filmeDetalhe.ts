import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../http'
import { qs, TAMANHO_PAGINA } from '../recurso'
import type { Ator, Exemplar, ExemplarDisponivel, Pagina } from '../tipos'

// Tudo que pertence a um filme (RF03 elenco, RF04 exemplares, RF09 disponíveis) fica sob a
// chave ['filmes', id, …]: as mutações do CRUD de filmes invalidam o prefixo ['filmes'] e
// atualizam estas listas junto.

export function useElenco(idFilme: number) {
  return useQuery({
    queryKey: ['filmes', idFilme, 'elenco'],
    queryFn: ({ signal }) => api<Ator[]>(`/api/filmes/${idFilme}/atores`, { signal }),
  })
}

export function useExemplares(idFilme: number, pagina: number) {
  return useQuery({
    queryKey: ['filmes', idFilme, 'exemplares', pagina],
    queryFn: ({ signal }) =>
      api<Pagina<Exemplar>>(`/api/filmes/${idFilme}/exemplares${qs({ page: pagina - 1, size: TAMANHO_PAGINA })}`, {
        signal,
      }),
    placeholderData: keepPreviousData,
  })
}

/** RF09: exemplares livres do filme (JOIN filme–exemplar no backend). */
export function useDisponiveis(idFilme: number | undefined) {
  return useQuery({
    queryKey: ['filmes', idFilme, 'disponiveis'],
    queryFn: ({ signal }) => api<ExemplarDisponivel[]>(`/api/filmes/${idFilme}/exemplares/disponiveis`, { signal }),
    enabled: idFilme !== undefined,
    staleTime: 0, // disponibilidade muda a cada aluguel: sempre confirma no servidor
  })
}

export function useVincularAtor(idFilme: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (idAtor: number) => api(`/api/filmes/${idFilme}/atores/${idAtor}`, { method: 'POST' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['filmes', idFilme, 'elenco'] }),
  })
}

export function useDesvincularAtor(idFilme: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (idAtor: number) => api(`/api/filmes/${idFilme}/atores/${idAtor}`, { method: 'DELETE' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['filmes', idFilme, 'elenco'] }),
  })
}

export function useAdicionarExemplar(idFilme: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => api<Exemplar>(`/api/filmes/${idFilme}/exemplares`, { method: 'POST' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['filmes'] }),
  })
}

export function useExcluirExemplar() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (idExemplar: number) => api(`/api/exemplares/${idExemplar}`, { method: 'DELETE' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['filmes'] }),
  })
}

/** Passa o exemplar para outro filme (só se estiver disponível; senão o backend responde 422). */
export function useMoverExemplar() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (v: { idExemplar: number; idFilme: number }) =>
      api<Exemplar>(`/api/exemplares/${v.idExemplar}`, { method: 'PUT', body: { idFilme: v.idFilme } }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['filmes'] }),
  })
}
