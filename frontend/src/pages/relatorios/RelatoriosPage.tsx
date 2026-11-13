import type { UseQueryResult } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { textoDoErro } from '../../api/problem'
import { useExemplaresPorFilme, useFaturamento, useFilmesComElenco } from '../../api/recursos/relatorios'
import type { ExemplaresPorFilme, FaturamentoFuncionario, FilmeComElenco } from '../../api/tipos'
import { Alert } from '../../components/Alert'
import { Button } from '../../components/Button'
import { Container } from '../../components/Container'
import { DataTable, type Coluna } from '../../components/DataTable'
import { PageHeader } from '../../components/PageHeader'
import { Stack } from '../../components/Stack'
import { dinheiro } from '../../lib/formato'
import estilos from './RelatoriosPage.module.css'

const colunasFaturamento: Coluna<FaturamentoFuncionario>[] = [
  { chave: 'funcionario', rotulo: 'Funcionário', render: (f) => f.nomeFuncionario },
  { chave: 'alugueis', rotulo: 'Aluguéis', direita: true, render: (f) => f.totalAlugueis },
  { chave: 'valorAlugueis', rotulo: 'Valor dos aluguéis', direita: true, render: (f) => dinheiro(f.valorAlugueis) },
  { chave: 'multas', rotulo: 'Multas', direita: true, render: (f) => dinheiro(f.valorMultas) },
  { chave: 'total', rotulo: 'Total', direita: true, render: (f) => <strong>{dinheiro(f.valorTotal)}</strong> },
]

const colunasExemplares: Coluna<ExemplaresPorFilme>[] = [
  { chave: 'filme', rotulo: 'Filme', render: (f) => f.nomeFilme },
  { chave: 'total', rotulo: 'Exemplares', direita: true, render: (f) => f.totalExemplares },
  { chave: 'disponiveis', rotulo: 'Disponíveis', direita: true, render: (f) => f.disponiveis },
]

const colunasElenco: Coluna<FilmeComElenco>[] = [
  { chave: 'filme', rotulo: 'Filme', render: (f) => f.nomeFilme },
  { chave: 'atores', rotulo: 'Atores no elenco', direita: true, render: (f) => f.totalAtores },
]

/** RF11 (só ADMIN): as três consultas rodam ao mesmo tempo; cada seção trata o próprio estado. */
export function RelatoriosPage() {
  const faturamento = useFaturamento()
  const exemplares = useExemplaresPorFilme()
  const elenco = useFilmesComElenco()

  const totais = faturamento.data?.reduce(
    (soma, f) => ({ alugueis: soma.alugueis + f.valorAlugueis, multas: soma.multas + f.valorMultas }),
    { alugueis: 0, multas: 0 },
  )

  return (
    <Container>
      <PageHeader titulo="Relatórios" />
      <Stack gap={7}>
        <Secao
          id="rel-faturamento"
          titulo="Faturamento por funcionário"
          descricao="Quanto cada funcionário registrou em aluguéis e em multas."
          consulta={faturamento}
          legenda="Faturamento por funcionário"
          colunas={colunasFaturamento}
          chaveLinha={(f) => f.idFuncionario}
          vazio="Nenhum aluguel registrado ainda."
          rodape={
            totais && (
              <p className={estilos.total}>
                Total geral: <strong>{dinheiro(totais.alugueis + totais.multas)}</strong> ({dinheiro(totais.alugueis)} em
                aluguéis + {dinheiro(totais.multas)} em multas)
              </p>
            )
          }
        />
        <Secao
          id="rel-exemplares"
          titulo="Exemplares por filme"
          descricao="Quantos exemplares cada filme tem e quantos estão livres agora."
          consulta={exemplares}
          legenda="Exemplares por filme"
          colunas={colunasExemplares}
          chaveLinha={(f) => f.idFilme}
          vazio="Nenhum filme com exemplares."
        />
        <Secao
          id="rel-elenco"
          titulo="Filmes com mais de um ator"
          descricao="Filmes que têm dois ou mais atores vinculados ao elenco."
          consulta={elenco}
          legenda="Filmes com mais de um ator"
          colunas={colunasElenco}
          chaveLinha={(f) => f.idFilme}
          vazio="Nenhum filme tem mais de um ator vinculado."
        />
      </Stack>
    </Container>
  )
}

function Secao<T>({
  id,
  titulo,
  descricao,
  consulta,
  legenda,
  colunas,
  chaveLinha,
  vazio,
  rodape,
}: {
  id: string
  titulo: string
  descricao: string
  consulta: UseQueryResult<T[]>
  legenda: string
  colunas: readonly Coluna<T>[]
  chaveLinha: (linha: T) => string | number
  vazio: string
  rodape?: ReactNode
}) {
  return (
    <section aria-labelledby={id}>
      <h2 id={id} className={estilos.titulo}>
        {titulo}
      </h2>
      <p className={estilos.descricao}>{descricao}</p>
      {consulta.isError ? (
        <Stack gap={3}>
          <Alert>{textoDoErro(consulta.error)}</Alert>
          <div>
            <Button variante="secundario" onClick={() => void consulta.refetch()}>
              Tentar de novo
            </Button>
          </div>
        </Stack>
      ) : (
        <>
          <DataTable
            legenda={legenda}
            colunas={colunas}
            linhas={consulta.data ?? []}
            chaveLinha={chaveLinha}
            carregando={consulta.isPending}
            vazio={vazio}
          />
          {rodape}
        </>
      )}
    </section>
  )
}
