import { useState, type ReactNode } from 'react'
import { textoDoErro } from '../api/problem'
import { Alert } from './Alert'
import { Button } from './Button'
import { Dialogo } from './Dialogo'
import { FormActions } from './Form'

interface Props {
  aberto: boolean
  titulo: string
  /** O que vai acontecer, em uma ou duas frases. */
  children: ReactNode
  /** Nomeia a ação ("Excluir cliente"), nunca um "OK" genérico. */
  rotuloConfirmar: string
  aoConfirmar: () => Promise<unknown>
  aoFechar: () => void
}

/** Confirmação de ação destrutiva. Se a API recusar (ex.: 409), a mensagem aparece aqui dentro. */
export function ConfirmDialog({ aberto, titulo, children, rotuloConfirmar, aoConfirmar, aoFechar }: Props) {
  return (
    <Dialogo aberto={aberto} aoFechar={aoFechar} titulo={titulo}>
      <Corpo rotuloConfirmar={rotuloConfirmar} aoConfirmar={aoConfirmar} aoFechar={aoFechar}>
        {children}
      </Corpo>
    </Dialogo>
  )
}

function Corpo({ children, rotuloConfirmar, aoConfirmar, aoFechar }: Omit<Props, 'aberto' | 'titulo'>) {
  const [pendente, setPendente] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function confirmar() {
    setPendente(true)
    setErro(null)
    try {
      await aoConfirmar()
      aoFechar()
    } catch (e) {
      setErro(textoDoErro(e))
    } finally {
      setPendente(false)
    }
  }

  return (
    <>
      <p>{children}</p>
      {erro && <Alert>{erro}</Alert>}
      <FormActions>
        <Button variante="perigo" onClick={() => void confirmar()} carregando={pendente}>
          {rotuloConfirmar}
        </Button>
        <Button variante="secundario" onClick={aoFechar} disabled={pendente}>
          Cancelar
        </Button>
      </FormActions>
    </>
  )
}
