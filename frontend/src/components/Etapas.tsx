import estilos from './Etapas.module.css'

interface Props {
  etapas: readonly string[]
  /** Etapa atual, começando em 0. */
  atual: number
}

/** Passo a passo de uma sequência real (aluguel). Celular: "Etapa 2 de 4"; a partir de 768 px: lista. */
export function Etapas({ etapas, atual }: Props) {
  return (
    <nav aria-label="Etapas do aluguel">
      <p className={estilos.resumo}>
        Etapa {atual + 1} de {etapas.length}: <strong>{etapas[atual]}</strong>
      </p>
      <ol className={estilos.lista}>
        {etapas.map((nome, i) => (
          <li
            key={nome}
            className={`${estilos.item} ${i === atual ? estilos.atual : ''} ${i < atual ? estilos.feita : ''}`}
            aria-current={i === atual ? 'step' : undefined}
          >
            <span className={estilos.numero} aria-hidden="true">
              {i + 1}
            </span>
            {nome}
          </li>
        ))}
      </ol>
    </nav>
  )
}
