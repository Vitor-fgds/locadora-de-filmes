import { Container } from './Container'
import { PageHeader } from './PageHeader'

// Placeholder das telas da Fase 6b.
export function EmBreve({ titulo }: { titulo: string }) {
  return (
    <Container>
      <PageHeader titulo={titulo} />
      <p>Esta tela ainda não foi construída.</p>
    </Container>
  )
}
