import { NavLink } from 'react-router'
import { itensMenu } from '../auth/menu'
import { podeUsar, rotuloPerfil, type Usuario } from '../auth/tipos'
import { useLogout } from '../auth/useUsuario'
import { Button } from '../components/Button'
import estilos from './MenuLateral.module.css'

/** Conteúdo do menu: usado na lateral fixa (≥1024 px) e dentro do drawer (celular/tablet). */
export function MenuLateral({ usuario }: { usuario: Usuario }) {
  const logout = useLogout()
  const itens = itensMenu.filter((item) => podeUsar(usuario.perfil, item.minimo))

  return (
    <div className={estilos.menu}>
      <p className={estilos.marca}>Locadora</p>
      <nav aria-label="Principal" className={estilos.nav}>
        <ul>
          {itens.map((item) => (
            <li key={item.caminho}>
              <NavLink to={item.caminho} className={({ isActive }) => `${estilos.link} ${isActive ? estilos.ativo : ''}`}>
                {item.rotulo}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
      <div className={estilos.usuario}>
        <p className={estilos.nome}>{usuario.nome}</p>
        <p className={estilos.perfil}>{rotuloPerfil[usuario.perfil]}</p>
        <Button variante="secundario" onClick={() => logout.mutate()} carregando={logout.isPending}>
          Sair
        </Button>
      </div>
    </div>
  )
}
