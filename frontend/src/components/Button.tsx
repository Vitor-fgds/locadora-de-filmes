import type { ComponentProps } from 'react'
import estilos from './Button.module.css'

interface Props extends ComponentProps<'button'> {
  variante?: 'primario' | 'secundario' | 'perigo'
  carregando?: boolean
}

export function Button({ variante = 'primario', carregando = false, disabled, children, className, ...resto }: Props) {
  return (
    <button
      type="button"
      {...resto}
      className={`${estilos.botao} ${estilos[variante]} ${className ?? ''}`}
      disabled={disabled || carregando}
      aria-busy={carregando || undefined}
    >
      {carregando && <span className={estilos.giro} aria-hidden="true" />}
      {children}
    </button>
  )
}
