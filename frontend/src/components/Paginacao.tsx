import { plural } from '../lib/formato'
import { Button } from './Button'
import estilos from './Paginacao.module.css'

interface Props {
  /** Página atual, começando em 1. */
  pagina: number
  totalPaginas: number
  totalRegistros: number
  aoMudar: (pagina: number) => void
  carregando?: boolean
}

export function Paginacao({ pagina, totalPaginas, totalRegistros, aoMudar, carregando = false }: Props) {
  if (totalRegistros === 0) return null
  return (
    <nav className={estilos.nav} aria-label="Paginação">
      <p className={estilos.resumo} aria-live="polite" aria-busy={carregando}>
        Página {pagina} de {Math.max(totalPaginas, 1)} · {plural(totalRegistros, 'registro', 'registros')}
      </p>
      <div className={estilos.botoes}>
        <Button variante="secundario" disabled={pagina <= 1} onClick={() => aoMudar(pagina - 1)}>
          Anterior
        </Button>
        <Button variante="secundario" disabled={pagina >= totalPaginas} onClick={() => aoMudar(pagina + 1)}>
          Próxima
        </Button>
      </div>
    </nav>
  )
}
