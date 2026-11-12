import { zodResolver } from '@hookform/resolvers/zod'
import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { aplicarErrosNoFormulario } from '../../api/formErrors'
import { funcionarios } from '../../api/recursos/cadastros'
import type { Funcionario } from '../../api/tipos'
import { rotuloPerfil } from '../../auth/tipos'
import { useUsuario } from '../../auth/useUsuario'
import { Alert } from '../../components/Alert'
import { BotaoLink } from '../../components/BotaoLink'
import { Button } from '../../components/Button'
import { Field } from '../../components/Field'
import { Form, FormActions, FormGrid } from '../../components/Form'
import { FormShell } from '../../components/FormShell'
import { Input, Select } from '../../components/Input'
import { NaoEncontradaPage } from '../NaoEncontradaPage'

const texto50 = (mensagem: string) => z.string().trim().min(1, mensagem).max(50, 'Use no máximo 50 caracteres.')

const base = z.object({
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
  perfil: z.enum(['ADMIN', 'ATENDENTE']),
  // BCrypt só lê os 72 primeiros bytes (acentos contam 2): o backend recusa acima disso.
  senha: z
    .string()
    .refine((v) => new TextEncoder().encode(v).length <= 72, 'A senha pode ter no máximo 72 bytes (acentos contam 2).'),
})
type Dados = z.infer<typeof base>

const MENSAGEM_SENHA = 'A senha precisa ter pelo menos 8 caracteres.'
const esquemaCriar = base.refine((d) => d.senha.length >= 8, { path: ['senha'], message: MENSAGEM_SENHA })
// Ao editar, senha vazia = manter a atual.
const esquemaEditar = base.refine((d) => d.senha === '' || d.senha.length >= 8, {
  path: ['senha'],
  message: MENSAGEM_SENHA,
})

const CAMPOS = Object.keys(base.shape)
const VAZIO: Dados = {
  primeiroNome: '',
  sobrenome: '',
  rua: '',
  bairro: '',
  cidade: '',
  email: '',
  telefone: '',
  perfil: 'ATENDENTE',
  senha: '',
}

const paraFormulario = ({ id: _id, ...dados }: Funcionario): Dados => ({ ...dados, senha: '' })

export function FuncionarioFormPage() {
  const { id: idTexto } = useParams()
  const id = idTexto === undefined ? undefined : Number(idTexto)
  const editando = id !== undefined
  const navigate = useNavigate()
  const { data: eu } = useUsuario()
  const existente = funcionarios.useUm(id)
  const salvar = funcionarios.useSalvar(id)
  const esquema = useMemo(() => (editando ? esquemaEditar : esquemaCriar), [editando])
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

  if (editando && !Number.isInteger(id)) return <NaoEncontradaPage />

  // O ADMIN não pode rebaixar a si mesmo (o backend recusa com 422): o perfil vira só leitura.
  const propriaConta = editando && eu?.id === id

  const enviar = handleSubmit(async ({ senha, ...dados }) => {
    try {
      await salvar.mutateAsync(senha ? { ...dados, senha } : dados)
      void navigate('/funcionarios', { state: { aviso: 'Funcionário salvo.' } })
    } catch (erro) {
      aplicarErrosNoFormulario(erro, setError, CAMPOS)
    }
  })

  return (
    <FormShell
      titulo={editando ? 'Editar funcionário' : 'Novo funcionário'}
      carregando={editando && existente.isPending}
      erro={existente.error}
      voltarPara="/funcionarios"
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
          <Field rotulo="Email (usado para entrar)" erro={errors.email?.message}>
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
          {propriaConta ? (
            <Field rotulo="Perfil" dica="Você não pode alterar o seu próprio perfil.">
              {(c) => <Input {...c} value={rotuloPerfil[existente.data?.perfil ?? 'ADMIN']} disabled readOnly />}
            </Field>
          ) : (
            <Field rotulo="Perfil" erro={errors.perfil?.message}>
              {(c) => (
                <Select {...c} {...register('perfil')}>
                  <option value="ATENDENTE">{rotuloPerfil.ATENDENTE}</option>
                  <option value="ADMIN">{rotuloPerfil.ADMIN}</option>
                </Select>
              )}
            </Field>
          )}
          <Field
            rotulo={editando ? 'Nova senha' : 'Senha'}
            dica={editando ? 'Deixe em branco para manter a atual.' : 'Mínimo de 8 caracteres.'}
            erro={errors.senha?.message}
          >
            {(c) => <Input {...c} {...register('senha')} type="password" autoComplete="new-password" />}
          </Field>
        </FormGrid>
        <FormActions>
          <Button type="submit" carregando={salvar.isPending}>
            Salvar funcionário
          </Button>
          <BotaoLink to="/funcionarios" variante="secundario">
            Cancelar
          </BotaoLink>
        </FormActions>
      </Form>
    </FormShell>
  )
}
