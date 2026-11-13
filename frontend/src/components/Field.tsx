import { useId, type ReactNode } from 'react'
import estilos from './Field.module.css'

export interface PropsControle {
  id: string
  'aria-describedby': string | undefined
  'aria-invalid': true | undefined
}

interface Props {
  rotulo: string
  dica?: string
  erro?: string | undefined
  /** Ocupa a linha inteira no FormGrid de 2 colunas. */
  inteiro?: boolean
  /** Recebe os atributos de acessibilidade para repassar ao <Input> / <Select>. */
  children: (controle: PropsControle) => ReactNode
}

export function Field({ rotulo, dica, erro, inteiro = false, children }: Props) {
  const id = useId()
  const idDica = `${id}-dica`
  const idErro = `${id}-erro`
  const descricao = [dica ? idDica : null, erro ? idErro : null].filter(Boolean).join(' ') || undefined

  return (
    <div className={`${estilos.campo} ${inteiro ? estilos.inteiro : ''}`}>
      <label htmlFor={id}>{rotulo}</label>
      {dica && (
        <span id={idDica} className={estilos.dica}>
          {dica}
        </span>
      )}
      {children({ id, 'aria-describedby': descricao, 'aria-invalid': erro ? true : undefined })}
      {erro && (
        <span id={idErro} className={estilos.erro}>
          {erro}
        </span>
      )}
    </div>
  )
}
