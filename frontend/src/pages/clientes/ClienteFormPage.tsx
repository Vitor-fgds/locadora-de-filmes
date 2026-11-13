import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { aplicarErrosNoFormulario } from '../../api/formErrors'
import { clientes } from '../../api/recursos/cadastros'
import type { Cliente } from '../../api/tipos'
import { Alert } from '../../components/Alert'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { Field } from '../../components/Field'
import { Form, FormActions, FormGrid } from '../../components/Form'
import { FormShell } from '../../components/FormShell'
import { Input } from '../../components/Input'
import { NaoEncontradaPage } from '../NaoEncontradaPage'

// Limites iguais ao ClienteRequest do backend (VARCHAR(50), telefone com DDD).
const texto50 = (mensagem: string) =>
  z.string().trim().min(1, mensagem).max(50, 'Use no máximo 50 caracteres.')

const esquema = z.object({
  primeiroNome: texto50('Informe o nome.'),
  sobrenome: texto50('Informe o sobrenome.'),
  rua: texto50('Informe a rua.'),
  bairro: texto50('Informe o bairro.'),
  cidade: texto50('Informe a cidade.'),
  email: z.email('Informe um email válido.').max(50, 'Use no máximo 50 caracteres.'),
  telefone: z
    .string()
    .transform((v) => v.replace(/\D/g, ''))
    .pipe(z.string().regex(/^\d{10,11}$/, 'Informe o DDD e o número (10 ou 11 dígitos).')),
})
type Dados = z.infer<typeof esquema>

const CAMPOS = Object.keys(esquema.shape)
const VAZIO: Dados = { primeiroNome: '', sobrenome: '', rua: '', bairro: '', cidade: '', email: '', telefone: '' }

const paraFormulario = ({ id: _id, ...dados }: Cliente): Dados => dados

export function ClienteFormPage() {
  const { id: idTexto } = useParams()
  const id = idTexto === undefined ? undefined : Number(idTexto)
  const navigate = useNavigate()
  const existente = clientes.useUm(id)
  const salvar = clientes.useSalvar(id)
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
      await salvar.mutateAsync(dados)
      void navigate('/clientes', { state: { aviso: 'Cliente salvo.' } })
    } catch (erro) {
      aplicarErrosNoFormulario(erro, setError, CAMPOS)
    }
  })

  return (
    <FormShell
      titulo={id === undefined ? 'Novo cliente' : 'Editar cliente'}
      carregando={id !== undefined && existente.isPending}
      erro={existente.error}
      voltarPara="/clientes"
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
          <Field rotulo="Email" erro={errors.email?.message}>
            {(c) => <Input {...c} {...register('email')} type="email" inputMode="email" autoComplete="off" />}
          </Field>
          <Field rotulo="Telefone" dica="Com DDD" erro={errors.telefone?.message}>
            {(c) => <Input {...c} {...register('telefone')} type="tel" inputMode="tel" autoComplete="off" />}
          </Field>
          <Field rotulo="Rua" inteiro erro={errors.rua?.message}>
            {(c) => <Input {...c} {...register('rua')} autoComplete="off" />}
          </Field>
          <Field rotulo="Bairro" erro={errors.bairro?.message}>
            {(c) => <Input {...c} {...register('bairro')} autoComplete="off" />}
          </Field>
          <Field rotulo="Cidade" erro={errors.cidade?.message}>
            {(c) => <Input {...c} {...register('cidade')} autoComplete="off" />}
          </Field>
        </FormGrid>
        <FormActions>
          <Button type="submit" carregando={salvar.isPending}>
            Salvar cliente
          </Button>
          <BotaoLink to="/clientes" variante="secundario">
            Cancelar
          </BotaoLink>
        </FormActions>
      </Form>
    </FormShell>
  )
}
