import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { buscarUsuario, chaveUsuario, login, logout, trocarSessao } from './authApi'

export function useUsuario() {
  return useQuery({
    queryKey: chaveUsuario,
    queryFn: buscarUsuario,
    retry: false,
    staleTime: 5 * 60_000,
  })
}

export function useLogin() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: login,
    onSuccess: (usuario) => trocarSessao(queryClient, usuario),
  })
}

export function useLogout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: logout,
    // Mesmo se o POST falhar (sessão já expirada), a tela deve voltar ao login.
    onSettled: () => trocarSessao(queryClient, null),
  })
}
