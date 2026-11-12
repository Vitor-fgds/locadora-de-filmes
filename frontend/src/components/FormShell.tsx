import type { ReactNode } from 'react'
import { textoDoErro } from '../api/problem'
import { Alert } from './Alert'
import { BotaoLink } from './BotaoLink'
import { Container } from './Container'
import { PageHeader } from './PageHeader'
import { Spinner } from './Spinner'

interface Props {
  titulo: string
  /** Enquanto carrega o registro a editar. */
  carregando?: boolean
  /** Falha ao carregar o registro. */
  erro?: unknown
  voltarPara: string
  children: ReactNode
}

/** Moldura das telas de cadastro: cabeçalho, carregamento do registro e falha de carga. */
export function FormShell({ titulo, carregando = false, erro, voltarPara, children }: Props) {
  return (
    <Container tamanho="estreito">
      <PageHeader titulo={titulo} />
      {carregando ? (
        <Spinner />
      ) : erro ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--espaco-3)' }}>
          <Alert>{textoDoErro(erro)}</Alert>
          <div>
            <BotaoLink to={voltarPara} variante="secundario">
              Voltar para a lista
            </BotaoLink>
          </div>
        </div>
      ) : (
        children
      )}
    </Container>
  )
}
