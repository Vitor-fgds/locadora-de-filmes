import { z } from 'zod'

// ProblemDetail (RFC 9457) como o GlobalExceptionHandler do backend devolve.
// Tolerante: qualquer campo ausente ou fora do formato vira undefined, nunca estoura.
const erroDeCampo = z.object({ campo: z.string(), mensagem: z.string() })

const problemaSchema = z.object({
  title: z.string().optional(),
  detail: z.string().optional(),
  erros: z.array(erroDeCampo).optional().catch(undefined),
})

export type ErroDeCampo = z.infer<typeof erroDeCampo>

export class ApiError extends Error {
  readonly status: number
  readonly title: string | undefined
  readonly detail: string | undefined
  readonly erros: readonly ErroDeCampo[]

  constructor(status: number, corpo: unknown) {
    const problema = problemaSchema.safeParse(corpo)
    const dados = problema.success ? problema.data : {}
    super(dados.detail ?? dados.title ?? `Erro ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.title = dados.title
    this.detail = dados.detail
    this.erros = dados.erros ?? []
  }
}

export const ehApiError = (erro: unknown): erro is ApiError => erro instanceof ApiError

/** Texto pronto para mostrar ao usuário (em <Alert>, diálogo…) a partir de qualquer erro. */
export function textoDoErro(erro: unknown): string {
  if (!ehApiError(erro)) return 'Não foi possível falar com o servidor. Tente de novo.'
  if (erro.erros.length > 0) return erro.erros.map((e) => `${e.campo}: ${e.mensagem}`).join(' · ')
  return erro.detail ?? erro.title ?? 'Algo deu errado. Tente de novo.'
}
