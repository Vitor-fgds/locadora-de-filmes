import { ApiError } from './problem'

// Cliente HTTP do SPA. Mesma origem (proxy do Vite em dev), então o cookie de sessão
// vai sozinho. Em POST/PUT/PATCH/DELETE ecoa o cookie XSRF-TOKEN no header
// X-XSRF-TOKEN (CSRF modo SPA do Spring Security).

type Metodo = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export interface OpcoesRequisicao {
  method?: Metodo
  body?: unknown
  signal?: AbortSignal
  /** Login e /me recebem 401 como resposta normal: não disparam o "sessão expirou". */
  ignorar401?: boolean
}

const COOKIE_CSRF = 'XSRF-TOKEN'
const HEADER_CSRF = 'X-XSRF-TOKEN'

let aoPerderSessao: (() => void) | null = null

/** O app registra aqui o que fazer quando uma chamada autenticada volta 401. */
export function registrarAoPerderSessao(callback: (() => void) | null): void {
  aoPerderSessao = callback
}

function lerTokenCsrf(): string | undefined {
  const par = document.cookie.split('; ').find((c) => c.startsWith(`${COOKIE_CSRF}=`))
  return par ? decodeURIComponent(par.slice(COOKIE_CSRF.length + 1)) : undefined
}

async function obterTokenCsrf(forcar: boolean): Promise<string | undefined> {
  if (!forcar) {
    const atual = lerTokenCsrf()
    if (atual) return atual
  }
  // O GET gera o token e devolve o cookie XSRF-TOKEN na resposta.
  await fetch('/api/auth/csrf', { credentials: 'same-origin' })
  return lerTokenCsrf()
}

async function lerCorpo(resposta: Response): Promise<unknown> {
  if (resposta.status === 204) return undefined
  const texto = await resposta.text()
  if (!texto) return undefined
  try {
    return JSON.parse(texto) as unknown
  } catch {
    return undefined
  }
}

async function enviar(path: string, opcoes: OpcoesRequisicao, renovarCsrf: boolean): Promise<Response> {
  const metodo = opcoes.method ?? 'GET'
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (opcoes.body !== undefined) headers['Content-Type'] = 'application/json'
  if (metodo !== 'GET') {
    const token = await obterTokenCsrf(renovarCsrf)
    if (token) headers[HEADER_CSRF] = token
  }
  return fetch(path, {
    method: metodo,
    headers,
    credentials: 'same-origin',
    body: opcoes.body === undefined ? undefined : JSON.stringify(opcoes.body),
    signal: opcoes.signal ?? null,
  })
}

export async function api<T = void>(path: string, opcoes: OpcoesRequisicao = {}): Promise<T> {
  let resposta = await enviar(path, opcoes, false)
  let corpo = await lerCorpo(resposta)

  // Token CSRF vencido (ex.: sessão renovada em outra aba): renova e tenta uma única vez.
  if (resposta.status === 403 && new ApiError(403, corpo).detail?.includes('CSRF')) {
    resposta = await enviar(path, opcoes, true)
    corpo = await lerCorpo(resposta)
  }

  if (!resposta.ok) {
    if (resposta.status === 401 && !opcoes.ignorar401) aoPerderSessao?.()
    throw new ApiError(resposta.status, corpo)
  }
  return corpo as T
}
