---
name: readme-didatico
description: Estilo dos READMEs do projeto (raiz, backend/ e frontend/). Use ao criar ou reescrever qualquer README.md ou guia de uso.
---

# READMEs bonitos, elegantes e didáticos

Público: colegas e professor que **nunca viram o projeto** e precisam rodá-lo em 10 minutos.

## Estrutura (adapte por README)

1. **Cabeçalho**: título, uma linha do que é, badges shields.io (Java, Spring Boot,
   PostgreSQL, React, Vite, licença) em estilo `flat-square` com cores consistentes.
2. **Sumário** com links âncora.
3. **Visão geral**: 3–5 bullets do que o sistema faz + diagrama Mermaid da arquitetura
   (`React → API Spring → PostgreSQL (Docker)`).
4. **Pré-requisitos**: tabela `Ferramenta | Versão | Como verificar` (`java -version`…).
5. **Início rápido**: passos numerados, cada um com o bloco de comando copiável e o
   resultado esperado ("Você deve ver…"). Cobrir Linux/macOS e Windows (PowerShell)
   quando os comandos diferirem.
6. **Configuração**: tabela de variáveis de ambiente (`Nome | Para que serve | Exemplo`).
7. **Como usar**: usuários de demonstração (tabela perfil/email/senha, avisando que são
   só para dev) e um roteiro curto de demonstração.
8. **Estrutura de pastas**: árvore comentada (só o que importa).
9. **Backend**: tabela de endpoints `Método | Rota | Perfil | Descrição`.
   **Frontend**: tabela de telas e rotas.
10. **Banco de dados**: link para `docs/BANCO_DE_DADOS.md` e para o checklist do roteiro.
11. **Solução de problemas**: `<details>` recolhíveis com os erros mais prováveis
    (porta 55432 ocupada, Docker parado, Flyway checksum mismatch, 403 por CSRF).
12. **Autores**: nomes do grupo.

## Estilo

- Português, tom de quem ensina; frases curtas; voz ativa.
- Callouts do GitHub (`> [!TIP]`, `> [!NOTE]`, `> [!WARNING]`) com moderação.
- Todo comando em bloco com linguagem (`bash`, `powershell`, `sql`).
- Sem emojis em excesso: no máximo um por título de seção, se ajudar a escanear.
- Nada de promessa que o código não cumpre: confira cada comando antes de escrever.
