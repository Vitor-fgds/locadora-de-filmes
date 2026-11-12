import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { aplicarErrosNoFormulario } from '../../api/formErrors'
import { ehApiError, textoDoErro } from '../../api/problem'
import { useRegistrarAluguel } from '../../api/recursos/alugueis'
import { clientes, filmes } from '../../api/recursos/cadastros'
import { useDisponiveis } from '../../api/recursos/filmeDetalhe'
import type { Aluguel, Cliente, ExemplarDisponivel, Filme } from '../../api/tipos'
import { Alert } from '../../components/Alert'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { Container } from '../../components/Container'
import { EscolherRegistro } from '../../components/EscolherRegistro'
import { Etapas } from '../../components/Etapas'
import { Field } from '../../components/Field'
import { Form, FormActions, FormGrid } from '../../components/Form'
import { Input } from '../../components/Input'
import { PageHeader } from '../../components/PageHeader'
import { Spinner } from '../../components/Spinner'
import { Stack } from '../../components/Stack'
import { data, dinheiro, hojeISO, nomeCompleto, paraNumero, somarDias } from '../../lib/formato'
import estilos from './NovoAluguelPage.module.css'

const ETAPAS = ['Filme', 'Exemplar', 'Cliente', 'Confirmação'] as const
const DIAS_PADRAO = 3

// Funções estáveis para o EscolherRegistro (ele as chama como hooks).
const useBuscaFilmes = (q: string) => filmes.useLista(q, 1)
const useBuscaClientes = (q: string) => clientes.useLista(q, 1)

/**
 * RF07 em quatro etapas: filme → exemplar disponível → cliente → confirmação.
 * Só a última chama a API (POST /api/alugueis, uma transação no backend).
 */
export function NovoAluguelPage() {
  const [etapa, setEtapa] = useState(0)
  const [filme, setFilme] = useState<Filme | null>(null)
  const [exemplar, setExemplar] = useState<ExemplarDisponivel | null>(null)
  const [cliente, setCliente] = useState<Cliente | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [feito, setFeito] = useState<Aluguel | null>(null)

  function recomecar() {
    setEtapa(0)
    setFilme(null)
    setExemplar(null)
    setCliente(null)
    setAviso(null)
    setFeito(null)
  }

  if (feito) {
    return (
      <Container>
        <PageHeader titulo="Aluguel registrado" />
        <Stack gap={5}>
          <Alert tipo="sucesso">Aluguel nº {feito.id} registrado.</Alert>
          <dl className={estilos.resumo}>
            <Linha rotulo="Filme" valor={`${feito.nomeFilme} (exemplar nº ${feito.idExemplar})`} />
            <Linha rotulo="Cliente" valor={feito.nomeCliente} />
            <Linha rotulo="Entrega prevista" valor={data(feito.dataEntrega)} />
            <Linha rotulo="Valor do aluguel" valor={dinheiro(feito.valorAluguel)} />
          </dl>
          <div className={estilos.fim}>
            <Button onClick={recomecar}>Registrar outro aluguel</Button>
            <BotaoLink to="/historico" variante="secundario">
              Ver no histórico
            </BotaoLink>
          </div>
        </Stack>
      </Container>
    )
  }

  return (
    <Container>
      <PageHeader titulo="Novo aluguel" />
      <Stack gap={5}>
        <Etapas etapas={ETAPAS} atual={etapa} />
        {aviso && <Alert tipo="aviso">{aviso}</Alert>}

        {etapa === 0 && (
          <section aria-labelledby="titulo-etapa">
            <h2 id="titulo-etapa" className={estilos.titulo}>
              Qual filme?
            </h2>
            <EscolherRegistro
              rotuloBusca="Buscar filme"
              useBusca={useBuscaFilmes}
              chave={(f) => f.id}
              rotuloItem={(f) => f.nome}
              aoEscolher={(f) => {
                setFilme(f)
                setExemplar(null)
                setAviso(null)
                setEtapa(1)
              }}
              vazio="Nenhum filme cadastrado."
            />
          </section>
        )}

        {etapa === 1 && filme && (
          <section aria-labelledby="titulo-etapa">
            <h2 id="titulo-etapa" className={estilos.titulo}>
              Qual exemplar de “{filme.nome}”?
            </h2>
            <ExemplaresLivres
              filme={filme}
              aoEscolher={(e) => {
                setExemplar(e)
                setAviso(null)
                setEtapa(cliente ? 3 : 2)
              }}
              aoTrocarFilme={() => setEtapa(0)}
            />
          </section>
        )}

        {etapa === 2 && (
          <section aria-labelledby="titulo-etapa">
            <h2 id="titulo-etapa" className={estilos.titulo}>
              Para qual cliente?
            </h2>
            <Stack gap={4}>
              <EscolherRegistro
                rotuloBusca="Buscar cliente"
                useBusca={useBuscaClientes}
                chave={(c) => c.id}
                rotuloItem={(c) => (
                  <>
                    {nomeCompleto(c)} <span className={estilos.discreto}>· {c.email}</span>
                  </>
                )}
                aoEscolher={(c) => {
                  setCliente(c)
                  setEtapa(3)
                }}
                vazio="Nenhum cliente cadastrado."
              />
              <div className={estilos.voltar}>
                <Button variante="secundario" onClick={() => setEtapa(1)}>
                  Voltar
                </Button>
                <BotaoLink to="/clientes/novo" variante="secundario">
                  Cadastrar novo cliente
                </BotaoLink>
              </div>
            </Stack>
          </section>
        )}

        {etapa === 3 && filme && exemplar && cliente && (
          <section aria-labelledby="titulo-etapa">
            <h2 id="titulo-etapa" className={estilos.titulo}>
              Confirmar o aluguel
            </h2>
            <Stack gap={5}>
              <dl className={estilos.resumo}>
                <Linha rotulo="Filme" valor={filme.nome} aoTrocar={() => setEtapa(0)} />
                <Linha rotulo="Exemplar" valor={`Nº ${exemplar.idExemplar}`} aoTrocar={() => setEtapa(1)} />
                <Linha rotulo="Cliente" valor={nomeCompleto(cliente)} aoTrocar={() => setEtapa(2)} />
              </dl>
              <Confirmar
                idExemplar={exemplar.idExemplar}
                idCliente={cliente.id}
                aoRegistrar={setFeito}
                aoExemplarIndisponivel={(mensagem) => {
                  setExemplar(null)
                  setAviso(`${mensagem} Escolha outro exemplar.`)
                  setEtapa(1)
                }}
                aoVoltar={() => setEtapa(2)}
              />
            </Stack>
          </section>
        )}
      </Stack>
    </Container>
  )
}

