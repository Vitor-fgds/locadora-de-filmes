import { useState } from 'react'
import { clientes } from '../../api/recursos/cadastros'
import type { Cliente } from '../../api/tipos'
import { usePode } from '../../auth/usePode'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { ConfirmDialog } from '../../components/ConfirmDialog'
import type { Coluna } from '../../components/DataTable'
import { ListaPagina } from '../../components/ListaPagina'
import { nomeCompleto, telefone } from '../../lib/formato'
import { useListaUrl } from '../../lib/useListaUrl'
import estilos from '../acoes.module.css'

export function ClientesListaPage() {
  const { q, pagina, setBusca, setPagina } = useListaUrl()
  const consulta = clientes.useLista(q, pagina)
  const excluir = clientes.useExcluir()
  const admin = usePode('ADMIN') // só o ADMIN exclui (o backend também exige)
  const [excluindo, setExcluindo] = useState<Cliente | null>(null)

  const colunas: Coluna<Cliente>[] = [
    { chave: 'nome', rotulo: 'Nome', render: nomeCompleto },
    { chave: 'email', rotulo: 'Email', render: (c) => c.email },
    { chave: 'telefone', rotulo: 'Telefone', render: (c) => telefone(c.telefone) },
    { chave: 'cidade', rotulo: 'Cidade', render: (c) => c.cidade },
    {
      chave: 'acoes',
      rotulo: 'Ações',
      render: (c) => (
        <div className={estilos.acoes}>
          <BotaoLink to={`/clientes/${c.id}`} variante="secundario" aria-label={`Editar ${nomeCompleto(c)}`}>
            Editar
          </BotaoLink>
          {admin && (
            <Button variante="secundario" onClick={() => setExcluindo(c)} aria-label={`Excluir ${nomeCompleto(c)}`}>
              Excluir
            </Button>
          )}
        </div>
      ),
    },
  ]

  return (
    <>
      <ListaPagina
        titulo="Clientes"
        acoes={<BotaoLink to="/clientes/novo">Novo cliente</BotaoLink>}
        busca={{ rotulo: 'Buscar cliente', valor: q, aoBuscar: setBusca, dica: 'Pelo nome ou sobrenome' }}
        consulta={consulta}
        pagina={pagina}
        aoMudarPagina={setPagina}
        colunas={colunas}
        chaveLinha={(c) => c.id}
        legenda="Clientes cadastrados"
        vazio="Nenhum cliente cadastrado. Use “Novo cliente” para começar."
        vazioBusca={(t) => `Nenhum cliente com “${t}”. Tente outro nome.`}
      />
      <ConfirmDialog
        aberto={excluindo !== null}
        titulo="Excluir cliente"
        rotuloConfirmar="Excluir cliente"
        aoConfirmar={() => excluir.mutateAsync(excluindo?.id ?? 0)}
        aoFechar={() => setExcluindo(null)}
      >
        {excluindo ? `${nomeCompleto(excluindo)} será removido(a) do cadastro. Isso não pode ser desfeito.` : ''}
      </ConfirmDialog>
    </>
  )
}
