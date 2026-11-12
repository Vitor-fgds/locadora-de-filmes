import { useEffect, useId, useState } from 'react'
import { Input } from './Input'
import estilos from './SearchBox.module.css'

interface Props {
  rotulo: string
  /** Valor vigente (vem da URL ou do estado da tela). */
  valor: string
  /** Chamado 300 ms depois da última tecla: a lista não consulta a API a cada letra. */
  aoBuscar: (texto: string) => void
  dica?: string
}

export function SearchBox({ rotulo, valor, aoBuscar, dica }: Props) {
  const id = useId()
  const [texto, setTexto] = useState(valor)

  useEffect(() => {
    if (texto.trim() === valor) return
    const espera = setTimeout(() => aoBuscar(texto.trim()), 300)
    return () => clearTimeout(espera)
  }, [texto, valor, aoBuscar])

  return (
    <div className={estilos.caixa} role="search">
      <label htmlFor={id}>{rotulo}</label>
      {dica && <span className={estilos.dica}>{dica}</span>}
      <Input
        id={id}
        type="search"
        inputMode="search"
        autoComplete="off"
        spellCheck={false}
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
      />
    </div>
  )
}
