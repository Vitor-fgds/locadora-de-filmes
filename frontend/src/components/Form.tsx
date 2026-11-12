import type { ComponentProps, ReactNode } from 'react'
import estilos from './Form.module.css'

// <form> com noValidate: a validação é do zod (mensagens em português, no campo certo).
export function Form({ className, ...resto }: ComponentProps<'form'>) {
  return <form noValidate {...resto} className={`${estilos.form} ${className ?? ''}`} />
}

/** Uma coluna no celular; duas a partir de 768 px. Use <Field inteiro> para ocupar a linha. */
export function FormGrid({ children }: { children: ReactNode }) {
  return <div className={estilos.grade}>{children}</div>
}

/** Botões do formulário: empilhados e largos no celular, lado a lado no desktop. A ação principal vem primeiro. */
export function FormActions({ children }: { children: ReactNode }) {
  return <div className={estilos.acoes}>{children}</div>
}
