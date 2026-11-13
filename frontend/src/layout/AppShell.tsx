import { useEffect, useRef, useState } from 'react'
import { Outlet, useLocation } from 'react-router'
import type { Usuario } from '../auth/tipos'
import { Perfuracao } from '../components/Perfuracao'
import { mediaQuery } from '../styles/breakpoints'
import estilos from './AppShell.module.css'
import { MenuLateral } from './MenuLateral'

const ID_DRAWER = 'menu-drawer'

/**
 * ≥1024 px: menu lateral fixo. Abaixo: barra superior com botão "Menu" que abre o mesmo
 * menu num <dialog> modal (foco preso, Esc fecha e o resto da página fica inerte — nativos).
 */
export function AppShell({ usuario }: { usuario: Usuario }) {
  const drawer = useRef<HTMLDialogElement>(null)
  const [aberto, setAberto] = useState(false)
  const { pathname } = useLocation()

  // Navegou para outra tela: fecha o drawer.
  useEffect(() => {
    drawer.current?.close()
  }, [pathname])

  // Virou desktop (ex.: girou o tablet): o drawer não faz mais sentido.
  useEffect(() => {
    const largo = window.matchMedia(mediaQuery('lg'))
    const aoMudar = (e: MediaQueryListEvent) => {
      if (e.matches) drawer.current?.close()
    }
    largo.addEventListener('change', aoMudar)
    return () => largo.removeEventListener('change', aoMudar)
  }, [])

  return (
    <div className={estilos.shell}>
      <a href="#conteudo" className={estilos.pular}>
        Pular para o conteúdo
      </a>

      <aside className={estilos.lateral}>
        <MenuLateral usuario={usuario} />
        <Perfuracao />
      </aside>

      <header className={estilos.barra}>
        <button
          type="button"
          className={estilos.botaoMenu}
          aria-expanded={aberto}
          aria-controls={ID_DRAWER}
          onClick={() => drawer.current?.showModal()}
        >
          <span className={estilos.hamburguer} aria-hidden="true" />
          Menu
        </button>
        <span className={estilos.marcaBarra}>Locadora</span>
      </header>

      <dialog
        ref={drawer}
        id={ID_DRAWER}
        className={estilos.drawer}
        aria-label="Menu"
        onToggle={(e) => setAberto(e.currentTarget.open)}
        onClose={() => setAberto(false)}
        // Clique no fundo escurecido (o próprio <dialog>, fora do conteúdo) fecha.
        onClick={(e) => {
          if (e.target === e.currentTarget) e.currentTarget.close()
        }}
      >
        <MenuLateral usuario={usuario} />
        <Perfuracao />
      </dialog>

      <main id="conteudo" className={estilos.conteudo} tabIndex={-1}>
        <Outlet />
      </main>
    </div>
  )
}
