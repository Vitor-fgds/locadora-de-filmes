import { filmes } from '../../api/recursos/cadastros'
import type { Filme } from '../../api/tipos'
import { usePode } from '../../auth/usePode'
import { BotaoLink } from '../../components/BotaoLink'
import type { Coluna } from '../../components/DataTable'
import { ListaPagina } from '../../components/ListaPagina'
import { data } from '../../lib/formato'
import { useListaUrl } from '../../lib/useListaUrl'

export function FilmesListaPage() {
  const { q, pagina, setBusca, setPagina } = useListaUrl()
  const consulta = filmes.useLista(q, pagina)
  const admin = usePode('ADMIN') // só o ADMIN cadastra; todos consultam exemplares e elenco

  const colunas: Coluna<Filme>[] = [
    { chave: 'nome', rotulo: 'Filme', render: (f) => f.nome },
    { chave: 'lancamento', rotulo: 'Lançamento', render: (f) => data(f.dataLancamento) },
    { chave: 'duracao', rotulo: 'Duração', render: (f) => `${f.duracaoMinutos} min` },
    {
      chave: 'acoes',
      rotulo: 'Ações',
      render: (f) => (
        <BotaoLink to={`/filmes/${f.id}`} variante="secundario" aria-label={`Ver exemplares e elenco de ${f.nome}`}>
          Exemplares e elenco
        </BotaoLink>
      ),
    },
  ]

  return (
    <ListaPagina
      titulo="Filmes"
      acoes={admin ? <BotaoLink to="/filmes/novo">Novo filme</BotaoLink> : undefined}
      busca={{ rotulo: 'Buscar filme', valor: q, aoBuscar: setBusca, dica: 'Pelo título' }}
      consulta={consulta}
      pagina={pagina}
      aoMudarPagina={setPagina}
      colunas={colunas}
      chaveLinha={(f) => f.id}
      legenda="Filmes do acervo"
      vazio={admin ? 'Nenhum filme no acervo. Use “Novo filme” para começar.' : 'Nenhum filme no acervo.'}
      vazioBusca={(t) => `Nenhum filme com “${t}”. Tente outro título.`}
    />
  )
}
