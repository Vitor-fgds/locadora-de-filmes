import type { UseQueryResult } from '@tanstack/react-query'
import { useState, type ReactNode } from 'react'
import { textoDoErro } from '../api/problem'
import type { Pagina } from '../api/tipos'
import { Alert } from './Alert'
import estilos from './EscolherRegistro.module.css'
import { SearchBox } from './SearchBox'
import { Spinner } from './Spinner'

interface Props<T> {
  rotuloBusca: string
  /** Hook que consulta a primeira página da lista pelo texto digitado. Deve ser uma função estável. */
  useBusca: (texto: string) => UseQueryResult<Pagina<T>>
  chave: (item: T) => number
  rotuloItem: (item: T) => ReactNode
  aoEscolher: (item: T) => void
  vazio: string
}

/** Busca + lista de botões grandes para escolher um registro (filme, cliente, ator…). */
export function EscolherRegistro<T>({ rotuloBusca, useBusca, chave, rotuloItem, aoEscolher, vazio }: Props<T>) {
  const [texto, setTexto] = useState('')
  const consulta = useBusca(texto)
  const dados = consulta.data

  return (
    <div className={estilos.caixa}>
      <SearchBox rotulo={rotuloBusca} valor={texto} aoBuscar={setTexto} />
      {consulta.isError && !dados && <Alert>{textoDoErro(consulta.error)}</Alert>}
      {consulta.isPending && <Spinner />}
      {dados && dados.content.length === 0 && <p>{texto ? `Nada encontrado para "${texto}".` : vazio}</p>}
      {dados && dados.content.length > 0 && (
        <>
          <ul className={estilos.lista} aria-busy={consulta.isPlaceholderData}>
            {dados.content.map((item) => (
              <li key={chave(item)}>
                <button type="button" className={estilos.opcao} onClick={() => aoEscolher(item)}>
                  {rotuloItem(item)}
                </button>
              </li>
            ))}
          </ul>
          {dados.page.totalElements > dados.content.length && (
            <p className={estilos.nota}>
              Mostrando {dados.content.length} de {dados.page.totalElements}. Digite mais letras para refinar.
            </p>
          )}
        </>
      )}
    </div>
  )
}
