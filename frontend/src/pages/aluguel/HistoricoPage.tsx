import { useState } from 'react'
import { useHistorico } from '../../api/recursos/alugueis'
import { clientes } from '../../api/recursos/cadastros'
import type { Aluguel } from '../../api/tipos'
import { Button } from '../../components/Button'
import type { Coluna } from '../../components/DataTable'
import { Dialogo } from '../../components/Dialogo'
import { EscolherRegistro } from '../../components/EscolherRegistro'
import { Etiqueta } from '../../components/Etiqueta'
import { ListaPagina } from '../../components/ListaPagina'
import { data, diasEntre, dinheiro, hojeISO, nomeCompleto, plural } from '../../lib/formato'
import type { Cliente } from '../../api/tipos'
import estilos from './HistoricoPage.module.css'

const useBuscaClientes = (q: string) => clientes.useLista(q, 1)

function situacao(a: Aluguel) {
  if (a.dataDevolucao) return <Etiqueta tom="ok">Devolvido</Etiqueta>
  const atraso = diasEntre(a.dataEntrega, hojeISO())
  return atraso > 0 ? (
    <Etiqueta tom="atencao">Em atraso há {plural(atraso, 'dia', 'dias')}</Etiqueta>
  ) : (
    <Etiqueta tom="neutro">Em aberto</Etiqueta>
  )
}

// Poucas colunas, com os dados relacionados empilhados: cabe ao lado do menu sem rolar.
const colunas: Coluna<Aluguel>[] = [
  {
    chave: 'filme',
    rotulo: 'Filme',
    render: (a) => (
      <>
        <span className={estilos.bloco}>{a.nomeFilme}</span>
        <span className={`${estilos.bloco} ${estilos.discreto} ${estilos.semQuebra}`}>Exemplar nº {a.idExemplar}</span>
      </>
    ),
  },
  {
    chave: 'cliente',
    rotulo: 'Cliente',
    render: (a) => (
      <>
        <span className={estilos.linha}>{a.nomeCliente}</span>
        <span className={`${estilos.linha} ${estilos.discreto}`}>Atendido por {a.nomeFuncionario}</span>
      </>
    ),
  },
  {
    chave: 'datas',
    rotulo: 'Datas',
    render: (a) => (
      <>
        <span className={estilos.data}>Saída {data(a.dataAluguel)}</span>
        <span className={estilos.data}>Entrega {data(a.dataEntrega)}</span>
        {a.dataDevolucao && <span className={estilos.data}>Devolução {data(a.dataDevolucao)}</span>}
      </>
    ),
  },
  {
    chave: 'valores',
    rotulo: 'Valores',
    direita: true,
    render: (a) => (
      <>
        <span className={estilos.linha}>{dinheiro(a.valorAluguel)}</span>
        {a.valorMulta > 0 && <span className={estilos.linha}>+ multa {dinheiro(a.valorMulta)}</span>}
      </>
    ),
  },
  { chave: 'situacao', rotulo: 'Situação', render: situacao },
]

/** RF10: histórico de aluguéis com cliente, funcionário e filme (JOIN no backend). */
export function HistoricoPage() {
  const [pagina, setPagina] = useState(1)
  const [emAberto, setEmAberto] = useState(false)
  const [cliente, setCliente] = useState<Cliente | null>(null)
  const [escolhendo, setEscolhendo] = useState(false)
  const consulta = useHistorico({ idCliente: cliente?.id, emAberto, pagina })

  return (
    <>
      <ListaPagina
        titulo="Histórico de aluguéis"
        filtros={
          <div className={estilos.filtros}>
            <label className={estilos.marcar}>
              <input
                type="checkbox"
                checked={emAberto}
                onChange={(e) => {
                  setEmAberto(e.target.checked)
                  setPagina(1)
                }}
              />
              Só aluguéis em aberto
            </label>
            {cliente ? (
              <div className={estilos.cliente}>
                <span>
                  Cliente: <strong>{nomeCompleto(cliente)}</strong>
                </span>
                <Button
                  variante="secundario"
                  onClick={() => {
                    setCliente(null)
                    setPagina(1)
                  }}
                >
                  Limpar filtro de cliente
                </Button>
              </div>
            ) : (
              <Button variante="secundario" onClick={() => setEscolhendo(true)}>
                Filtrar por cliente
              </Button>
            )}
          </div>
        }
        consulta={consulta}
        pagina={pagina}
        aoMudarPagina={setPagina}
        colunas={colunas}
        chaveLinha={(a) => a.id}
        legenda="Aluguéis registrados"
        vazio={emAberto || cliente ? 'Nenhum aluguel com esses filtros.' : 'Nenhum aluguel registrado ainda.'}
        vazioBusca={() => ''}
      />
      <Dialogo aberto={escolhendo} aoFechar={() => setEscolhendo(false)} titulo="Filtrar por cliente">
        <EscolherRegistro
          rotuloBusca="Buscar cliente"
          useBusca={useBuscaClientes}
          chave={(c) => c.id}
          rotuloItem={(c) => nomeCompleto(c)}
          aoEscolher={(c) => {
            setCliente(c)
            setPagina(1)
            setEscolhendo(false)
          }}
          vazio="Nenhum cliente cadastrado."
        />
      </Dialogo>
    </>
  )
}
