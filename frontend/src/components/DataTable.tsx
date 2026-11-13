import type { ReactNode } from 'react'
import estilos from './DataTable.module.css'

export interface Coluna<T> {
  chave: string
  rotulo: string
  render: (linha: T) => ReactNode
  /** Números e valores em dinheiro ficam alinhados à direita. */
  direita?: boolean
}

interface Props<T> {
  /** Descreve a tabela para leitores de tela. */
  legenda: string
  colunas: readonly Coluna<T>[]
  linhas: readonly T[]
  chaveLinha: (linha: T) => string | number
  carregando?: boolean
  vazio?: string
}

/**
 * Tabela que vira lista de cartões abaixo de 768 px (cada célula mostra o próprio rótulo).
 * Os roles explícitos mantêm a semântica de tabela mesmo com display:block no celular.
 * Entre 768 e 1024 px o contêiner rola por dentro, nunca a página.
 */
export function DataTable<T>({
  legenda,
  colunas,
  linhas,
  chaveLinha,
  carregando = false,
  vazio = 'Nada por aqui ainda.',
}: Props<T>) {
  if (carregando) {
    return (
      <p className={estilos.mensagem} role="status">
        Carregando…
      </p>
    )
  }
  if (linhas.length === 0) return <p className={estilos.mensagem}>{vazio}</p>

  return (
    <div className={estilos.rolagem}>
      <table className={estilos.tabela} role="table">
        <caption className={estilos.legenda}>{legenda}</caption>
        <thead role="rowgroup">
          <tr role="row">
            {colunas.map((c) => (
              <th key={c.chave} role="columnheader" scope="col" className={c.direita ? estilos.direita : undefined}>
                {c.rotulo}
              </th>
            ))}
          </tr>
        </thead>
        <tbody role="rowgroup">
          {linhas.map((linha) => (
            <tr key={chaveLinha(linha)} role="row">
              {colunas.map((c) => (
                <td key={c.chave} role="cell" data-rotulo={c.rotulo} className={c.direita ? estilos.direita : undefined}>
                  {c.render(linha)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
