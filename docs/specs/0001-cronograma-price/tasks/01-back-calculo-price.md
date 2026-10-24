---
status: aprovada
atualizado: 2026-10-24
descricao: Cria o módulo calculator com o cálculo do cronograma Price exposto na api do módulo.
---
# 01 · back · Cálculo do cronograma Price

Spec: [Cronograma pela tabela Price](../spec.md) · Depende de: nenhuma

## Abordagem técnica
- Java puro, sem biblioteca nova: o cálculo é aritmética de `BigDecimal` e datas com `LocalDate`.
- Potência com expoente negativo em `MathContext.DECIMAL128` (34 dígitos); só a parcela, os juros e a amortização são arredondados ao centavo, HALF_EVEN.
- O módulo não tem banco nem REST: quem precisa do cronograma chama a interface da `api/`, implementada em `application/`.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/calculator/api/ScheduleCalculator` | novo | Interface que outros módulos usam para pedir um cronograma. |
| `.../calculator/api/ScheduleRequest` | novo | Valor de entrada: sistema de amortização, valor financiado, taxa mensal em percentual, prazo e primeiro vencimento. |
| `.../calculator/api/AmortizationSystem` | novo | Sistemas de amortização suportados; por ora só `PRICE`. |
| `.../calculator/api/InstallmentSchedule` | novo | Cronograma devolvido: lista ordenada de parcelas. |
| `.../calculator/api/Installment` | novo | Uma parcela: número, vencimento, valor, juros, amortização e saldo devedor depois dela. |
| `.../calculator/api/InvalidScheduleRequestException` | novo | Recusa da entrada, com a mensagem do campo fora do limite. |
| `.../calculator/domain/ScheduleLimits` | novo | Limites de valor, taxa e prazo (CAL-03) e a validação da entrada. |
| `.../calculator/domain/PriceSchedule` | novo | Parcela fixa, juros, amortização, resíduo na última parcela (CAL-01) e vencimentos (CAL-02). |
| `.../calculator/application/ScheduleCalculatorService` | novo | Implementa `ScheduleCalculator`: valida pelos limites e delega ao sistema pedido. Bean do Spring. |
| `backend/src/test/java/com/bancoexemplo/custody/architecture/ArchitectureTest` | alterado | Regra nova: fora do próprio módulo, só a `api/` é acessível. |
| `backend/src/test/java/com/bancoexemplo/custody/archfixture/ReachesIntoCalculatorDomain` | novo | Classe de teste que viola a fronteira de propósito, para o teste de arquitetura provar que a regra falha. |

## Testes desta task
- [x] Unitário de domínio `PriceScheduleTest`: CT-1 a CT-7.
- [x] Unitário de domínio `ScheduleLimitsTest`: CT-8 a CT-11.
- [x] Unitário de caso de uso `ScheduleCalculatorServiceTest`: entrada inválida chega como `InvalidScheduleRequestException`; entrada válida devolve o cronograma Price.
- [x] Arquitetura: classe de outro módulo que importe `calculator.domain` falha a regra.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6, CT-7, CT-8, CT-9, CT-10, CT-11

## Checklist
- [x] Testes de domínio escritos antes do código.
- [x] Módulo `calculator` com `api/`, `domain/` e `application/`.
- [x] Regra ArchUnit de acesso só pela `api/`.
- [x] `./gradlew build` e `./gradlew pitest` verdes, mutação acima de 80%.
