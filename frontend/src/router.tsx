import { createBrowserRouter, Navigate } from 'react-router'
import { Autenticado, ExigePerfil } from './auth/Guardas'
import { itensMenu } from './auth/menu'
import { EmBreve } from './components/EmBreve'
import { LoginPage } from './pages/LoginPage'
import { NaoEncontradaPage } from './pages/NaoEncontradaPage'
import { PerfilPage } from './pages/PerfilPage'

// Telas da Fase 6b: já têm rota e controle de perfil; o conteúdo vem depois.
const telasEmBreve = itensMenu
  .filter((item) => item.caminho !== '/perfil')
  .map((item) => ({
    path: item.caminho,
    element: (
      <ExigePerfil minimo={item.minimo}>
        <EmBreve titulo={item.rotulo} />
      </ExigePerfil>
    ),
  }))

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <Autenticado />,
    children: [
      { index: true, element: <Navigate to="/perfil" replace /> },
      { path: 'perfil', element: <PerfilPage /> },
      ...telasEmBreve,
      { path: '*', element: <NaoEncontradaPage /> },
    ],
  },
])