function Linha({ rotulo, valor, aoTrocar }: { rotulo: string; valor: string; aoTrocar?: () => void }) {
  return (
    <div className={estilos.linha}>
      <dt>{rotulo}</dt>
      <dd>{valor}</dd>
      {aoTrocar && (
        <Button variante="secundario" onClick={aoTrocar} aria-label={`Trocar ${rotulo.toLowerCase()}`}>
          Trocar
        </Button>
      )}
    </div>
  )
}

// RF09: só aparecem exemplares disponíveis; a lista é buscada de novo a cada visita.
function ExemplaresLivres({
  filme,
  aoEscolher,
  aoTrocarFilme,
}: {
  filme: Filme
  aoEscolher: (e: ExemplarDisponivel) => void
  aoTrocarFilme: () => void
}) {
  const livres = useDisponiveis(filme.id)

  if (livres.isPending) return <Spinner />
  if (livres.isError) return <Alert>{textoDoErro(livres.error)}</Alert>
  return (
    <Stack gap={4}>
      {livres.data.length === 0 ? (
        <p>Nenhum exemplar livre deste filme. Escolha outro filme.</p>
      ) : (
        <ul className={estilos.exemplares}>
          {livres.data.map((e) => (
            <li key={e.idExemplar}>
              <button type="button" className={estilos.exemplar} onClick={() => aoEscolher(e)}>
                Exemplar nº {e.idExemplar}
              </button>
            </li>
          ))}
        </ul>
      )}
      <div>
        <Button variante="secundario" onClick={aoTrocarFilme}>
          Escolher outro filme
        </Button>
      </div>
    </Stack>
  )
}

const esquema = z.object({
  dataEntrega: z
    .string()
    .min(1, 'Informe a data de entrega.')
    .refine((v) => v >= hojeISO(), 'A entrega não pode ser antes de hoje.'),
  valorAluguel: z
    .string()
    .refine((v) => !Number.isNaN(paraNumero(v)), 'Informe o valor, como 8,50.')
    .refine((v) => paraNumero(v) <= 99_999_999.99, 'Valor alto demais.'),
})
type Dados = z.infer<typeof esquema>

function Confirmar({
  idExemplar,
  idCliente,
  aoRegistrar,
  aoExemplarIndisponivel,
  aoVoltar,
}: {
  idExemplar: number
  idCliente: number
  aoRegistrar: (a: Aluguel) => void
  aoExemplarIndisponivel: (mensagem: string) => void
  aoVoltar: () => void
}) {
  const registrar = useRegistrarAluguel()
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<Dados>({
    resolver: zodResolver(esquema),
    defaultValues: { dataEntrega: somarDias(hojeISO(), DIAS_PADRAO), valorAluguel: '' },
  })

  const enviar = handleSubmit(async (dados) => {
    try {
      const aluguel = await registrar.mutateAsync({
        idExemplar,
        idCliente,
        dataEntrega: dados.dataEntrega,
        valorAluguel: paraNumero(dados.valorAluguel),
      })
      aoRegistrar(aluguel)
    } catch (erro) {
      // 422: outro atendente alugou este exemplar enquanto você preenchia (RN01).
      if (ehApiError(erro) && erro.status === 422) aoExemplarIndisponivel(textoDoErro(erro))
      else aplicarErrosNoFormulario(erro, setError, ['dataEntrega', 'valorAluguel'])
    }
  })

  return (
    <Form onSubmit={enviar}>
      {errors.root?.servidor?.message && <Alert>{errors.root.servidor.message}</Alert>}
      <FormGrid>
        <Field rotulo="Entrega prevista" erro={errors.dataEntrega?.message}>
          {(c) => <Input {...c} {...register('dataEntrega')} type="date" min={hojeISO()} />}
        </Field>
        <Field rotulo="Valor do aluguel (R$)" erro={errors.valorAluguel?.message}>
          {(c) => <Input {...c} {...register('valorAluguel')} inputMode="decimal" autoComplete="off" placeholder="0,00" />}
        </Field>
      </FormGrid>
      <FormActions>
        <Button type="submit" carregando={registrar.isPending}>
          Registrar aluguel
        </Button>
        <Button variante="secundario" onClick={aoVoltar}>
          Voltar
        </Button>
      </FormActions>
    </Form>
  )
}
