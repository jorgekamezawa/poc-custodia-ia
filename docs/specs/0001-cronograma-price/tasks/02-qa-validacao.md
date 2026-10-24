---
status: concluída
atualizado: 2026-10-24
descricao: Validação final da feature Cronograma pela tabela Price contra a spec.
---
# 02 · qa · Validação

Spec: [Cronograma pela tabela Price](../spec.md) · Depende de: 01

## Verificação contra a spec
| Critério / cenário | Como verificado | Resultado | Evidência |
|---|---|---|---|
| CA-1 (CT-1, CT-4) | Unitário `PriceScheduleTest` | passou | `./gradlew build`: 28 testes, 0 falhas |
| CA-2 (CT-2, CT-3) | Unitário `PriceScheduleTest` | passou | idem |
| CA-3 (CT-5) | Unitário `PriceScheduleTest` | passou | idem |
| CA-4 (CT-6, CT-7) | Unitário `PriceScheduleTest` | passou | idem |
| CA-5 (CT-8 a CT-11) | Unitário `ScheduleLimitsTest` e `ScheduleCalculatorServiceTest` | passou | idem |

## Boas práticas
- [x] Convenções do projeto seguidas (revisor-codigo no PR #33).
- [x] Sensores verdes (build, lint, testes, PIT): mutação 25 de 25 mortos (100%).

## Testes ponta a ponta criados
- Nenhum: a feature não tem tela nem endpoint; o caminho principal é provado pelos testes unitários.

## Relatório
Todos os critérios passaram. Achado: os testes parametrizados saíam no relatório como `[1] term = 0`, sem o cenário; corrigido aqui com `name = "{displayName} [{arguments}]"`, e a convenção vai para a rule do backend. Nada pendente.
