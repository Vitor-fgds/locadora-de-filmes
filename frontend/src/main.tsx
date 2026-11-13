import { QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { registrarAoPerderSessao } from './api/http'
import { trocarSessao } from './auth/authApi'
import { queryClient } from './queryClient'
import { router } from './router'
import './styles/global.css'

// Qualquer chamada autenticada que volte 401 (sessão expirou, conta removida, perfil
// alterado) leva ao login com um aviso, guardando a tela de origem para voltar depois.
registrarAoPerderSessao(() => {
  const { pathname, search } = window.location
  if (pathname === '/login') return
  void router.navigate('/login?expirou=1', { replace: true, state: { from: pathname + search } })
  trocarSessao(queryClient, null)
})

const raiz = document.getElementById('root')
if (!raiz) throw new Error('Elemento #root não encontrado em index.html')

createRoot(raiz).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </StrictMode>,
)
