---
status: aprovada
atualizado: 2026-10-24
descricao: Adiciona ao módulo calculator o cálculo do CET sobre o cronograma Price ou SAC, exposto na api do módulo.
---
# 01 · back · Cálculo do CET

Spec: [Custo efetivo total do contrato](../spec.md) · Depende de: nenhuma

## Abordagem técnica
- A taxa é achada por bisseção (divide o intervalo ao meio até chegar à taxa) entre 0% e 1.000% ao ano, até a diferença ficar abaixo de 10⁻¹⁰ ao ano; a fórmula tem expoente fracionário, que `BigDecimal` não calcula.
- O desconto de cada parcela usa `double`: o resultado é uma taxa arredondada a 2 casas, e o erro do `double` (cerca de 10⁻¹⁵) fica muito abaixo disso. As parcelas e o valor liberado continuam `BigDecimal` e só viram `double` dentro do cálculo da taxa.
- Arredondamento final para 2 casas em `BigDecimal`, HALF_EVEN.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/calculator/api/EffectiveCostCalculator` | novo | Interface que outros módulos usam para pedir o CET. |
| `.../calculator/api/EffectiveCostRequest` | novo | Pedido do cronograma, data de liberação e custos pagos antecipadamente. |
| `.../calculator/api/TotalEffectiveCost` | novo | CET em percentual ao ano, com 2 casas. |
| `.../calculator/domain/EffectiveCostLimits` | novo | Validação da data de liberação e dos custos (CAL-06). |
| `.../calculator/domain/TotalEffectiveCostRate` | novo | Taxa anual pela fórmula da CAL-05, a partir do valor liberado, das parcelas e da data de liberação. |
| `.../calculator/application/EffectiveCostCalculatorService` | novo | Implementa `EffectiveCostCalculator`: valida, pede o cronograma ao `ScheduleCalculator` do módulo e calcula a taxa. Bean do Spring. |

## Testes desta task
- [x] Unitário de domínio `TotalEffectiveCostRateTest`: CT-1 a CT-5, com cronogramas montados pelo `PriceSchedule` e pelo `SacSchedule`, e CET acima de 100% ao ano (custos de R$ 6.000,00: 931,54%), para o intervalo de busca crescer.
- [x] Unitário de domínio `EffectiveCostLimitsTest`: CT-6 a CT-10.
- [x] Unitário de caso de uso `EffectiveCostCalculatorServiceTest`: CT-11 e pedido válido devolvendo o CET do CT-1.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6, CT-7, CT-8, CT-9, CT-10, CT-11

## Checklist
- [x] Testes de domínio escritos antes do código.
- [x] `./gradlew build` e `./gradlew pitest` verdes, mutação acima de 80%.
