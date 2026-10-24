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
| 2026-10-24 | 3 (mandato 1) | História #9: cronograma SAC (spec 0002, PRs #36, #39 e este) | não medido | 0 | Revisor-docs contradisse a orientação que deu na spec 0001 (apelido de termo); revisor-codigo sem violações |
| 2026-10-24 | 3 (mandato 1) | História #10: CET pela Resolução CMN 4.881/2020 (spec 0003, PRs #41, #44 e este) | não medido | 0 | Fonte oficial lida do PDF do BCB; 3 mutantes equivalentes documentados; revisor-codigo não conhece o registro do mandato |
| 2026-10-24 | 3 (mandato 1) | História #47: entrar no portal (spec 0004, ADRs 0013 e 0014, PRs #48, #53 a #55 e este) | não medido | 1 | Primeira fatia com tela; o ponta a ponta no CI achou 2 defeitos de CSRF que os testes de API e componente não pegavam |
| 2026-10-24 | 3 (mandato 1) | História #12: cadastro de contrato (spec 0005, ADR-0015, PRs #58, #62, #63 e este) | não medido | 0 | Primeiro módulo com banco; ponta a ponta verde de primeira; build acusou orçamento do pacote, resolvido com carregamento sob demanda |
| 2026-10-24 | 3 (mandato 1) | História #13: consulta de contrato (spec 0006, PRs #66, #70 a #73 e este) | não medido | 2 | Incidente: merge com teste vermelho (PR #71), consertado no #72 e evitado daqui em diante pelo script de merge |
| 2026-10-24 | 3 (mandato 1) | História #15: pagamento em dia com Kafka, tabela de saída e consumidor idempotente (spec 0007, ADRs 0016 e 0017, PRs #75, #81 a #84 e este) | não medido | 0 | Primeira mensageria entre módulos; o script de merge segurou um CI que falhou por rede |
