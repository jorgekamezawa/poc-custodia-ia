---
status: aprovada
atualizado: 2026-10-24
descricao: Pagamento de parcela depois do vencimento, com multa de 2% e juros de mora de 1% ao mês pelos dias de atraso, e simulação do valor devido numa data.
---
# Multa e juros de mora

História: [#16 Cobrar multa e juros de mora de parcela atrasada](https://github.com/jorgekamezawa/poc-custodia-ia/issues/16)

## Problema
Hoje o pagamento depois do vencimento é recusado ([PAG-03](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/pagamentos.md)), e o operador não consegue registrar a parcela atrasada que o cliente pagou com multa e juros de mora. Ele também precisa saber quanto cobrar antes de registrar.

## Fora de escopo
- Juros remuneratórios durante o atraso e comissão de permanência.
- Renegociar ou parcelar o atraso: épico de renegociação.
- Avisar o cliente do atraso.

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| alterada | [PAG-02](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/pagamentos.md) | O valor pago é o valor devido na data: o da parcela, mais a multa e os juros de mora quando há atraso. Em dia, o diferente é recusado com "Valor deve ser igual ao da parcela: R$ X"; com atraso, com "Valor deve ser igual ao da parcela com multa e juros de mora: R$ X". |
| alterada | [PAG-03](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/pagamentos.md) | A data do pagamento fica entre a liberação e hoje; fora disso é recusada com "Data do pagamento deve ficar entre a liberação e hoje". Depois do vencimento, o pagamento passa a ser aceito com a multa (PAG-05) e os juros de mora (PAG-06). |
| adicionada | PAG-05 | Parcela paga depois do vencimento tem multa de mora de 2% do valor da parcela, cobrada uma vez e arredondada ao centavo ([GER-01](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/gerais.md)). Fonte: [CDC, art. 52, §1º](https://www.planalto.gov.br/ccivil_03/leis/l8078compilado.htm), que limita a multa a 2% do valor da prestação. |
| adicionada | PAG-06 | Parcela paga depois do vencimento tem juros de mora de 1% ao mês, simples, proporcionais aos dias corridos de atraso: valor da parcela × 1% ÷ 30 × dias, arredondados ao centavo. |

## Critérios de aceite
- **CA-1.** Dada a parcela 1 de R$ 945,60 vencida em 10/10/2026, quando o operador registra em 24/10/2026 (14 dias de atraso) o pagamento de R$ 968,92, então o pagamento é registrado com multa de R$ 18,91 e juros de mora de R$ 4,41, e a parcela aparece "Paga".
- **CA-2.** Dada a mesma parcela, quando o operador pede o valor devido em 24/10/2026, então recebe parcela R$ 945,60, multa R$ 18,91, juros de mora R$ 4,41 e total R$ 968,92.
- **CA-3.** Dado 1 dia de atraso, quando o valor devido é calculado, então a multa é R$ 18,91, os juros R$ 0,32 e o total R$ 964,83.
- **CA-4.** Dado o pagamento no dia do vencimento, quando o valor devido é calculado, então não há multa nem juros e o total é R$ 945,60.
- **CA-5.** Dado um pagamento atrasado de valor diferente do devido, quando é enviado, então é recusado com "Valor deve ser igual ao da parcela com multa e juros de mora: R$ 968,92".
- **CA-7.** Dada uma parcela já paga, quando o operador pede o valor devido, então é recusado com "Parcela já paga".
- **CA-6.** Dada a tela de pagamento, quando o operador escolhe a data, então o valor sugerido passa a ser o devido nessa data, com a multa e os juros mostrados.

## Telas
Sem esboço nesta história.
- **Formulário de pagamento** (tela Contrato): ao abrir e a cada mudança de data, pede o valor devido; mostra Parcela, Multa, Juros de mora e Total e preenche o Valor com o Total. Recusa da simulação (ex.: data fora do intervalo) aparece no formulário.

## Contratos de API
### GET /api/contracts/{contractId}/payments/quote
Exige sessão. Aplica as mesmas recusas do registro (PAG-01, PAG-03, PAG-04, contrato que não existe).

Parâmetros de consulta:

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| installmentNumber | inteiro | sim | | 1 |
| paidOn | data (AAAA-MM-DD) | sim | PAG-03 | 2026-10-24 |

Resposta 200:

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| installmentNumber | inteiro | sim | | 1 |
| installmentAmount | número | sim | 2 casas | 945.60 |
| daysLate | inteiro | sim | 0 em dia | 14 |
| lateFee | número | sim | PAG-05 | 18.91 |
| defaultInterest | número | sim | PAG-06 | 4.41 |
| total | número | sim | soma dos três | 968.92 |

| Código | Quando |
|---|---|
| 200 | Valor calculado |
| 400 | PAG-01 ou PAG-03 |
| 401 | Sem sessão |
| 404 | Contrato não existe |
| 409 | PAG-04 |

### POST /api/contracts/{contractId}/payments (alterado)
Corpo igual ao da spec 0007. A resposta 201 ganha dois campos; `amount` segue sendo o total pago.

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| lateFee | número | sim | PAG-05; 0 em dia | 18.91 |
| defaultInterest | número | sim | PAG-06; 0 em dia | 4.41 |

## Dados
### payment (alterada)
| Coluna | Tipo | Nulo | Restrições | Descrição |
|---|---|---|---|---|
| lateFee | numeric(15,2) | não | padrão 0 | Multa de mora cobrada |
| default_interest | numeric(15,2) | não | padrão 0 | Juros de mora cobrados |

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-2 | unitário | Parcela R$ 945,60, 14 dias de atraso | Multa R$ 18,91, juros R$ 4,41, total R$ 968,92 |
| CT-2 | CA-3 | unitário | Parcela R$ 945,60, 1 dia de atraso | Multa R$ 18,91, juros R$ 0,32, total R$ 964,83 |
| CT-3 | CA-4 | unitário | Parcela R$ 945,60, 0 dia de atraso | Multa R$ 0,00, juros R$ 0,00, total R$ 945,60 |
| CT-4 | CA-2 | unitário | Parcela R$ 945,60, 60 dias de atraso | Multa R$ 18,91, juros R$ 18,91, total R$ 983,42 |
| CT-5 | CA-1 | unitário | Pagamento de R$ 968,92 em 24/10/2026 da parcela vencida em 10/10/2026 | Aceito, com multa R$ 18,91 e juros R$ 4,41 guardados |
| CT-6 | CA-5 | unitário | Pagamento de R$ 945,60 em 24/10/2026 da parcela vencida em 10/10/2026 | Recusado: "Valor deve ser igual ao da parcela com multa e juros de mora: R$ 968,92" |
| CT-7 | CA-2 | unitário | Simulação da parcela vencida em 10/10/2026 para 24/10/2026 | Parcela 945,60, 14 dias, multa 18,91, juros 4,41, total 968,92 |
| CT-8 | CA-2 | integração | GET da simulação da parcela 1, vencida há 14 dias, para hoje | 200 com `daysLate` 14, `lateFee` 18.91, `defaultInterest` 4.41 e `total` 968.92 |
| CT-9 | CA-1 | integração | POST da parcela 1 vencida há 14 dias, hoje, com 968.92 | 201 com `lateFee` 18.91 e `defaultInterest` 4.41 |
| CT-10 | CA-7 | integração | GET da simulação de parcela já paga | 409 com "Parcela já paga" |
| CT-11 | CA-6 | componente | Formulário aberto para a parcela vencida, simulação respondendo total 968.92 | Mostra multa e juros e preenche o Valor com 968.92; nova data pede nova simulação |
| CT-12 | CA-1, CA-6 | ponta a ponta | Contrato com a parcela 1 vencida há 14 dias; registrar o pagamento hoje com o valor sugerido | Formulário mostra total R$ 968,92; depois, parcela 1 "Paga" |
