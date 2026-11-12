import type { ReactNode } from 'react'
import estilos from './Container.module.css'

interface Props {
  children: ReactNode
  /** "estreito" para formulários e telas de uma coluna. */
  tamanho?: 'padrao' | 'estreito'
}

export function Container({ children, tamanho = 'padrao' }: Props) {
  return <div className={`${estilos.container} ${tamanho === 'estreito' ? estilos.estreito : ''}`}>{children}</div>
}
