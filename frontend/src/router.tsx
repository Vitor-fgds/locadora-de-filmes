import type { ComponentType } from 'react'
import { createBrowserRouter, Navigate, type RouteObject } from 'react-router'
import { Autenticado, ExigePerfil } from './auth/Guardas'
import type { Perfil } from './auth/tipos'
import { Spinner } from './components/Spinner'
import { LoginPage } from './pages/LoginPage'
import { NaoEncontradaPage } from './pages/NaoEncontradaPage'
import { PerfilPage } from './pages/PerfilPage'

/**
 * Rota carregada sob demanda: o código da tela só baixa na primeira visita (o login e o menu
 * abrem mais rápido). `minimo` esconde a tela de quem não tem o perfil; o backend é a barreira real.
 */
function rota(path: string, carregar: () => Promise<ComponentType>, minimo?: Perfil): RouteObject {
  return {
    path,
    lazy: async () => {
      const Tela = await carregar()
      return {
        Component: () =>
          minimo ? (
            <ExigePerfil minimo={minimo}>
              <Tela />
            </ExigePerfil>
          ) : (
            <Tela />
          ),
      }
    },
  }
}

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <Autenticado />,
    hydrateFallbackElement: <Spinner />,
    children: [
      { index: true, element: <Navigate to="/alugueis/novo" replace /> },
      { path: 'perfil', element: <PerfilPage /> },

      rota('alugueis/novo', () => import('./pages/aluguel/NovoAluguelPage').then((m) => m.NovoAluguelPage)),
      rota('devolucoes', () => import('./pages/aluguel/DevolucoesPage').then((m) => m.DevolucoesPage)),
      rota('historico', () => import('./pages/aluguel/HistoricoPage').then((m) => m.HistoricoPage)),

      rota('clientes', () => import('./pages/clientes/ClientesListaPage').then((m) => m.ClientesListaPage)),
      rota('clientes/novo', () => import('./pages/clientes/ClienteFormPage').then((m) => m.ClienteFormPage)),
      rota('clientes/:id', () => import('./pages/clientes/ClienteFormPage').then((m) => m.ClienteFormPage)),

      rota('filmes', () => import('./pages/filmes/FilmesListaPage').then((m) => m.FilmesListaPage)),
      rota('filmes/novo', () => import('./pages/filmes/FilmeFormPage').then((m) => m.FilmeFormPage), 'ADMIN'),
      rota('filmes/:id', () => import('./pages/filmes/FilmeDetalhePage').then((m) => m.FilmeDetalhePage)),
      rota('filmes/:id/editar', () => import('./pages/filmes/FilmeFormPage').then((m) => m.FilmeFormPage), 'ADMIN'),

      rota('atores', () => import('./pages/atores/AtoresListaPage').then((m) => m.AtoresListaPage)),
      rota('atores/novo', () => import('./pages/atores/AtorFormPage').then((m) => m.AtorFormPage), 'ADMIN'),
      rota('atores/:id', () => import('./pages/atores/AtorFormPage').then((m) => m.AtorFormPage), 'ADMIN'),

      rota('funcionarios', () => import('./pages/funcionarios/FuncionariosListaPage').then((m) => m.FuncionariosListaPage), 'ADMIN'),
      rota('funcionarios/novo', () => import('./pages/funcionarios/FuncionarioFormPage').then((m) => m.FuncionarioFormPage), 'ADMIN'),
      rota('funcionarios/:id', () => import('./pages/funcionarios/FuncionarioFormPage').then((m) => m.FuncionarioFormPage), 'ADMIN'),

      rota('relatorios', () => import('./pages/relatorios/RelatoriosPage').then((m) => m.RelatoriosPage), 'ADMIN'),

      { path: '*', element: <NaoEncontradaPage /> },
    ],
  },
])
