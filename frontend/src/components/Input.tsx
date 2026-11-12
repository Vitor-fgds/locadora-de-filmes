import type { ComponentProps } from 'react'
import estilos from './Input.module.css'

// Passe `type`, `inputMode` e `autoComplete` corretos em cada uso (email, tel, number, date…).
export function Input({ className, ...resto }: ComponentProps<'input'>) {
  return <input {...resto} className={`${estilos.controle} ${className ?? ''}`} />
}

export function Select({ className, ...resto }: ComponentProps<'select'>) {
  return <select {...resto} className={`${estilos.controle} ${className ?? ''}`} />
}
