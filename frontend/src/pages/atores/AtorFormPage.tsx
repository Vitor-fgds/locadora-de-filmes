import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { aplicarErrosNoFormulario } from '../../api/formErrors'
import { atores } from '../../api/recursos/cadastros'
import type { Ator } from '../../api/tipos'
import { Alert } from '../../components/Alert'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { Field } from '../../components/Field'
import { Form, FormActions, FormGrid } from '../../components/Form'
import { FormShell } from '../../components/FormShell'
import { Input } from '../../components/Input'
import { NaoEncontradaPage } from '../NaoEncontradaPage'

const texto50 = (mensagem: string) => z.string().trim().min(1, mensagem).max(50, 'Use no máximo 50 caracteres.')

const esquema = z.object({
  primeiroNome: texto50('Informe o nome.'),
  sobrenome: texto50('Informe o sobrenome.'),
})
type Dados = z.infer<typeof esquema>

const CAMPOS = Object.keys(esquema.shape)
const paraFormulario = ({ id: _id, ...dados }: Ator): Dados => dados

export function AtorFormPage() {
  const { id: idTexto } = useParams()
  const id = idTexto === undefined ? undefined : Number(idTexto)
  const navigate = useNavigate()
  const existente = atores.useUm(id)
  const salvar = atores.useSalvar(id)
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<Dados>({
    resolver: zodResolver(esquema),
    defaultValues: { primeiroNome: '', sobrenome: '' },
    values: existente.data ? paraFormulario(existente.data) : undefined,
  })

  if (id !== undefined && !Number.isInteger(id)) return <NaoEncontradaPage />

  const enviar = handleSubmit(async (dados) => {
    try {
      await salvar.mutateAsync(dados)
      void navigate('/atores', { state: { aviso: 'Ator salvo.' } })
    } catch (erro) {
      aplicarErrosNoFormulario(erro, setError, CAMPOS)
    }
  })

  return (
    <FormShell
      titulo={id === undefined ? 'Novo ator' : 'Editar ator'}
      carregando={id !== undefined && existente.isPending}
      erro={existente.error}
      voltarPara="/atores"
    >
      <Form onSubmit={enviar}>
        {errors.root?.servidor?.message && <Alert>{errors.root.servidor.message}</Alert>}
        <FormGrid>
          <Field rotulo="Nome" erro={errors.primeiroNome?.message}>
            {(c) => <Input {...c} {...register('primeiroNome')} autoComplete="off" />}
          </Field>
          <Field rotulo="Sobrenome" erro={errors.sobrenome?.message}>
            {(c) => <Input {...c} {...register('sobrenome')} autoComplete="off" />}
          </Field>
        </FormGrid>
        <FormActions>
          <Button type="submit" carregando={salvar.isPending}>
            Salvar ator
          </Button>
          <BotaoLink to="/atores" variante="secundario">
            Cancelar
          </BotaoLink>
        </FormActions>
      </Form>
    </FormShell>
  )
}
