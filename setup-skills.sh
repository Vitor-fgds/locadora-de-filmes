#!/usr/bin/env bash
# Instala plugins e skills do projeto com escopo de PROJETO (.claude/settings.json),
# para que todo o grupo use o mesmo conjunto. Cada integrante roda este script uma vez.
# Windows: rode os mesmos comandos no PowerShell, um por linha.
set -euo pipefail

echo "==> Servidores de linguagem (os plugins LSP só funcionam com eles no PATH)"
npm install -g typescript typescript-language-server
if ! command -v jdtls >/dev/null 2>&1; then
  echo "AVISO: jdtls não encontrado. macOS: brew install jdtls"
  echo "       Linux/Windows: baixe o Eclipse JDT Language Server e coloque 'jdtls' no PATH."
fi

echo "==> Marketplace oficial da Anthropic (normalmente já vem registrado)"
claude plugin marketplace add anthropics/claude-plugins-official || true

echo "==> Plugins oficiais"
for p in jdtls-lsp typescript-lsp context7 security-guidance frontend-design code-simplifier; do
  claude plugin install "$p@claude-plugins-official" --scope project
done

echo "==> Trail of Bits: auditoria de padrões inseguros"
# Requer Claude Code v2.1.292+. Em versões antigas:
#   claude plugin marketplace add trailofbits/skills
#   e instale insecure-defaults pelo /plugin (aba Discover).
claude plugin install insecure-defaults --marketplace trailofbits/skills --scope project

echo "==> Vercel: boas práticas de desempenho em React (vai para .claude/skills/)"
npx -y skills add vercel-labs/agent-skills --skill vercel-react-best-practices --agent claude-code

echo
echo "Pronto. Abra 'claude' na raiz e confira com /plugin e /skills."
