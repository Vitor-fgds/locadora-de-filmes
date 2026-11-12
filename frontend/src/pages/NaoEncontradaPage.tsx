import { Link } from 'react-router'
import { Container } from '../components/Container'
import { PageHeader } from '../components/PageHeader'

export function NaoEncontradaPage() {
  return (
    <Container>
      <PageHeader titulo="Página não encontrada" />
      <p>O endereço não existe. Use o menu para chegar à tela que você procura.</p>
      <p>
        <Link to="/perfil">Voltar ao meu perfil</Link>
      </p>
    </Container>
  )
}
