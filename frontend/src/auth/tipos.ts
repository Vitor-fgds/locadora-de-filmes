export type Perfil = 'ADMIN' | 'ATENDENTE'

// Espelha UsuarioResponse do backend (sem senha).
export interface Usuario {
  id: number
  nome: string
  email: string
  perfil: Perfil
}

// ADMIN herda tudo do ATENDENTE (mesma hierarquia do backend, SecurityConfig.roleHierarchy).
export const podeUsar = (perfil: Perfil, minimo: Perfil): boolean =>
  perfil === 'ADMIN' || perfil === minimo

export const rotuloPerfil: Record<Perfil, string> = {
  ADMIN: 'Administrador',
  ATENDENTE: 'Atendente',
}
