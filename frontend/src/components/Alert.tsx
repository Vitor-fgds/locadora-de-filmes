import type { ReactNode } from 'react'
import estilos from './Alert.module.css'

interface Props {
  tipo?: 'erro' | 'aviso'
  children: ReactNode
}

// "erro" é anunciado na hora pelo leitor de tela (role=alert); "aviso" espera a vez.
export function Alert({ tipo = 'erro', children }: Props) {
  return (
    <div className={`${estilos.alerta} ${estilos[tipo]}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      {children}
    </div>
  )
}
