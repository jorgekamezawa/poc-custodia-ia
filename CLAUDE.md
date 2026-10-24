# poc-custodia-ia

Sistema fictício de custódia de financiamentos (Banco Exemplo), usado numa POC de desenvolvimento orientado a IA. Backend Java/Spring Boot em `backend/`, portal Angular em `frontend/`. Contexto e decisões da POC: `docs/poc-contexto.md`.

## Regras invioláveis do fluxo
1. **Nada de commit antes da validação.** Terminou, pare e mostre: o que mudou, a evidência (comando e saída real) e o que ficou de fora. Commit só com aprovação explícita.
2. **Aprovação vale para uma ação só.** Aprovar o desenvolvimento não aprova o commit; aprovar o commit não aprova o push nem o PR. O hook `guard_git.py` pede confirmação; a primeira aprovação vale para as outras duas por 5 minutos.
3. **Um PR por task, um PR aberto por vez.** O merge é de quem mantém o projeto.
4. **Branch se atualiza por rebase**, nunca por merge da `main`.
5. **Documento só é commitado depois de aprovado.** Rejeitado durante a escrita, é apagado sem passar pelo git.
6. **Ação destrutiva** (apagar arquivo, volume, branch, dado): pergunte antes.

## Modo autônomo (mandato)
Com `.claude/mandato.json` válido (escopo aprovado pela pessoa, data `ate` não vencida), as regras 1, 2, 3 e 5 acima e as paradas das skills passam a ser cumpridas por sensores e subagentes revisores, pela skill `lote-autonomo`. A regra 4 continua. A regra 6 continua para dados e volumes locais, branch `main` e repositório. O que pode e o que segue proibido está na skill.

## Comandos
| O quê | Comando |
|---|---|
| Build, formato e testes do backend (precisa de Docker) | `cd backend && ./gradlew build` |
| Formatar o backend | `cd backend && ./gradlew spotlessApply` |
| Teste de mutação do backend | `cd backend && ./gradlew pitest` (relatório em `build/reports/pitest`) |
| Lint, formato e testes do frontend | `cd frontend && npm run lint && npm run format:check && npm test -- --watch=false` |
| Formatar o frontend | `cd frontend && npm run format` |
| Ponta a ponta (sobe backend, banco e portal sozinho) | `cd e2e && npm run format:check && npm test` |
| Verificar documentos | `python3 ../custodia-docs/scripts/check_docs.py --root .` |

## Onde mora cada coisa
| O quê | Onde |
|---|---|
| Visão, glossário, regras de negócio vigentes, áreas vizinhas | `../custodia-docs/` (doc da área) |
| Padrão de documentação e templates | `../custodia-docs/padroes/` e `../custodia-docs/templates/` |
| Por que escolhemos | `docs/adr/` (área: `../custodia-docs/adr/`) |
| O que uma feature muda, e como implementar | `docs/specs/NNNN-titulo/spec.md` e `tasks/` |
| Experimento, debate de entrega, operação | `docs/spikes/`, `docs/decisoes/`, `docs/runbooks/` |
| Como codificar sempre | `.claude/rules/` (carregadas pela pasta do arquivo) |
| Backlog | GitHub Projects e Issues |

Para achar contexto: leia o campo `descricao` do cabeçalho antes de abrir um documento inteiro.

## Padrões
- Código, identificadores e comentários em inglês; documentos, specs, commits e PRs em português.
- Toda feature passa pelo fluxo da spec descrito no padrão de documentação: spec aprovada, tasks por camada aprovadas, uma task por PR, task de QA no fim.
- Regra de negócio nova ou alterada atualiza a página de regras do módulo no `custodia-docs`, no mesmo ciclo da feature.

## Pronto significa
- Build passa, suíte verde, sem aviso novo, evidência mostrada.
- Documentos passam no script e no subagente `revisor-docs`.
- Subiu contêiner ou aplicação para testar, derrube ao terminar.
