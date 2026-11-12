import type { ReactNode } from 'react'
import estilos from './Etiqueta.module.css'

interface Props {
  /** "ok" = disponível/em dia · "neutro" = alugado/em aberto · "atencao" = atraso e multa. */
  tom: 'ok' | 'neutro' | 'atencao'
  children: ReactNode
}

// Situação sempre em texto: a cor reforça, mas não carrega a informação sozinha.
export function Etiqueta({ tom, children }: Props) {
  return <span className={`${estilos.etiqueta} ${estilos[tom]}`}>{children}</span>
}
