---
status: aprovada
atualizado: 2026-10-24
descricao: Adiciona o SAC ao módulo calculator, pedido pela mesma interface da api que já atende o Price.
---
# 01 · back · Cálculo do cronograma SAC

Spec: [Cronograma pelo SAC](../spec.md) · Depende de: nenhuma

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/calculator/api/AmortizationSystem` | alterado | Ganha o valor `SAC`. |
| `.../calculator/domain/SacSchedule` | novo | Amortização constante, juros sobre o saldo anterior e resíduo na última parcela (CAL-04); vencimentos pela CAL-02. |
| `.../calculator/domain/DueDates` | novo | Vencimento da parcela n a partir do primeiro (CAL-02), usado por Price e SAC. |
| `.../calculator/domain/PriceSchedule` | alterado | Passa a usar `DueDates`. |
| `.../calculator/application/ScheduleCalculatorService` | alterado | Delega o pedido `SAC` ao `SacSchedule`. |

## Testes desta task
- [ ] Unitário de domínio `SacScheduleTest`: CT-1 a CT-5.
- [ ] Unitário de caso de uso `ScheduleCalculatorServiceTest`: CT-6 e pedido SAC válido devolvendo o cronograma SAC.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6

## Checklist
- [ ] Testes de domínio escritos antes do código.
- [ ] Testes do Price seguem verdes depois de extrair `DueDates`.
- [ ] `./gradlew build` e `./gradlew pitest` verdes, mutação acima de 80%.
