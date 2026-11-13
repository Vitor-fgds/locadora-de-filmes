import { rotuloPerfil } from '../auth/tipos'
import { useLogout, useUsuario } from '../auth/useUsuario'
import { Button } from '../components/Button'
import { Container } from '../components/Container'
import { PageHeader } from '../components/PageHeader'
import { Stack } from '../components/Stack'
import estilos from './PerfilPage.module.css'

export function PerfilPage() {
  const { data: usuario } = useUsuario()
  const logout = useLogout()
  if (!usuario) return null // Autenticado já garante o usuário; isto só satisfaz o TypeScript.

  return (
    <Container tamanho="estreito">
      <PageHeader titulo="Meu perfil" />
      <Stack gap={5}>
        <dl className={estilos.dados}>
          <div>
            <dt>Nome</dt>
            <dd>{usuario.nome}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{usuario.email}</dd>
          </div>
          <div>
            <dt>Perfil</dt>
            <dd>{rotuloPerfil[usuario.perfil]}</dd>
          </div>
        </dl>
        <div>
          <Button variante="secundario" onClick={() => logout.mutate()} carregando={logout.isPending}>
            Sair
          </Button>
        </div>
      </Stack>
    </Container>
  )
}
