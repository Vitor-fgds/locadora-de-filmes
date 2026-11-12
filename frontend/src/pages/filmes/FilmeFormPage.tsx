import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { aplicarErrosNoFormulario } from '../../api/formErrors'
import { filmes } from '../../api/recursos/cadastros'
import type { Filme } from '../../api/tipos'
import { Alert } from '../../components/Alert'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { Field } from '../../components/Field'
import { Form, FormActions, FormGrid } from '../../components/Form'
import { FormShell } from '../../components/FormShell'
import { Input } from '../../components/Input'
import { hojeISO } from '../../lib/formato'
import { NaoEncontradaPage } from '../NaoEncontradaPage'
import estilos from './FilmeFormPage.module.css'

// Os campos do formulário são sempre texto; a conversão para número acontece ao enviar.
// Limites iguais ao FilmeRequest do backend.
const esquema = z.object({
  nome: z.string().trim().min(1, 'Informe o título.').max(50, 'Use no máximo 50 caracteres.'),
  descricao: z.string().trim().min(1, 'Informe a descrição.').max(2000, 'Use no máximo 2000 caracteres.'),
  duracaoMinutos: z
    .string()
    .regex(/^\d+$/, 'Informe a duração em minutos.')
    .refine((v) => Number(v) >= 1 && Number(v) <= 1000, 'A duração deve ficar entre 1 e 1000 minutos.'),
  dataLancamento: z
    .string()
    .min(1, 'Informe a data de lançamento.')
    .refine((v) => v <= hojeISO(), 'A data de lançamento não pode ser no futuro.'),
})
type Dados = z.infer<typeof esquema>

const CAMPOS = Object.keys(esquema.shape)
const VAZIO: Dados = { nome: '', descricao: '', duracaoMinutos: '', dataLancamento: '' }

const paraFormulario = (f: Filme): Dados => ({
  nome: f.nome,
  descricao: f.descricao,
  duracaoMinutos: String(f.duracaoMinutos),
  dataLancamento: f.dataLancamento,
})

export function FilmeFormPage() {
  const { id: idTexto } = useParams()
  const id = idTexto === undefined ? undefined : Number(idTexto)
  const navigate = useNavigate()
  const existente = filmes.useUm(id)
  const salvar = filmes.useSalvar(id)
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<Dados>({
    resolver: zodResolver(esquema),
    defaultValues: VAZIO,
    values: existente.data ? paraFormulario(existente.data) : undefined,
  })

  if (id !== undefined && !Number.isInteger(id)) return <NaoEncontradaPage />

  const enviar = handleSubmit(async (dados) => {
    try {
      const salvo = await salvar.mutateAsync({ ...dados, duracaoMinutos: Number(dados.duracaoMinutos) })
      void navigate(`/filmes/${salvo.id}`, { state: { aviso: 'Filme salvo.' } })
    } catch (erro) {
      aplicarErrosNoFormulario(erro, setError, CAMPOS)
    }
  })

  const voltar = id === undefined ? '/filmes' : `/filmes/${id}`

  return (
    <FormShell
      titulo={id === undefined ? 'Novo filme' : 'Editar filme'}
      carregando={id !== undefined && existente.isPending}
      erro={existente.error}
      voltarPara="/filmes"
    >
      <Form onSubmit={enviar}>
        {errors.root?.servidor?.message && <Alert>{errors.root.servidor.message}</Alert>}
        <FormGrid>
          <Field rotulo="Título" inteiro erro={errors.nome?.message}>
            {(c) => <Input {...c} {...register('nome')} autoComplete="off" />}
          </Field>
          <Field rotulo="Duração (minutos)" erro={errors.duracaoMinutos?.message}>
            {(c) => <Input {...c} {...register('duracaoMinutos')} type="number" inputMode="numeric" min={1} max={1000} />}
          </Field>
          <Field rotulo="Data de lançamento" erro={errors.dataLancamento?.message}>
            {(c) => <Input {...c} {...register('dataLancamento')} type="date" max={hojeISO()} />}
          </Field>
          <Field rotulo="Descrição" inteiro erro={errors.descricao?.message}>
            {(c) => <textarea {...c} {...register('descricao')} className={estilos.descricao} rows={5} />}
          </Field>
        </FormGrid>
        <FormActions>
          <Button type="submit" carregando={salvar.isPending}>
            Salvar filme
          </Button>
          <BotaoLink to={voltar} variante="secundario">
            Cancelar
          </BotaoLink>
        </FormActions>
      </Form>
    </FormShell>
  )
}
