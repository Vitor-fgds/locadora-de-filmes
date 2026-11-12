import { useState } from 'react'
import { funcionarios } from '../../api/recursos/cadastros'
import type { Funcionario } from '../../api/tipos'
import { rotuloPerfil } from '../../auth/tipos'
import { useUsuario } from '../../auth/useUsuario'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { ConfirmDialog } from '../../components/ConfirmDialog'
import type { Coluna } from '../../components/DataTable'
import { Etiqueta } from '../../components/Etiqueta'
import { ListaPagina } from '../../components/ListaPagina'
import { nomeCompleto, telefone } from '../../lib/formato'
import { useListaUrl } from '../../lib/useListaUrl'
import estilos from '../acoes.module.css'

// Tela só do ADMIN (a rota já exige o perfil; o backend também).
export function FuncionariosListaPage() {
  const { q, pagina, setBusca, setPagina } = useListaUrl()
  const consulta = funcionarios.useLista(q, pagina)
  const excluir = funcionarios.useExcluir()
  const { data: eu } = useUsuario()
  const [excluindo, setExcluindo] = useState<Funcionario | null>(null)

  const colunas: Coluna<Funcionario>[] = [
    { chave: 'nome', rotulo: 'Nome', render: nomeCompleto },
    { chave: 'email', rotulo: 'Email', render: (f) => f.email },
    { chave: 'telefone', rotulo: 'Telefone', render: (f) => telefone(f.telefone) },
    {
      chave: 'perfil',
      rotulo: 'Perfil',
      render: (f) => <Etiqueta tom={f.perfil === 'ADMIN' ? 'ok' : 'neutro'}>{rotuloPerfil[f.perfil]}</Etiqueta>,
    },
    {
      chave: 'acoes',
      rotulo: 'Ações',
      render: (f) => (
        <div className={estilos.acoes}>
          <BotaoLink to={`/funcionarios/${f.id}`} variante="secundario" aria-label={`Editar ${nomeCompleto(f)}`}>
            Editar
          </BotaoLink>
          {/* A própria conta não pode ser excluída (o backend recusa com 422). */}
          {f.id !== eu?.id && (
            <Button variante="secundario" onClick={() => setExcluindo(f)} aria-label={`Excluir ${nomeCompleto(f)}`}>
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
        titulo="Funcionários"
        acoes={<BotaoLink to="/funcionarios/novo">Novo funcionário</BotaoLink>}
        busca={{ rotulo: 'Buscar funcionário', valor: q, aoBuscar: setBusca, dica: 'Pelo nome ou sobrenome' }}
        consulta={consulta}
        pagina={pagina}
        aoMudarPagina={setPagina}
        colunas={colunas}
        chaveLinha={(f) => f.id}
        legenda="Funcionários cadastrados"
        vazio="Nenhum funcionário cadastrado."
        vazioBusca={(t) => `Nenhum funcionário com “${t}”. Tente outro nome.`}
      />
      <ConfirmDialog
        aberto={excluindo !== null}
        titulo="Excluir funcionário"
        rotuloConfirmar="Excluir funcionário"
        aoConfirmar={() => excluir.mutateAsync(excluindo?.id ?? 0)}
        aoFechar={() => setExcluindo(null)}
      >
        {excluindo
          ? `${nomeCompleto(excluindo)} perde o acesso ao sistema. Quem já registrou aluguéis não pode ser excluído.`
          : ''}
      </ConfirmDialog>
    </>
  )
}
