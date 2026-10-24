---
status: aceito
atualizado: 2026-10-24
descricao: O portal do operador é feito em Angular, na versão mais recente com suporte, com as ferramentas padrão do Angular CLI.
---
# ADR-0006: Angular no frontend

**Decisão:** o portal do operador é feito em Angular, na versão mais recente com suporte, usando as ferramentas padrão do Angular CLI (incluindo o Vitest como executor de testes), porque é o framework do time de referência.

## Contexto
A demonstração inclui telas e testes de front. O time de referência desenvolve em Angular; a ferramenta de esboço de tela (Claude Design) funciona melhor com React, mas o esboço é só referência visual e o agente o traduz para Angular.

## Alternativas descartadas
- **React:** integração mais direta com a sincronização de design system do Claude Design. Descartado porque a demonstração perderia credibilidade para um time Angular.
- **Vue:** curva de entrada menor. Descartado pelo mesmo motivo.

## Consequências
- **Ganhos:** telas e testes reaproveitáveis pelo time; padrões do CLI dispensam configurar build e testes à mão.
- **Custos:** o esboço do Claude Design sai em HTML e precisa ser traduzido para Angular.
- **Passa a ser obrigatório:** seguir as escolhas padrão do Angular CLI, salvo decisão registrada.
