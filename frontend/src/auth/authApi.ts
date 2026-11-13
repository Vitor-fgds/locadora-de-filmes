import type { QueryClient } from '@tanstack/react-query'
import { api } from '../api/http'
import { ehApiError } from '../api/problem'
import type { Usuario } from './tipos'

export interface Credenciais {
  email: string
  senha: string
}

export const chaveUsuario = ['auth', 'me'] as const

/**
 * Troca a sessão no cache: descarta tudo que veio da sessão anterior (listas, buscas…)
 * e grava o novo usuário (ou null no logout). Mantém a query do usuário viva, porque
 * há componentes inscritos nela — `queryClient.clear()` os deixaria órfãos.
 */
export function trocarSessao(queryClient: QueryClient, usuario: Usuario | null): void {
  queryClient.removeQueries({ predicate: (q) => q.queryKey[0] !== chaveUsuario[0] })
  queryClient.setQueryData(chaveUsuario, usuario)
}

export const login = (credenciais: Credenciais): Promise<Usuario> =>
  api<Usuario>('/api/auth/login', { method: 'POST', body: credenciais, ignorar401: true })

export const logout = (): Promise<void> => api('/api/auth/logout', { method: 'POST' })

/** Usuário da sessão atual, ou null se não há sessão (401 não é erro aqui). */
export async function buscarUsuario(): Promise<Usuario | null> {
  try {
    return await api<Usuario>('/api/auth/me', { ignorar401: true })
  } catch (erro) {
    if (ehApiError(erro) && erro.status === 401) return null
    throw erro
  }
}
