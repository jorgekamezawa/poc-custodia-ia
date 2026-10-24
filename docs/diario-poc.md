---
status: ativo
atualizado: 2026-10-24
descricao: Tempo gasto e rodadas de correção de cada entrega da POC, para mostrar a curva em J.
---
# Diário da POC

Uma linha por entrega. Tempo é o de relógio, da aprovação do plano até a entrega aprovada; rodadas de correção contam as vezes em que o resultado voltou para ajuste.

| Data | Fase | Entrega | Tempo | Rodadas de correção | Observação |
|---|---|---|---|---|---|
| 2026-10-24 | 0 | Esqueleto: backend, frontend, CI e portal | cerca de 30 min | 0 | Planejamento e definições da POC tomaram a maior parte do dia, antes da fase |
| 2026-10-24 | 1a | Padrão de documentação, templates, verificação, portal e ADRs | cerca de 3 h | 2 | Specs debatidas a fundo (pesquisa de SDD); revisor apontou 2 problemas num ADR |
| 2026-10-24 | 1b | CLAUDE.md, rules e hooks | cerca de 40 min | 0 | Hook de git só vale a partir da próxima sessão |
| 2026-10-24 | 1c | Sensores: formato, análise estática, lint, PIT e Playwright | cerca de 1 h | 2 | Conflito JUnit 5 e 6 trazido pelo plugin do PIT; Chromium sem bibliotecas no WSL |
| 2026-10-24 | 1d | Skills, subagentes e scripts de apoio (board fica para depois do PR) | cerca de 50 min | 3 | Revisor externo achou 17 pontos (4 de prática documentada); idioma dos subagentes trocado para português após pesquisa; pesquisador passou do limite de linhas na validação |
| 2026-10-24 | 2 | Discovery e backlog: visão, glossário, regras gerais e 21 issues no board | não medido | 0 | check_docs exigia o cabeçalho de exemplo do template de regras (corrigido em PR próprio); texto oficial do CONTRAN 807 ilegível sem leitor de PDF, regras ficaram fictícias; revisor-docs achou 4 termos sem explicação |
| 2026-10-24 | 3 (mandato 1) | História #8: cronograma Price (spec 0001, PRs #30, #33 e este) | não medido | 0 | Primeira história em modo autônomo; revisores acharam 3 pontos, todos corrigidos; testes parametrizados sem o cenário no relatório |
