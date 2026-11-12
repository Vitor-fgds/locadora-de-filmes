import estilos from './Perfuracao.module.css'

/**
 * Tira de perfurações de película: o único ornamento do sistema.
 * Posicione o elemento pai com `position: relative`; a tira cola na borda direita.
 */
export function Perfuracao({ lado = 'direita' }: { lado?: 'direita' | 'esquerda' }) {
  return <div className={`${estilos.tira} ${lado === 'esquerda' ? estilos.esquerda : estilos.direita}`} aria-hidden="true" />
}
