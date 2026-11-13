import { useCallback } from 'react'
import { useSearchParams } from 'react-router'

/**
 * Busca e página de uma lista ficam na URL (?q=ana&pagina=2): voltar, recarregar e copiar o
 * link preservam o que o atendente estava vendo. Buscar de novo volta para a página 1.
 */
export function useListaUrl() {
  const [params, setParams] = useSearchParams()
  const q = params.get('q') ?? ''
  const pagina = Math.max(1, Math.trunc(Number(params.get('pagina'))) || 1)

  const setBusca = useCallback(
    (novo: string) =>
      setParams(
        (atual) => {
          const p = new URLSearchParams(atual)
          if (novo) p.set('q', novo)
          else p.delete('q')
          p.delete('pagina')
          return p
        },
        { replace: true },
      ),
    [setParams],
  )

  const setPagina = useCallback(
    (n: number) =>
      setParams((atual) => {
        const p = new URLSearchParams(atual)
        if (n > 1) p.set('pagina', String(n))
        else p.delete('pagina')
        return p
      }),
    [setParams],
  )

  return { q, pagina, setBusca, setPagina }
}
