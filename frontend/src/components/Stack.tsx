import type { CSSProperties, ReactNode } from 'react'
import estilos from './Stack.module.css'

interface Props {
  children: ReactNode
  gap?: 2 | 3 | 4 | 5 | 6 | 7
}

export function Stack({ children, gap = 4 }: Props) {
  return (
    <div className={estilos.stack} style={{ '--gap': `var(--espaco-${gap})` } as CSSProperties}>
      {children}
    </div>
  )
}
