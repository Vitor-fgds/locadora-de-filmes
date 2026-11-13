import estilos from './Spinner.module.css'

export function Spinner({ rotulo = 'Carregando…' }: { rotulo?: string }) {
  return (
    <div className={estilos.caixa} role="status">
      <span className={estilos.giro} aria-hidden="true" />
      <span>{rotulo}</span>
    </div>
  )
}
