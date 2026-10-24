---
status: aceito
atualizado: 2026-10-24
descricao: Formato, análise estática, lint, teste de mutação e ponta a ponta são verificados por ferramenta, no build e no CI.
---
# ADR-0012: Regra de código verificada por ferramenta

**Decisão:** toda regra de código que uma ferramenta consegue checar é checada por ela, no build e no CI, antes da revisão humana, porque o agente se corrige sozinho quando o erro aparece na hora, e a revisão humana fica para o que nenhuma ferramenta pega.

## Contexto
O agente escreve a maior parte do código. Regra escrita só em texto (rules, `CLAUDE.md`) é contexto, não garantia; a revisão humana é o gargalo e não deve gastar tempo com formato ou com teste que passa sem testar nada.

## Alternativas descartadas
- **Só regras em texto e revisão humana:** o agente esquece ou interpreta diferente, e o revisor vira corretor de formato.
- **Verificar só no CI:** o agente descobre o erro minutos depois, num PR já aberto, em vez de no próprio ciclo.

## Consequências
- **Ganhos:** formato uniforme sem discussão; dinheiro comparado ou construído errado quebra a compilação; teste fraco é pego pelo teste de mutação (altera o código de propósito e confere se algum teste falha); o portal é testado pelo teste ponta a ponta (sobe o portal e age como o usuário).
- **Custos:** build mais lento, mais ainda com teste de mutação e ponta a ponta.
- **Passa a ser obrigatório:**
  - no Java, formatador e análise estática (procura erros sem executar o código);
  - no Angular, lint e formatador;
  - no domínio, teste de mutação;
  - no portal, teste ponta a ponta;
  - commit com código fora do formato é barrado por hook;
  - regra nova de código ganha ferramenta quando possível, e só fica em texto o que nenhuma ferramenta checa.
