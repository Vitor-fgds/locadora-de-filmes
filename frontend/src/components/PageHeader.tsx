import type { ReactNode } from 'react'
import estilos from './PageHeader.module.css'

interface Props {
  titulo: string
  /** Botões de ação da página (ex.: "Novo cliente"). Vão para baixo do título no celular. */
  acoes?: ReactNode
}

export function PageHeader({ titulo, acoes }: Props) {
  return (
    <header className={estilos.cabecalho}>
      <h1>{titulo}</h1>
      {acoes && <div className={estilos.acoes}>{acoes}</div>}
    </header>
  )
}
