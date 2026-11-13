import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './http'
import type { Pagina } from './tipos'

export const TAMANHO_PAGINA = 20

/** Monta "?a=1&b=2" ignorando valores vazios. */
export function qs(params: Record<string, string | number | boolean | undefined | null>): string {
  const busca = new URLSearchParams()
  for (const [chave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== null && valor !== '' && valor !== false) busca.set(chave, String(valor))
  }
  const texto = busca.toString()
  return texto ? `?${texto}` : ''
}

/**
 * Hooks do CRUD padrão da API (GET lista com ?nome=&page=, GET /{id}, POST, PUT /{id}, DELETE /{id}).
 * Clientes, filmes, atores e funcionários seguem todos esse formato.
 * `chave` é o prefixo das query keys: qualquer escrita invalida tudo do recurso.
 */
export function criarRecurso<Entidade, Dados>(base: string, chave: string) {
  function useLista(nome: string, pagina: number) {
    return useQuery({
      queryKey: [chave, 'lista', nome, pagina],
      queryFn: ({ signal }) =>
        api<Pagina<Entidade>>(`${base}${qs({ nome, page: pagina - 1, size: TAMANHO_PAGINA })}`, { signal }),
      placeholderData: keepPreviousData, // a página anterior fica na tela enquanto a nova carrega
    })
  }

  function useUm(id: number | undefined) {
    return useQuery({
      queryKey: [chave, 'um', id],
      queryFn: ({ signal }) => api<Entidade>(`${base}/${id}`, { signal }),
      enabled: id !== undefined,
    })
  }

  function useSalvar(id: number | undefined) {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: (dados: Dados) =>
        id === undefined
          ? api<Entidade>(base, { method: 'POST', body: dados })
          : api<Entidade>(`${base}/${id}`, { method: 'PUT', body: dados }),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [chave] }),
    })
  }

  function useExcluir() {
    const queryClient = useQueryClient()
    return useMutation({
      mutationFn: (id: number) => api(`${base}/${id}`, { method: 'DELETE' }),
      onSuccess: () => queryClient.invalidateQueries({ queryKey: [chave] }),
    })
  }

  return { useLista, useUm, useSalvar, useExcluir }
}
