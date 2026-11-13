import type { UseQueryResult } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { useLocation } from 'react-router'
import type { Pagina } from '../api/tipos'
import { textoDoErro } from '../api/problem'
import { Alert } from './Alert'
import { Button } from './Button'
import { Container } from './Container'
import { DataTable, type Coluna } from './DataTable'
import { PageHeader } from './PageHeader'
import { Paginacao } from './Paginacao'
import { SearchBox } from './SearchBox'
import { Stack } from './Stack'

interface Props<T> {
  titulo: string
  acoes?: ReactNode
  busca?: { rotulo: string; valor: string; aoBuscar: (texto: string) => void; dica?: string }
  /** Filtros extras acima da tabela (ex.: "Só em aberto"). */
  filtros?: ReactNode
  consulta: UseQueryResult<Pagina<T>>
  pagina: number
  aoMudarPagina: (pagina: number) => void
  colunas: readonly Coluna<T>[]
  chaveLinha: (linha: T) => string | number
  legenda: string
  /** Mensagem quando não há nenhum registro (com a dica do que fazer). */
  vazio: string
  /** Mensagem quando a busca/filtro não encontra nada. */
  vazioBusca: (termo: string) => string
}

/**
 * Lista paginada padrão: título, busca, tabela (cartões no celular), paginação e os estados de
 * carregando / erro / vazio. Também mostra o aviso "salvo" que o formulário deixa ao voltar.
 */
export function ListaPagina<T>({
  titulo,
  acoes,
  busca,
  filtros,
  consulta,
  pagina,
  aoMudarPagina,
  colunas,
  chaveLinha,
  legenda,
  vazio,
  vazioBusca,
}: Props<T>) {
  const { state } = useLocation()
  const aviso = (state as { aviso?: string } | null)?.aviso
  const dados = consulta.data

  return (
    <Container>
      <PageHeader titulo={titulo} acoes={acoes} />
      <Stack gap={4}>
        {aviso && <Alert tipo="sucesso">{aviso}</Alert>}
        {busca && <SearchBox {...busca} />}
        {filtros}

        {consulta.isError && !dados ? (
          <Stack gap={3}>
            <Alert>{textoDoErro(consulta.error)}</Alert>
            <div>
              <Button variante="secundario" onClick={() => void consulta.refetch()}>
                Tentar de novo
              </Button>
            </div>
          </Stack>
        ) : (
          <div aria-busy={consulta.isFetching} style={{ opacity: consulta.isPlaceholderData ? 0.6 : 1 }}>
            <DataTable
              legenda={legenda}
              colunas={colunas}
              linhas={dados?.content ?? []}
              chaveLinha={chaveLinha}
              carregando={consulta.isPending}
              vazio={busca?.valor ? vazioBusca(busca.valor) : vazio}
            />
            {dados && (
              <Paginacao
                pagina={pagina}
                totalPaginas={dados.page.totalPages}
                totalRegistros={dados.page.totalElements}
                aoMudar={aoMudarPagina}
                carregando={consulta.isFetching}
              />
            )}
          </div>
        )}
      </Stack>
    </Container>
  )
}
