import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Navigate, useLocation, useSearchParams } from 'react-router'
import { z } from 'zod'
import { aplicarErrosNoFormulario } from '../api/formErrors'
import { ehApiError } from '../api/problem'
import { useLogin, useUsuario } from '../auth/useUsuario'
import { Alert } from '../components/Alert'
import { Button } from '../components/Button'
import { Field } from '../components/Field'
import { Form, FormActions } from '../components/Form'
import { Input } from '../components/Input'
import { Perfuracao } from '../components/Perfuracao'
import estilos from './LoginPage.module.css'

// Mesmos limites do LoginRequest do backend (email ≤ 50; senha ≤ 72 bytes do BCrypt).
const esquema = z.object({
  email: z.email('Informe um email válido.').max(50, 'Use no máximo 50 caracteres.'),
  senha: z.string().min(1, 'Informe a senha.').max(72, 'Use no máximo 72 caracteres.'),
})
type Dados = z.infer<typeof esquema>

const CAMPOS = ['email', 'senha'] as const

// Só aceita caminho interno ("/clientes"), nunca "//site.com" ou "https://…".
function destinoSeguro(estado: unknown): string {
  const origem = (estado as { from?: unknown } | null)?.from
  return typeof origem === 'string' && origem.startsWith('/') && !origem.startsWith('//') ? origem : '/perfil'
}

export function LoginPage() {
  const { data: usuario } = useUsuario()
  const login = useLogin()
  const local = useLocation()
  const [params] = useSearchParams()
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<Dados>({ resolver: zodResolver(esquema) })

  if (usuario) return <Navigate to={destinoSeguro(local.state)} replace />

  const entrar = handleSubmit(async (dados) => {
    try {
      await login.mutateAsync(dados) // ao gravar o usuário no cache, o redirect acima acontece
    } catch (erro) {
      if (ehApiError(erro) && erro.status === 401) {
        setError('root.servidor', { message: 'Email ou senha incorretos.' })
      } else {
        aplicarErrosNoFormulario(erro, setError, CAMPOS)
      }
    }
  })

  return (
    <main className={estilos.pagina}>
      <div className={estilos.painel}>
        <Perfuracao lado="esquerda" />
        <div className={estilos.conteudo}>
          <h1 className={estilos.marca}>Locadora</h1>
          <p className={estilos.subtitulo}>Entre com seu email de funcionário.</p>

          <Form onSubmit={entrar}>
            {params.get('expirou') && !errors.root?.servidor && (
              <Alert tipo="aviso">Sua sessão terminou. Entre de novo.</Alert>
            )}
            {errors.root?.servidor?.message && <Alert>{errors.root.servidor.message}</Alert>}

            <Field rotulo="Email" erro={errors.email?.message}>
              {(controle) => (
                <Input
                  {...controle}
                  {...register('email')}
                  type="email"
                  inputMode="email"
                  autoComplete="username"
                  autoCapitalize="none"
                  spellCheck={false}
                />
              )}
            </Field>
            <Field rotulo="Senha" erro={errors.senha?.message}>
              {(controle) => <Input {...controle} {...register('senha')} type="password" autoComplete="current-password" />}
            </Field>

            <FormActions>
              <Button type="submit" carregando={login.isPending}>
                Entrar
              </Button>
            </FormActions>
          </Form>
        </div>
      </div>
    </main>
  )
}
