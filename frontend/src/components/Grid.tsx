import type { CSSProperties, ReactNode } from 'react'
import estilos from './Grid.module.css'

interface Props {
  children: ReactNode
  /** Largura mínima de cada coluna; as colunas se ajustam sozinhas à largura disponível. */
  min?: string
  gap?: 3 | 4 | 5 | 6
}

export function Grid({ children, min = '16rem', gap = 5 }: Props) {
  const estilo = { '--min': min, '--gap': `var(--espaco-${gap})` } as CSSProperties
  return (
    <div className={estilos.grid} style={estilo}>
      {children}
    </div>
  )
}
