import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { textoDoErro } from '../../api/problem'
import { useDevolver, useHistorico } from '../../api/recursos/alugueis'
import type { Aluguel } from '../../api/tipos'
import { Alert } from '../../components/Alert'
import { Button } from '../../components/Button'
import type { Coluna } from '../../components/DataTable'
import { Dialogo } from '../../components/Dialogo'
import { Etiqueta } from '../../components/Etiqueta'
import { Field } from '../../components/Field'
import { Form, FormActions } from '../../components/Form'
import { Input } from '../../components/Input'
import { ListaPagina } from '../../components/ListaPagina'
import { data, diasEntre, dinheiro, hojeISO, plural } from '../../lib/formato'
import estilos from './DevolucoesPage.module.css'

/** RF08: aluguéis em aberto. A multa não é calculada aqui: vem da stored procedure. */
export function DevolucoesPage() {
  const [pagina, setPagina] = useState(1)
  const consulta = useHistorico({ emAberto: true, pagina })
  const [devolvendo, setDevolvendo] = useState<Aluguel | null>(null)

  const colunas: Coluna<Aluguel>[] = [
    {
      chave: 'filme',
      rotulo: 'Filme',
      render: (a) => (
        <>
          <span className={estilos.bloco}>{a.nomeFilme}</span>
          <span className={`${estilos.bloco} ${estilos.discreto} ${estilos.semQuebra}`}>Exemplar nº {a.idExemplar}</span>
        </>
      ),
    },
    { chave: 'cliente', rotulo: 'Cliente', render: (a) => a.nomeCliente },
    {
      chave: 'entrega',
      rotulo: 'Entrega prevista',
      render: (a) => {
        const atraso = diasEntre(a.dataEntrega, hojeISO())
        return (
          <>
            {data(a.dataEntrega)}{' '}
            {atraso > 0 && <Etiqueta tom="atencao">Em atraso há {plural(atraso, 'dia', 'dias')}</Etiqueta>}
          </>
        )
      },
    },
    { chave: 'valor', rotulo: 'Valor', direita: true, render: (a) => dinheiro(a.valorAluguel) },
    {
      chave: 'acoes',
      rotulo: 'Ações',
      render: (a) => (
        <Button
          variante="secundario"
          onClick={() => setDevolvendo(a)}
          aria-label={`Registrar devolução de ${a.nomeFilme} (${a.nomeCliente})`}
        >
          Registrar devolução
        </Button>
      ),
    },
  ]

  return (
    <>
      <ListaPagina
        titulo="Devolução"
        consulta={consulta}
        pagina={pagina}
        aoMudarPagina={setPagina}
        colunas={colunas}
        chaveLinha={(a) => a.id}
        legenda="Aluguéis em aberto"
        vazio="Nenhum aluguel em aberto. Tudo devolvido."
        vazioBusca={() => ''}
      />
      <Dialogo
        aberto={devolvendo !== null}
        aoFechar={() => setDevolvendo(null)}
        titulo={devolvendo ? `Devolução de ${devolvendo.nomeFilme}` : ''}
      >
        {devolvendo && <Devolver aluguel={devolvendo} aoFechar={() => setDevolvendo(null)} />}
      </Dialogo>
    </>
  )
}

function Devolver({ aluguel, aoFechar }: { aluguel: Aluguel; aoFechar: () => void }) {
  const devolver = useDevolver()
  const [resultado, setResultado] = useState<Aluguel | null>(null)

  // Data da devolução: do dia do aluguel até hoje (a procedure também confere).
  const esquema = z.object({
    dataDevolucao: z
      .string()
      .min(1, 'Informe a data da devolução.')
      .refine((v) => v >= aluguel.dataAluguel, 'A devolução não pode ser antes do aluguel.')
      .refine((v) => v <= hojeISO(), 'A devolução não pode ser no futuro.'),
  })
  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<z.infer<typeof esquema>>({
    resolver: zodResolver(esquema),
    defaultValues: { dataDevolucao: hojeISO() },
  })

  const dataEscolhida = useWatch({ control, name: 'dataDevolucao' })
  const atrasoPrevisto = dataEscolhida ? Math.max(0, diasEntre(aluguel.dataEntrega, dataEscolhida)) : 0

  if (resultado) {
    const atraso = Math.max(0, diasEntre(resultado.dataEntrega, resultado.dataDevolucao ?? resultado.dataEntrega))
    return (
      <>
        <Alert tipo="sucesso">Devolução registrada em {data(resultado.dataDevolucao)}.</Alert>
        <dl className={estilos.resultado}>
          <div>
            <dt>Dias de atraso</dt>
            <dd>{atraso}</dd>
          </div>
          <div>
            <dt>Multa calculada pelo banco</dt>
            <dd>{resultado.valorMulta > 0 ? <Etiqueta tom="atencao">{dinheiro(resultado.valorMulta)}</Etiqueta> : dinheiro(0)}</dd>
          </div>
          <div>
            <dt>Valor do aluguel</dt>
            <dd>{dinheiro(resultado.valorAluguel)}</dd>
          </div>
          <div className={estilos.total}>
            <dt>Total a receber</dt>
            <dd>{dinheiro(resultado.valorAluguel + resultado.valorMulta)}</dd>
          </div>
        </dl>
        <FormActions>
          <Button onClick={aoFechar}>Fechar</Button>
        </FormActions>
      </>
    )
  }

  const enviar = handleSubmit((dados) => {
    devolver.mutate({ id: aluguel.id, dataDevolucao: dados.dataDevolucao }, { onSuccess: setResultado })
  })

  return (
    <Form onSubmit={enviar}>
      <p>
        {aluguel.nomeCliente} · exemplar nº {aluguel.idExemplar} · entrega prevista em {data(aluguel.dataEntrega)}
      </p>
      {devolver.isError && <Alert>{textoDoErro(devolver.error)}</Alert>}
      <Field rotulo="Data da devolução" erro={errors.dataDevolucao?.message}>
        {(c) => <Input {...c} {...register('dataDevolucao')} type="date" min={aluguel.dataAluguel} max={hojeISO()} />}
      </Field>
      {atrasoPrevisto > 0 ? (
        <Alert tipo="aviso">
          Atraso previsto: {plural(atrasoPrevisto, 'dia', 'dias')}. O valor da multa é calculado pelo banco de dados ao
          confirmar.
        </Alert>
      ) : (
        <p className={estilos.discreto}>Sem atraso nesta data. O banco confirma o valor da multa ao registrar.</p>
      )}
      <FormActions>
        <Button type="submit" carregando={devolver.isPending}>
          Confirmar devolução
        </Button>
        <Button variante="secundario" onClick={aoFechar} disabled={devolver.isPending}>
          Cancelar
        </Button>
      </FormActions>
    </Form>
  )
}
