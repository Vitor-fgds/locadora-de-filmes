import { useState } from 'react'
import { atores } from '../../api/recursos/cadastros'
import type { Ator } from '../../api/tipos'
import { usePode } from '../../auth/usePode'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { ConfirmDialog } from '../../components/ConfirmDialog'
import type { Coluna } from '../../components/DataTable'
import { ListaPagina } from '../../components/ListaPagina'
import { nomeCompleto } from '../../lib/formato'
import { useListaUrl } from '../../lib/useListaUrl'
import estilos from '../acoes.module.css'

export function AtoresListaPage() {
  const { q, pagina, setBusca, setPagina } = useListaUrl()
  const consulta = atores.useLista(q, pagina)
  const excluir = atores.useExcluir()
  const admin = usePode('ADMIN') // ATENDENTE só consulta
  const [excluindo, setExcluindo] = useState<Ator | null>(null)

  const colunas: Coluna<Ator>[] = [
    { chave: 'nome', rotulo: 'Nome', render: nomeCompleto },
    ...(admin
      ? [
          {
            chave: 'acoes',
            rotulo: 'Ações',
            render: (a: Ator) => (
              <div className={estilos.acoes}>
                <BotaoLink to={`/atores/${a.id}`} variante="secundario" aria-label={`Editar ${nomeCompleto(a)}`}>
                  Editar
                </BotaoLink>
                <Button variante="secundario" onClick={() => setExcluindo(a)} aria-label={`Excluir ${nomeCompleto(a)}`}>
                  Excluir
                </Button>
              </div>
            ),
          },
        ]
      : []),
  ]

  return (
    <>
      <ListaPagina
        titulo="Atores"
        acoes={admin ? <BotaoLink to="/atores/novo">Novo ator</BotaoLink> : undefined}
        busca={{ rotulo: 'Buscar ator', valor: q, aoBuscar: setBusca, dica: 'Pelo nome ou sobrenome' }}
        consulta={consulta}
        pagina={pagina}
        aoMudarPagina={setPagina}
        colunas={colunas}
        chaveLinha={(a) => a.id}
        legenda="Atores cadastrados"
        vazio={admin ? 'Nenhum ator cadastrado. Use “Novo ator” para começar.' : 'Nenhum ator cadastrado.'}
        vazioBusca={(t) => `Nenhum ator com “${t}”. Tente outro nome.`}
      />
      <ConfirmDialog
        aberto={excluindo !== null}
        titulo="Excluir ator"
        rotuloConfirmar="Excluir ator"
        aoConfirmar={() => excluir.mutateAsync(excluindo?.id ?? 0)}
        aoFechar={() => setExcluindo(null)}
      >
        {excluindo
          ? `${nomeCompleto(excluindo)} será removido(a) do cadastro e de todos os elencos. Isso não pode ser desfeito.`
          : ''}
      </ConfirmDialog>
    </>
  )
}
