import { QueryClient } from '@tanstack/react-query'
import { ehApiError } from './api/problem'

// Erro 4xx é resposta definitiva do servidor (credencial, validação, permissão):
// repetir não muda nada. Só falha de rede e 5xx merecem nova tentativa.
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (tentativas, erro) => tentativas < 2 && !(ehApiError(erro) && erro.status < 500),
      refetchOnWindowFocus: false,
    },
  },
})
