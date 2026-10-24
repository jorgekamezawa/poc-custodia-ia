---
name: nova-feature
description: Conduz uma história do board até o PR pelo fluxo de feature da POC (refinamento, esboço, spec, tasks, implementação, QA), com aprovação humana em cada etapa. Use só quando a pessoa pedir com /nova-feature.
disable-model-invocation: true
argument-hint: <número da issue da história>
---

# Nova feature

Leva a história `$ARGUMENTS` do board ao PR, pelo fluxo da seção 6 de `docs/poc-contexto.md`. **Cada etapa termina numa parada:** mostre o resultado, as decisões que você tomou sem a pessoa ter definido, e espere aprovação explícita. Aprovar uma etapa não aprova a seguinte, nem commit.

## Onde estamos
Sem número da história em `$ARGUMENTS`, pergunte. Ao começar ou retomar, descubra a etapa antes de agir: `gh issue view $ARGUMENTS` (história e sub-issues), spec em `docs/specs/` com o link da história e o status dela, status das tasks e PR aberto (`gh pr list`). Diga em uma linha onde está e siga dali.

## Etapas
1. **História:** issue `type: story` no board. Não existe? `product-owner`, fluxo 2.
2. **Refinamento:** `product-owner`, fluxo 3. Parada.
3. **Esboço de tela** (só se a história tiver tela): prepare o briefing para o Claude Design (telas, campos, estados vazios e de erro, telas parecidas que já existem) e peça o link do esboço aprovado. O esboço é feito pela pessoa.
4. **Spec:** `product-owner`, fluxo 4 (negócio). Parada. Depois `tech-lead`, fluxo 3 (técnica e cenários). Parada.
5. **Tasks:** `tech-lead`, fluxos 0 e 4, até as issues das tasks criadas. Parada antes do PR de docs, aberto com `abrir-pr`.
6. **Implementação**, uma task por vez, na ordem das dependências, cada uma depois do merge da anterior:
   1. Branch `<issue-da-task>-<titulo-curto>` a partir da `main` atualizada.
   2. Testes e código pela task, pelas rules da pasta e pela `padrao-testes`; marque o checklist da task.
   3. Sensores verdes com os comandos do `CLAUDE.md`; corrija até passar.
   4. Chame o subagente `revisor-codigo` pelo nome (nunca um fork), só com o caminho da task e a branch; corrija o que proceder.
   5. Parada com o que mudou, a evidência e o que ficou de fora. Com o OK, `abrir-pr`.
7. **QA:** a task `qa` (sobe o portal, Playwright, relatório). Derrube tudo ao terminar. No mesmo PR: task `concluída` e spec `entregue`. Depois, `product-owner` fluxo 5 (regras do módulo, PR no `custodia-docs`) e uma linha em `docs/diario-poc.md`.

## Regras
- Nenhuma parada é pulada, nem em modo automático.
- Pergunta que surge na implementação e muda a spec: pare e pergunte. Spec aprovada que muda volta à etapa 4, com linha no histórico.
- Um PR aberto por vez em cada repositório.
