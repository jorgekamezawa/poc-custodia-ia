---
status: aprovada
atualizado: 2026-10-24
descricao: Cálculo do CET (custo efetivo total) de um contrato, ao ano, pela fórmula da Resolução CMN 4.881/2020, sobre o cronograma Price ou SAC.
---
# Custo efetivo total do contrato

História: [#10 Calcular o CET do contrato](https://github.com/jorgekamezawa/poc-custodia-ia/issues/10)

## Problema
O CET (custo efetivo total: taxa ao ano que soma juros, tarifas e impostos) precisa constar do contrato e da consulta do operador. A taxa mensal do contrato não mostra o custo real quando há custos pagos antecipadamente, e cada módulo calcular o CET do seu jeito daria valores diferentes.

## Fora de escopo
- Cálculo do IOF e de seguros: o valor já vem somado nos custos pagos antecipadamente ou no valor financiado.
- Taxa flutuante ou indexada: a Resolução CMN 4.881/2020 manda não considerá-la, e a custódia não tem esse produto.
- Mudança no cronograma: vale a [CAL-01 a CAL-04](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/calculadoras.md) como estão.

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| adicionada | CAL-05 | O CET é a taxa anual que iguala o valor liberado (valor financiado menos os custos pagos antecipadamente) à soma das parcelas descontadas por `(1 + CET)^(dias entre a liberação e o vencimento ÷ 365)`. É expresso em percentual ao ano, com 2 casas, arredondado pelo meio para o par (empate vai ao dígito par, como na [GER-01](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/gerais.md)). Fonte: [Resolução CMN 4.881/2020, art. 4º](https://www.bcb.gov.br/content/estabilidadefinanceira/especialnor/Resolu%C3%A7%C3%A3o4881.pdf). |
| adicionada | CAL-06 | A data de liberação é anterior ao primeiro vencimento, e os custos pagos antecipadamente vão de R$ 0,00 até menos que o valor financiado. Fora disso, o cálculo é recusado dizendo qual campo está fora. |

## Critérios de aceite
- **CA-1.** Dado contrato Price de R$ 10.000,00 a 2% ao mês em 12 meses, liberado em 10/01/2027 com primeiro vencimento em 10/02/2027 e sem custos antecipados, quando o CET é calculado, então ele é 26,95% ao ano.
- **CA-2.** Dado o contrato do CA-1 com R$ 300,00 de custos pagos antecipadamente, quando o CET é calculado, então ele é 34,65% ao ano.
- **CA-3.** Dado o contrato do CA-1 pelo SAC, quando o CET é calculado, então ele é 26,96% ao ano.
- **CA-4.** Dado data de liberação no primeiro vencimento ou depois, ou custos antecipados fora da CAL-06, quando o CET é pedido, então ele é recusado com a mensagem do campo fora do limite.
- **CA-5.** Dado um pedido cujo cronograma é recusado pela CAL-03, quando o CET é pedido, então ele é recusado com a mesma mensagem.

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-1 | unitário | Price R$ 10.000,00; 2% a.m.; 12 meses; liberação 10/01/2027; 1º vencimento 10/02/2027; custos R$ 0,00 | CET 26,95% a.a. |
| CT-2 | CA-2 | unitário | Mesma do CT-1 com custos de R$ 300,00 | CET 34,65% a.a. |
| CT-3 | CA-3 | unitário | Mesma do CT-1 pelo SAC | CET 26,96% a.a. |
| CT-4 | CA-1 | unitário | Price R$ 1.000,00; 1,5% a.m.; 1 mês; liberação 10/01/2027; vencimento 10/02/2027 | CET 19,16% a.a. |
| CT-5 | CA-1 | unitário | Mesma do CT-1 | CET com escala 2 |
| CT-6 | CA-4 | unitário | Liberação em 10/02/2027 com 1º vencimento em 10/02/2027 | Recusado: "Data de liberação deve ser anterior ao primeiro vencimento" |
| CT-7 | CA-4 | unitário | Liberação em 11/02/2027 com 1º vencimento em 10/02/2027 | Recusado: "Data de liberação deve ser anterior ao primeiro vencimento" |
| CT-8 | CA-4 | unitário | Custos de -R$ 0,01 sobre valor de R$ 10.000,00 | Recusado: "Custos antecipados devem ficar entre R$ 0,00 e menos que o valor financiado" |
| CT-9 | CA-4 | unitário | Custos de R$ 10.000,00 sobre valor de R$ 10.000,00 | Recusado com a mensagem do CT-8 |
| CT-10 | CA-4 | unitário | Custos de R$ 9.999,99 sobre valor de R$ 10.000,00 | Aceito |
| CT-11 | CA-5 | unitário | Prazo de 73 meses | Recusado: "Prazo deve estar entre 1 e 72 meses" |
