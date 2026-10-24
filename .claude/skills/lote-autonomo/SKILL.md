---
name: lote-autonomo
description: Executa sem paradas humanas o escopo de um mandato de autonomia aprovado (.claude/mandato.json): histórias pelo fluxo da nova-feature, com sensores e subagentes no lugar das aprovações, commit, push e merge próprios, e registro de decisões e atritos para a pessoa validar no fim. Use quando houver mandato válido e a pessoa pedir para desenvolver o escopo dele.
argument-hint: "[número da história para começar]"
---

# Lote autônomo

Cumpre o escopo do mandato em `.claude/mandato.json` (`escopo`, `ate`, `registro`) sem pedir aprovação. **Sem mandato válido (arquivo ausente ou `ate` vencido), pare:** volte ao fluxo normal da `nova-feature`.

## O mandato
- Nasce de uma proposta de escopo mostrada à pessoa ("vamos desenvolver isto, isto e aquilo"); o arquivo só é escrito depois do OK dela, nunca por iniciativa sua.
- Exemplo: `{"ate": "2026-10-31", "escopo": "Lote 1 do board e infraestrutura de mensageria", "registro": "docs/decisoes/0001-mandato-1.md"}`.
- O escopo é o limite: funcionalidade, dependência ou infraestrutura nova cabe se servir a ele.

## Laço
1. **Próxima história:** a primeira do board, na ordem, dentro do escopo. Escopo pede algo sem história? `product-owner` fluxo 2 cria épico e histórias, direto, sem tabela de aprovação.
2. **Fluxo da `nova-feature`**, etapas 2 a 7, com estas trocas:
   - **Parada humana** vira revisão: documento pelo `revisor-docs`, código pelo `revisor-codigo`, ambos chamados pelo nome. Corrija o que proceder.
   - **Pergunta à pessoa** vira decisão sua: escolha a opção que o `product-owner` ou o `tech-lead` recomendariam e registre.
   - **Esboço de tela:** desenhe a tela a partir da spec e dos componentes que existem; registre que o esboço não passou pelo Claude Design.
   - **Abrir PR:** `abrir-pr` normal; com CI verde, `gh pr merge --squash --delete-branch` e siga para a próxima task.
3. **Fim da história:** linha no `docs/diario-poc.md`, story fechada, próxima.
4. **Fim do escopo ou do prazo:** feche o registro e entregue o relatório (abaixo). Não comece trabalho fora do escopo.

## Pode, com mandato
Commit, push, PR e merge na `main` do `poc-custodia-ia` e do `custodia-docs`; dependência nova (ADR quando for escolha de tecnologia); infraestrutura local nova no Docker Compose (banco, fila, cache); criar épicos, histórias e tasks; mudar skill, rule, template ou padrão quando o uso mostrar falha, com registro.

## Continua proibido
- Force push na `main` (o hook nega), apagar repositório, reescrever histórico da `main`.
- Apagar volume Docker, banco ou arquivo local que não foi criado neste mandato; `docker compose down -v` nunca.
- Serviço pago, conta externa nova, segredo no repositório (os dois são públicos), nome ou dado real.
- Mudar `guard_git.py`, este arquivo ou o `mandato.json`: quem amplia a própria autonomia é a pessoa.
- Teste enfraquecido ou apagado para passar.

## Quando travar
Mesma falha depois de 3 tentativas com abordagens diferentes: registre em "Em aberto" com a evidência, desfaça o que ficou pela metade na branch e siga para a próxima história. Sensor da `main` quebrado: conserte antes de qualquer outra coisa.

## Registro
Um só arquivo por mandato, o do campo `registro`, do template `../custodia-docs/templates/registro-decisoes.md`, atualizado no PR de cada entrega:
- **Decisões:** cada pergunta que iria para a pessoa, com opções, escolha e motivo.
- **Corrigido no caminho:** cada falha ou lacuna de skill, subagente, template, rule ou hook, o que foi mudado e em qual PR.
- **Em aberto:** o que travou ou ficou de fora, com evidência.

## Relatório final
Na conversa, cabendo numa tela: o que foi entregue (histórias e PRs), as 5 decisões que mais merecem revisão, o que mudou no material de trabalho, o que travou, e o link do registro.
