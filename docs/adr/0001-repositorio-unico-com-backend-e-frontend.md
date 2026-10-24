---
status: aceito
atualizado: 2026-10-24
descricao: Backend e frontend ficam no mesmo repositório, com uma pasta para cada um.
---
# ADR-0001: Backend e frontend no mesmo repositório

**Decisão:** backend e frontend ficam no repositório `poc-custodia-ia`, em `backend/` e `frontend/`, porque uma feature mexe nos dois e assim spec, tasks, código e testes andam juntos.

## Contexto
Cada feature tem uma spec e tasks separadas por camada (back, front, infra), com um PR por task. O agente precisa ver a spec e o código das duas camadas sem trocar de repositório.

## Alternativas descartadas
- **Um repositório por aplicação:** backend e frontend com histórico e CI próprios. Descartado porque spec e tasks ficariam num terceiro lugar, ou duplicadas.

## Consequências
- **Ganhos:** um lugar só para spec, tasks e código; o agente navega tudo com um clone.
- **Custos:** o CI roda jobs das duas stacks em todo PR.
- **Passa a ser obrigatório:** cada stack com seu próprio build dentro da sua pasta, sem ferramenta de monorepo.
