import { useEffect, useId, useRef, type ReactNode } from 'react'
import estilos from './Dialogo.module.css'

interface Props {
  aberto: boolean
  aoFechar: () => void
  titulo: string
  children: ReactNode
}

/**
 * Janela modal sobre <dialog> nativo: foco preso, Esc fecha e o resto da página fica inerte.
 * O conteúdo só existe enquanto aberto, então formulários e erros começam limpos a cada abertura.
 */
export function Dialogo({ aberto, aoFechar, titulo, children }: Props) {
  const ref = useRef<HTMLDialogElement>(null)
  const idTitulo = useId()

  useEffect(() => {
    const dialogo = ref.current
    if (!dialogo) return
    if (aberto && !dialogo.open) dialogo.showModal()
    else if (!aberto && dialogo.open) dialogo.close()
  }, [aberto])

  return (
    <dialog
      ref={ref}
      className={estilos.dialogo}
      aria-labelledby={idTitulo}
      onClose={aoFechar}
      // Clique no fundo escurecido (o próprio <dialog>, fora do conteúdo) fecha.
      onClick={(e) => {
        if (e.target === e.currentTarget) e.currentTarget.close()
      }}
    >
      {aberto && (
        <div className={estilos.conteudo}>
          <h2 id={idTitulo}>{titulo}</h2>
          {children}
        </div>
      )}
    </dialog>
  )
}
