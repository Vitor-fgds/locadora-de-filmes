import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ApiError, ehApiError } from './problem'

/**
 * Leva o ProblemDetail do backend para o formulário:
 * - `erros[{campo, mensagem}]` (validação) → erro no campo correspondente;
 * - qualquer outro erro → `errors.root.servidor`, exibido num <Alert> no topo.
 * Campo desconhecido pelo formulário também cai em `root.servidor`.
 */
export function aplicarErrosNoFormulario<T extends FieldValues>(
  erro: unknown,
  setError: UseFormSetError<T>,
  camposDoFormulario: readonly string[],
): void {
  if (!ehApiError(erro)) {
    setError('root.servidor', { message: 'Não foi possível falar com o servidor. Tente de novo.' })
    return
  }
  const conhecidos = new Set(camposDoFormulario)
  const restantes: string[] = []
  for (const { campo, mensagem } of erro.erros) {
    if (conhecidos.has(campo)) setError(campo as Path<T>, { message: mensagem })
    else restantes.push(`${campo}: ${mensagem}`)
  }
  if (erro.erros.length === 0 || restantes.length > 0) {
    setError('root.servidor', { message: mensagemDe(erro, restantes) })
  }
}

function mensagemDe(erro: ApiError, restantes: string[]): string {
  if (restantes.length > 0) return restantes.join(' · ')
  return erro.detail ?? erro.title ?? 'Algo deu errado. Tente de novo.'
}
