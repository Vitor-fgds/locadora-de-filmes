import { Link, type LinkProps } from 'react-router'
import estilos from './Button.module.css'

/** Link de navegação com a aparência de botão (mesmos estilos e alvo de 44 px do Button). */
export function BotaoLink({
  variante = 'primario',
  className,
  ...resto
}: LinkProps & { variante?: 'primario' | 'secundario' | 'perigo' }) {
  return <Link {...resto} className={`${estilos.botao} ${estilos[variante]} ${estilos.link} ${className ?? ''}`} />
}
