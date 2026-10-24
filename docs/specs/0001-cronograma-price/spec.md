---
status: entregue
atualizado: 2026-10-24
descricao: Cálculo do cronograma de parcelas pela tabela Price (parcelas de mesmo valor), a partir de valor financiado, taxa mensal, prazo e primeiro vencimento.
---
# Cronograma pela tabela Price

História: [#8 Calcular cronograma pela tabela Price](https://github.com/jorgekamezawa/poc-custodia-ia/issues/8)

## Problema
Contratos, Antecipação e Alteração de plano precisam do cronograma de parcelas de um contrato Price, e hoje o operador calcula à mão. A custódia precisa de um cálculo único, exato ao centavo, que os outros módulos chamem.

## Fora de escopo
- Carência (prazo sem pagar) e parcela balão (parcela final maior): épico próprio. Termos no [glossário](https://github.com/jorgekamezawa/custodia-docs/blob/main/glossario.md).
- CET (custo efetivo total) e tarifas: história #10.
- Tela ou endpoint: o cálculo é chamado por outros módulos; a consulta no portal vem com o contrato (#13).
- Guardar o cronograma: é do módulo Contratos (#12).

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| adicionada | CAL-01 | A parcela Price é `valor financiado × taxa ÷ (1 − (1 + taxa)^−prazo)`, arredondada ao centavo (GER-01). Os juros remuneratórios (chamados só de juros nesta spec) de cada parcela são o saldo devedor anterior × taxa, arredondados; a amortização é a parcela menos os juros. A última parcela amortiza todo o saldo restante, absorvendo a diferença de arredondamento. Com taxa zero, a parcela é o valor financiado ÷ prazo. |
| adicionada | CAL-02 | O primeiro vencimento é informado; os seguintes caem no mesmo dia dos meses seguintes. Mês sem esse dia usa o último dia do mês, e o mês seguinte volta ao dia original. |
| adicionada | CAL-03 | Valor financiado de R$ 1.000,00 a R$ 1.000.000,00; taxa de 0% a 10% ao mês, com até 4 casas decimais; prazo de 1 a 72 meses. Fora disso, o cálculo é recusado dizendo qual campo está fora. |

## Critérios de aceite
- **CA-1.** Dado valor financiado de R$ 10.000,00, taxa de 2% ao mês e prazo de 12 meses, quando o cronograma é calculado, então as parcelas 1 a 11 valem R$ 945,60, a parcela 12 vale R$ 945,55 e o saldo devedor final é zero.
- **CA-2.** Dado um cronograma calculado, quando se olha cada parcela, então ela traz número, vencimento, valor, juros, amortização e saldo devedor depois dela, e juros mais amortização é igual ao valor.
- **CA-3.** Dado taxa zero, quando o cronograma é calculado, então não há juros e a diferença de arredondamento fica na última parcela.
- **CA-4.** Dado primeiro vencimento em 31/01/2027, quando o cronograma de 3 meses é calculado, então os vencimentos são 31/01/2027, 28/02/2027 e 31/03/2027.
- **CA-5.** Dado valor, taxa ou prazo fora dos limites da CAL-03, quando o cálculo é pedido, então ele é recusado com a mensagem do campo fora do limite.

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-1 | unitário | R$ 10.000,00; 2% a.m.; 12 meses | Parcelas 1 a 11 de R$ 945,60; parcela 12 de R$ 945,55; saldo final R$ 0,00 |
| CT-2 | CA-1, CA-2 | unitário | Mesma entrada do CT-1 | Parcela 1: juros R$ 200,00, amortização R$ 745,60, saldo R$ 9.254,40; parcela 2: juros R$ 185,09, amortização R$ 760,51, saldo R$ 8.493,89 |
| CT-3 | CA-2 | unitário | Mesma entrada do CT-1 | Em toda parcela, juros + amortização = valor; soma das amortizações = R$ 10.000,00 |
| CT-4 | CA-1 | unitário | R$ 1.000,00; 1,5% a.m.; 1 mês | Parcela única de R$ 1.015,00: juros R$ 15,00, amortização R$ 1.000,00 |
| CT-5 | CA-3 | unitário | R$ 10.000,00; 0% a.m.; 3 meses | Parcelas de R$ 3.333,33, R$ 3.333,33 e R$ 3.333,34, todas com juros R$ 0,00 |
| CT-6 | CA-4 | unitário | Primeiro vencimento 31/01/2027; 3 meses | Vencimentos 31/01/2027, 28/02/2027 e 31/03/2027 |
| CT-7 | CA-4 | unitário | Primeiro vencimento 31/01/2028; 2 meses | Vencimentos 31/01/2028 e 29/02/2028 (ano bissexto) |
| CT-8 | CA-5 | unitário | Valor R$ 999,99 e depois R$ 1.000.000,01 | Recusado: "Valor financiado deve estar entre R$ 1.000,00 e R$ 1.000.000,00" |
| CT-9 | CA-5 | unitário | Valor R$ 1.000,00 e R$ 1.000.000,00 (limites) | Aceito |
| CT-10 | CA-5 | unitário | Taxa -0,01%, 10,01% e 1,23456%; depois 10% | Os três primeiros recusados: "Taxa deve estar entre 0% e 10% ao mês, com até 4 casas decimais"; 10% aceito |
| CT-11 | CA-5 | unitário | Prazo 0 e 73 meses; depois 1 e 72 | 0 e 73 recusados: "Prazo deve estar entre 1 e 72 meses"; 1 e 72 aceitos |
