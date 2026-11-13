// Datas trafegam como "AAAA-MM-DD" (LocalDate do Java). Todas as contas abaixo usam só esse
// texto, sem `new Date("AAAA-MM-DD")`, que interpreta como UTC e mostra o dia anterior no Brasil.

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export const dinheiro = (valor: number): string => moeda.format(valor)

export const nomeCompleto = (pessoa: { primeiroNome: string; sobrenome: string }): string =>
  `${pessoa.primeiroNome} ${pessoa.sobrenome}`

/** "2026-11-05" → "05/11/2026". */
export function data(iso: string | null | undefined): string {
  if (!iso) return '—'
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

const dois = (n: number) => String(n).padStart(2, '0')

/** Hoje, no fuso do computador do atendente, como "AAAA-MM-DD". */
export function hojeISO(): string {
  const d = new Date()
  return `${d.getFullYear()}-${dois(d.getMonth() + 1)}-${dois(d.getDate())}`
}

function emDias(iso: string): number {
  const [ano, mes, dia] = iso.split('-').map(Number) as [number, number, number]
  return Date.UTC(ano, mes - 1, dia) / 86_400_000
}

/** Dias inteiros de `de` até `ate` (negativo se `ate` vem antes). */
export const diasEntre = (de: string, ate: string): number => emDias(ate) - emDias(de)

export function somarDias(iso: string, dias: number): string {
  const d = new Date(emDias(iso) * 86_400_000 + dias * 86_400_000)
  return `${d.getUTCFullYear()}-${dois(d.getUTCMonth() + 1)}-${dois(d.getUTCDate())}`
}

/** "38999998888" → "(38) 99999-8888". Outros formatos voltam como vieram. */
export function telefone(digitos: string): string {
  if (digitos.length === 11) return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 7)}-${digitos.slice(7)}`
  if (digitos.length === 10) return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 6)}-${digitos.slice(6)}`
  return digitos
}

/** Aceita "12,50" e "12.50"; devolve NaN se não for um valor com até 2 casas decimais. */
export function paraNumero(texto: string): number {
  const limpo = texto.trim().replace(/\s/g, '').replace(',', '.')
  return /^\d+(\.\d{1,2})?$/.test(limpo) ? Number(limpo) : Number.NaN
}

export const plural = (n: number, singular: string, pluralTexto: string): string =>
  `${n} ${n === 1 ? singular : pluralTexto}`
