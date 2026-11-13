import { Link } from 'react-router'
import { Container } from '../components/Container'
import { PageHeader } from '../components/PageHeader'

export function SemPermissaoPage() {
  return (
    <Container>
      <PageHeader titulo="Sem permissão" />
      <p>Seu perfil não tem acesso a esta tela. Peça a um administrador, se precisar dela.</p>
      <p>
        <Link to="/perfil">Voltar ao meu perfil</Link>
      </p>
    </Container>
  )
}
