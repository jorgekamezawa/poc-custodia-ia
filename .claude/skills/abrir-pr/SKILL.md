---
name: abrir-pr
description: Abre o PR de uma entrega no padrão do projeto: confere as pré-condições por script, roda os sensores, garante a revisão do subagente e escreve o corpo em português com spec, task, cenários e evidência. Use quando for abrir PR, publicar a branch ou mandar uma entrega para revisão.
---

# Abrir PR

Commit, push e PR só com aprovação explícita da pessoa. Sem ela, pare e peça. O hook `guard_git` confirma de novo na hora; esta skill garante que o que chega lá está pronto.

## Passos
1. **Pré-condições:** `python3 ${CLAUDE_SKILL_DIR}/scripts/preflight.py`. Falhou? Mostre o que falta e pare; não contorne.
   - Branch atrás da `main`: `git fetch origin && git rebase origin/main`, nunca merge da `main`.
2. **Sensores** de cada parte que a branch toca, com os comandos do `CLAUDE.md` (backend, frontend, documentos; ponta a ponta na task de QA). Guarde a última linha de cada saída para o corpo.
3. **Revisão** pelo subagente, chamado pelo nome (nunca um fork) e sem o seu raciocínio: código vai para o `revisor-codigo`, com o caminho da task e a branch; documento vai para o `revisor-docs`, com o caminho. Corrija o que proceder; o que recusar, recuse com motivo.
4. **Commit** em português, verbo na terceira pessoa do presente ("Adiciona o registro de gravame").
5. **Push:** `git push -u origin <branch>`; depois de um rebase, `--force-with-lease`, nunca `--force`.
6. **PR:** `gh pr create --base main`, com o corpo abaixo. Mostre o link. O board anda pelas automações; o merge é de quem mantém o projeto.

## Corpo
```markdown
Closes #<issue da task>        (PR de docs da spec: Refs #<história>)
Spec: <link> · Task: <link>

## O que muda
- <uma linha por mudança>

## Cenários cobertos
<0007/CT-1, 0007/CT-2: verdes>

## Evidência
- `<comando>`: <última linha da saída>

## Revisão
<subagente>: <linha "Resultado:">; recusados: <apontamento e motivo>

## Fora desta entrega (só quando houver)
- <o que ficou de fora e por quê>
```
