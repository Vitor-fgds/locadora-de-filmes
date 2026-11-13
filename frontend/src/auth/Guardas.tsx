import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { Spinner } from '../components/Spinner'
import { AppShell } from '../layout/AppShell'
import { SemPermissaoPage } from '../pages/SemPermissaoPage'
import { podeUsar, type Perfil } from './tipos'
import { useUsuario } from './useUsuario'

/** Rota-pai das telas internas: sem sessão vai ao login; com sessão envolve no AppShell. */
export function Autenticado() {
  const { data: usuario, isPending } = useUsuario()
  const local = useLocation()

  if (isPending) return <Spinner rotulo="Verificando sessão…" />
  if (!usuario) return <Navigate to="/login" replace state={{ from: local.pathname + local.search }} />
  return <AppShell usuario={usuario} />
}

/** Esconde a tela de quem não tem o perfil mínimo (o backend continua sendo a barreira real). */
export function ExigePerfil({ minimo, children }: { minimo: Perfil; children: ReactNode }) {
  const { data: usuario } = useUsuario()
  if (!usuario || !podeUsar(usuario.perfil, minimo)) return <SemPermissaoPage />
  return children
}
