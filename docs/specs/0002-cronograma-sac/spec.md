---
status: aprovada
atualizado: 2026-10-24
descricao: Cálculo do cronograma de parcelas pelo SAC (amortização constante, parcela decrescente), no mesmo cálculo que já atende a tabela Price.
---
# Cronograma pelo SAC

História: [#9 Calcular cronograma pelo SAC](https://github.com/jorgekamezawa/poc-custodia-ia/issues/9)

## Problema
Parte dos contratos de veículo é pelo SAC (sistema de amortização constante: a amortização é igual em todas as parcelas e a parcela diminui com o tempo). Os módulos que já pedem o cronograma Price precisam pedir o SAC pela mesma interface.

## Fora de escopo
- Carência e parcela balão: épico próprio.
- Mudança nos limites ou nos vencimentos: valem a [CAL-02 e a CAL-03](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/calculadoras.md) como estão.

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| adicionada | CAL-04 | No SAC, a amortização de cada parcela é o valor financiado ÷ prazo, arredondada ao centavo (GER-01). Os juros remuneratórios são o saldo devedor anterior × taxa, arredondados; a parcela é amortização mais juros remuneratórios. A última parcela amortiza todo o saldo restante, absorvendo a diferença de arredondamento. |

## Critérios de aceite
- **CA-1.** Dado valor financiado de R$ 10.000,00, taxa de 2% ao mês e prazo de 12 meses pelo SAC, quando o cronograma é calculado, então as parcelas 1 a 11 amortizam R$ 833,33, a parcela 12 amortiza R$ 833,37 e o saldo devedor final é zero.
- **CA-2.** Dado o cronograma do CA-1, quando se olha cada parcela, então a parcela é juros remuneratórios mais amortização e diminui de uma parcela para a seguinte.
- **CA-3.** Dado o SAC, quando o pedido tem vencimento ou limite que a [CAL-02 ou a CAL-03](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/calculadoras.md) tratam, então vale o mesmo comportamento do Price.

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-1 | unitário | R$ 10.000,00; 2% a.m.; 12 meses; SAC | Amortização de R$ 833,33 nas parcelas 1 a 11 e de R$ 833,37 na 12; saldo final R$ 0,00 |
| CT-2 | CA-1, CA-2 | unitário | Mesma entrada do CT-1 | Parcela 1 de R$ 1.033,33 (juros remuneratórios R$ 200,00); parcela 2 de R$ 1.016,66 (juros remuneratórios R$ 183,33); parcela 12 de R$ 850,04 (juros remuneratórios R$ 16,67) |
| CT-3 | CA-2 | unitário | Mesma entrada do CT-1 | Em toda parcela, juros remuneratórios + amortização = valor; cada parcela menor que a anterior; soma das amortizações = R$ 10.000,00 |
| CT-4 | CA-1 | unitário | R$ 10.000,00; 0% a.m.; 3 meses; SAC | Parcelas de R$ 3.333,33, R$ 3.333,33 e R$ 3.333,34, todas com juros remuneratórios de R$ 0,00 |
| CT-5 | CA-3 | unitário | Primeiro vencimento 31/01/2027; 3 meses; SAC | Vencimentos 31/01/2027, 28/02/2027 e 31/03/2027 |
| CT-6 | CA-3 | unitário | Pedido SAC com prazo 73 meses, pelo serviço de cálculo | Recusado: "Prazo deve estar entre 1 e 72 meses" |
