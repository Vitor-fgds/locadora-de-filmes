import { useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router'
import { textoDoErro } from '../../api/problem'
import { atores, filmes } from '../../api/recursos/cadastros'
import {
  useAdicionarExemplar,
  useDesvincularAtor,
  useDisponiveis,
  useElenco,
  useExcluirExemplar,
  useExemplares,
  useMoverExemplar,
  useVincularAtor,
} from '../../api/recursos/filmeDetalhe'
import type { Ator, Exemplar, Filme } from '../../api/tipos'
import { usePode } from '../../auth/usePode'
import { Alert } from '../../components/Alert'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { ConfirmDialog } from '../../components/ConfirmDialog'
import { Container } from '../../components/Container'
import { DataTable, type Coluna } from '../../components/DataTable'
import { Dialogo } from '../../components/Dialogo'
import { EscolherRegistro } from '../../components/EscolherRegistro'
import { Etiqueta } from '../../components/Etiqueta'
import { PageHeader } from '../../components/PageHeader'
import { Paginacao } from '../../components/Paginacao'
import { Spinner } from '../../components/Spinner'
import { Stack } from '../../components/Stack'
import { data, nomeCompleto, plural } from '../../lib/formato'
import estilos from '../acoes.module.css'
import { NaoEncontradaPage } from '../NaoEncontradaPage'
import detalhe from './FilmeDetalhePage.module.css'

// Funções estáveis para o EscolherRegistro (ele as chama como hooks).
const useBuscaAtores = (q: string) => atores.useLista(q, 1)
const useBuscaFilmes = (q: string) => filmes.useLista(q, 1)

export function FilmeDetalhePage() {
  const { id: idTexto } = useParams()
  const id = Number(idTexto)
  if (!Number.isInteger(id)) return <NaoEncontradaPage />
  return <Detalhe id={id} />
}

function Detalhe({ id }: { id: number }) {
  const admin = usePode('ADMIN')
  const navigate = useNavigate()
  const { state } = useLocation()
  const aviso = (state as { aviso?: string } | null)?.aviso

  // As quatro consultas disparam juntas (cada hook é independente): nada espera pelo outro.
  const filme = filmes.useUm(id)
  const elenco = useElenco(id)
  const [paginaExemplares, setPaginaExemplares] = useState(1)
  const exemplares = useExemplares(id, paginaExemplares)
  const disponiveis = useDisponiveis(id)

  const excluirFilme = filmes.useExcluir()
  const adicionarExemplar = useAdicionarExemplar(id)
  const excluirExemplar = useExcluirExemplar()
  const desvincular = useDesvincularAtor(id)

  const [excluindoFilme, setExcluindoFilme] = useState(false)
  const [excluindoExemplar, setExcluindoExemplar] = useState<Exemplar | null>(null)
  const [movendo, setMovendo] = useState<Exemplar | null>(null)
  const [vinculando, setVinculando] = useState(false)
  const [removendo, setRemovendo] = useState<Ator | null>(null)

  if (filme.isPending) {
    return (
      <Container>
        <Spinner />
      </Container>
    )
  }
  if (filme.isError || !filme.data) {
    return (
      <Container>
        <PageHeader titulo="Filme" />
        <Stack gap={3}>
          <Alert>{textoDoErro(filme.error)}</Alert>
          <div>
            <BotaoLink to="/filmes" variante="secundario">
              Voltar para os filmes
            </BotaoLink>
          </div>
        </Stack>
      </Container>
    )
  }
  const f: Filme = filme.data

  const colunasExemplares: Coluna<Exemplar>[] = [
    { chave: 'id', rotulo: 'Exemplar', render: (e) => `Nº ${e.id}` },
    {
      chave: 'situacao',
      rotulo: 'Situação',
      render: (e) => (e.alugado ? <Etiqueta tom="neutro">Alugado</Etiqueta> : <Etiqueta tom="ok">Disponível</Etiqueta>),
    },
    ...(admin
      ? [
          {
            chave: 'acoes',
            rotulo: 'Ações',
            render: (e: Exemplar) => (
              <div className={estilos.acoes}>
                {!e.alugado && (
                  <Button variante="secundario" onClick={() => setMovendo(e)} aria-label={`Mover exemplar ${e.id} para outro filme`}>
                    Mover
                  </Button>
                )}
                <Button variante="secundario" onClick={() => setExcluindoExemplar(e)} aria-label={`Excluir exemplar ${e.id}`}>
                  Excluir
                </Button>
              </div>
            ),
          },
        ]
      : []),
  ]

  const colunasElenco: Coluna<Ator>[] = [
    { chave: 'nome', rotulo: 'Ator', render: nomeCompleto },
    ...(admin
      ? [
          {
            chave: 'acoes',
            rotulo: 'Ações',
            render: (a: Ator) => (
              <Button variante="secundario" onClick={() => setRemovendo(a)} aria-label={`Remover ${nomeCompleto(a)} do elenco`}>
                Remover do elenco
              </Button>
            ),
          },
        ]
      : []),
  ]

  const totalExemplares = exemplares.data?.page.totalElements
  const livres = disponiveis.data?.length

  return (
    <Container>
      <PageHeader
        titulo={f.nome}
        acoes={
          admin && (
            <>
              <BotaoLink to={`/filmes/${id}/editar`} variante="secundario">
                Editar filme
              </BotaoLink>
              <Button variante="perigo" onClick={() => setExcluindoFilme(true)}>
                Excluir filme
              </Button>
            </>
          )
        }
      />
      <Stack gap={6}>
        {aviso && <Alert tipo="sucesso">{aviso}</Alert>}

        <section aria-labelledby="sec-dados">
          <h2 id="sec-dados" className={detalhe.titulo}>
            Dados do filme
          </h2>
          <dl className={detalhe.dados}>
            <div>
              <dt>Lançamento</dt>
              <dd>{data(f.dataLancamento)}</dd>
            </div>
            <div>
              <dt>Duração</dt>
              <dd>{f.duracaoMinutos} min</dd>
            </div>
            <div>
              <dt>Descrição</dt>
              <dd className={detalhe.texto}>{f.descricao}</dd>
            </div>
          </dl>
        </section>

        <section aria-labelledby="sec-exemplares">
          <div className={detalhe.cabecalho}>
            <h2 id="sec-exemplares" className={detalhe.titulo}>
              Exemplares
            </h2>
            {admin && (
              <Button variante="secundario" onClick={() => adicionarExemplar.mutate()} carregando={adicionarExemplar.isPending}>
                Adicionar exemplar
              </Button>
            )}
          </div>
          {adicionarExemplar.isError && <Alert>{textoDoErro(adicionarExemplar.error)}</Alert>}
          {totalExemplares !== undefined && livres !== undefined && (
            <p className={detalhe.resumo}>
              {plural(totalExemplares, 'exemplar', 'exemplares')} no total ·{' '}
              <strong>{livres === 1 ? '1 disponível agora' : `${livres} disponíveis agora`}</strong>
            </p>
          )}
          {exemplares.isError && !exemplares.data ? (
            <Alert>{textoDoErro(exemplares.error)}</Alert>
          ) : (
            <>
              <DataTable
                legenda={`Exemplares de ${f.nome}`}
                colunas={colunasExemplares}
                linhas={exemplares.data?.content ?? []}
                chaveLinha={(e) => e.id}
                carregando={exemplares.isPending}
                vazio={admin ? 'Este filme ainda não tem exemplares. Use “Adicionar exemplar”.' : 'Este filme ainda não tem exemplares.'}
              />
              {exemplares.data && (
                <Paginacao
                  pagina={paginaExemplares}
                  totalPaginas={exemplares.data.page.totalPages}
                  totalRegistros={exemplares.data.page.totalElements}
                  aoMudar={setPaginaExemplares}
                  carregando={exemplares.isFetching}
                />
              )}
            </>
          )}
        </section>

        <section aria-labelledby="sec-elenco">
          <div className={detalhe.cabecalho}>
            <h2 id="sec-elenco" className={detalhe.titulo}>
              Elenco
            </h2>
            {admin && (
              <Button variante="secundario" onClick={() => setVinculando(true)}>
                Vincular ator
              </Button>
            )}
          </div>
          {elenco.isError && !elenco.data ? (
            <Alert>{textoDoErro(elenco.error)}</Alert>
          ) : (
            <DataTable
              legenda={`Elenco de ${f.nome}`}
              colunas={colunasElenco}
              linhas={elenco.data ?? []}
              chaveLinha={(a) => a.id}
              carregando={elenco.isPending}
              vazio={admin ? 'Nenhum ator vinculado. Use “Vincular ator”.' : 'Nenhum ator vinculado a este filme.'}
            />
          )}
        </section>
      </Stack>

      <ConfirmDialog
        aberto={excluindoFilme}
        titulo="Excluir filme"
        rotuloConfirmar="Excluir filme"
        aoConfirmar={async () => {
          await excluirFilme.mutateAsync(id)
          void navigate('/filmes', { state: { aviso: 'Filme excluído.' } })
        }}
        aoFechar={() => setExcluindoFilme(false)}
      >
        {`“${f.nome}” será removido do acervo e o elenco será desvinculado. Filmes com exemplares não podem ser excluídos.`}
      </ConfirmDialog>

      <ConfirmDialog
        aberto={excluindoExemplar !== null}
        titulo="Excluir exemplar"
        rotuloConfirmar="Excluir exemplar"
        aoConfirmar={() => excluirExemplar.mutateAsync(excluindoExemplar?.id ?? 0)}
        aoFechar={() => setExcluindoExemplar(null)}
      >
        {`O exemplar nº ${excluindoExemplar?.id ?? ''} será removido. Exemplares com aluguéis registrados não podem ser excluídos.`}
      </ConfirmDialog>

      <ConfirmDialog
        aberto={removendo !== null}
        titulo="Remover do elenco"
        rotuloConfirmar="Remover do elenco"
        aoConfirmar={() => desvincular.mutateAsync(removendo?.id ?? 0)}
        aoFechar={() => setRemovendo(null)}
      >
        {removendo ? `${nomeCompleto(removendo)} deixa de constar no elenco de “${f.nome}”. O cadastro do ator continua.` : ''}
      </ConfirmDialog>

      <Dialogo aberto={vinculando} aoFechar={() => setVinculando(false)} titulo="Vincular ator ao elenco">
        <VincularAtor idFilme={id} aoConcluir={() => setVinculando(false)} />
      </Dialogo>

      <Dialogo aberto={movendo !== null} aoFechar={() => setMovendo(null)} titulo={`Mover exemplar nº ${movendo?.id ?? ''}`}>
        {movendo && <MoverExemplar exemplar={movendo} aoConcluir={() => setMovendo(null)} />}
      </Dialogo>
    </Container>
  )
}

function VincularAtor({ idFilme, aoConcluir }: { idFilme: number; aoConcluir: () => void }) {
  const vincular = useVincularAtor(idFilme)
  return (
    <>
      {vincular.isError && <Alert>{textoDoErro(vincular.error)}</Alert>}
      <EscolherRegistro
        rotuloBusca="Buscar ator"
        useBusca={useBuscaAtores}
        chave={(a) => a.id}
        rotuloItem={nomeCompleto}
        aoEscolher={(a) => vincular.mutate(a.id, { onSuccess: aoConcluir })}
        vazio="Nenhum ator cadastrado."
      />
    </>
  )
}

function MoverExemplar({ exemplar, aoConcluir }: { exemplar: Exemplar; aoConcluir: () => void }) {
  const mover = useMoverExemplar()
  return (
    <>
      <p>Escolha o filme que passará a ter este exemplar.</p>
      {mover.isError && <Alert>{textoDoErro(mover.error)}</Alert>}
      <EscolherRegistro
        rotuloBusca="Buscar filme"
        useBusca={useBuscaFilmes}
        chave={(f) => f.id}
        rotuloItem={(f) => f.nome}
        aoEscolher={(f) => mover.mutate({ idExemplar: exemplar.id, idFilme: f.id }, { onSuccess: aoConcluir })}
        vazio="Nenhum filme cadastrado."
      />
    </>
  )
}
