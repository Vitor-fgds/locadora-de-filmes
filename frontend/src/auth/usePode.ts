import { podeUsar, type Perfil } from './tipos'
import { useUsuario } from './useUsuario'

/** Esconde ações que o perfil não pode executar. O backend continua sendo a barreira real. */
export function usePode(minimo: Perfil): boolean {
  const { data: usuario } = useUsuario()
  return usuario ? podeUsar(usuario.perfil, minimo) : false
}
