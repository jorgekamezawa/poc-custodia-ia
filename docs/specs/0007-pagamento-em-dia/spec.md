---
status: entregue
atualizado: 2026-10-24
descricao: Registro do pagamento em dia de uma parcela pelo portal, com a parcela marcada como paga e o saldo devedor atualizado no contrato por evento.
---
# Pagamento de parcela em dia

História: [#15 Registrar pagamento de parcela em dia](https://github.com/jorgekamezawa/poc-custodia-ia/issues/15)

## Problema
O operador recebe a confirmação de que o cliente pagou uma parcela e precisa registrar isso. Sem o registro, a parcela aparece vencida e o saldo devedor não cai.

## Fora de escopo
- Pagamento depois do vencimento, com multa e juros de mora: história #16.
- Pagamento parcial, adiantado de várias parcelas ou quitação: história #18.
- Estorno de pagamento registrado por engano.

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| adicionada | PAG-01 | Só a parcela não paga mais antiga pode ser paga; outra é recusada com "Pague antes a parcela N". |
| adicionada | PAG-02 | O valor pago é igual ao valor da parcela; outro é recusado com "Valor deve ser igual ao da parcela: R$ X". |
| adicionada | PAG-03 | A data do pagamento fica entre a liberação e hoje e não passa do vencimento da parcela. Antes da liberação ou depois de hoje é recusada com "Data do pagamento deve ficar entre a liberação e hoje"; depois do vencimento, com "Pagamento depois do vencimento ainda não pode ser registrado". |
| adicionada | PAG-04 | Cada parcela é paga uma vez; um segundo pagamento é recusado com "Parcela já paga". |
| alterada | [CON-05](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/contratos.md) | Parcela paga aparece como "Paga", qualquer que seja o vencimento; a não paga segue "Em aberto" até o vencimento e "Vencida" depois. |

O saldo devedor passa a cair com o pagamento pela [CON-06](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/contratos.md), sem mudança no enunciado. Termos no [glossário](https://github.com/jorgekamezawa/custodia-docs/blob/main/glossario.md). Quem pode registrar: operador e supervisor.

## Critérios de aceite
- **CA-1.** Dado o contrato de R$ 10.000,00 a 2% ao mês em 12 meses, liberado em 10/09/2026, com a parcela 1 de R$ 945,60 vencendo em 10/11/2026, quando o operador registra em 24/10/2026 o pagamento de R$ 945,60 da parcela 1, então o pagamento é registrado e, na consulta do contrato, a parcela 1 aparece "Paga" e o saldo devedor é R$ 9.254,40.
- **CA-2.** Dada a parcela 1 em aberto, quando alguém tenta pagar a parcela 2, então é recusado com "Pague antes a parcela 1".
- **CA-3.** Dado um valor diferente do da parcela, quando o pagamento é enviado, então é recusado com "Valor deve ser igual ao da parcela: R$ 945,60".
- **CA-4.** Dada uma data de pagamento depois do vencimento, quando o pagamento é enviado, então é recusado com "Pagamento depois do vencimento ainda não pode ser registrado".
- **CA-5.** Dada uma data de pagamento antes da liberação ou depois de hoje, quando o pagamento é enviado, então é recusado com "Data do pagamento deve ficar entre a liberação e hoje".
- **CA-6.** Dada uma parcela já paga, quando alguém tenta pagá-la de novo, então é recusado com "Parcela já paga".
- **CA-7.** Dado que o contrato recebe o evento do mesmo pagamento duas vezes, quando o aplica, então a parcela fica paga uma vez e o saldo cai uma vez.
- **CA-8.** Dado um contrato que não existe, quando alguém registra pagamento para ele, então é recusado com "Contrato não encontrado".

## Telas
Sem esboço nesta história.
- **Contrato** (`/contratos/:id`): na linha da parcela não paga mais antiga, botão "Registrar pagamento". Ele abre um formulário com Data do pagamento (hoje, de início) e Valor (o da parcela, de início) e o botão Registrar.
- Pagamento aceito mostra "Pagamento registrado." e recarrega a consulta até a parcela aparecer "Paga" (o contrato recebe o evento em instantes). Recusa do servidor aparece no formulário, com a mensagem dele.

## Contratos de API
### POST /api/contracts/{contractId}/payments
Exposto pelo backend, chamado pelo portal. Exige sessão e o cabeçalho `X-XSRF-TOKEN` (proteção contra requisição forjada, [ADR-0013](../../adr/0013-sessao-com-cookie-e-csrf-no-portal.md)).

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| installmentNumber | inteiro | sim | PAG-01, PAG-04 | 1 |
| paidOn | data | sim | PAG-03 | 2026-10-24 |
| amount | número | sim | PAG-02; 2 casas | 945.60 |

Resposta 201, com `Location: /api/contracts/{contractId}/payments/{id}`:

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| id | texto | sim | UUID | 7c1d... |
| contractId | texto | sim | UUID | 3f2a... |
| installmentNumber | inteiro | sim | | 1 |
| paidOn | data | sim | | 2026-10-24 |
| amount | número | sim | | 945.60 |
| registeredBy | texto | sim | | operador |
| registeredAt | instante | sim | UTC | 2026-10-24T15:00:00Z |

| Código | Quando |
|---|---|
| 201 | Registrado |
| 400 | PAG-01, PAG-02 ou PAG-03; corpo `{"detail": "<mensagem>"}` |
| 401 | Sem sessão |
| 403 | Sem o cabeçalho de CSRF |
| 404 | Contrato não existe |
| 409 | PAG-04 |

## Dados
### payment (criada)
| Coluna | Tipo | Nulo | Restrições | Descrição |
|---|---|---|---|---|
| id | uuid | não | chave primária | Identificador do pagamento |
| contract_id | uuid | não | | Contrato (sem chave estrangeira: a tabela é de outro módulo) |
| installment_number | integer | não | | Parcela paga |
| paid_on | date | não | | Data do pagamento |
| amount | numeric(15,2) | não | | Valor pago |
| registered_by | varchar(60) | não | | Usuário que registrou |
| registered_at | timestamptz | não | | Instante do registro |

Chaves e índices: único (`contract_id`, `installment_number`), que garante a PAG-04.

### contract_installment (alterada)
| Coluna | Tipo | Nulo | Restrições | Descrição |
|---|---|---|---|---|
| paid_on | date | sim | | Data do pagamento; nula enquanto não paga |

### outbox_event e processed_event (criadas)
Tabelas da [tabela de saída e do consumidor idempotente](../../adr/0017-outbox-e-consumidor-idempotente.md).

| Coluna | Tipo | Nulo | Restrições | Descrição |
|---|---|---|---|---|
| outbox_event.id | uuid | não | chave primária | Identificador do evento |
| outbox_event.topic | varchar(100) | não | | Tópico de destino |
| outbox_event.event_key | varchar(100) | não | | Chave da mensagem |
| outbox_event.payload | jsonb | não | | Corpo do evento |
| outbox_event.created_at | timestamptz | não | | Quando foi gravado |
| outbox_event.published_at | timestamptz | sim | índice parcial onde é nula | Quando saiu para o Kafka |
| processed_event.event_id | uuid | não | chave primária com `consumer` | Evento aplicado |
| processed_event.consumer | varchar(60) | não | | Quem aplicou |
| processed_event.processed_at | timestamptz | não | | Quando aplicou |

## Eventos ou arquivos
### Tópico custody.payment-registered
Tópico do Kafka (registro de eventos entre módulos, [ADR-0016](../../adr/0016-eventos-entre-modulos-pelo-kafka.md)), publicado pelo módulo de pagamentos e consumido pelo módulo de contratos. Chave: `contractId`.

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| eventId | texto | sim | UUID, único | 9b0e... |
| occurredAt | instante | sim | UTC | 2026-10-24T15:00:00Z |
| contractId | texto | sim | UUID | 3f2a... |
| installmentNumber | inteiro | sim | | 1 |
| paidOn | data | sim | | 2026-10-24 |
| amount | número | sim | 2 casas | 945.60 |

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-1 | unitário | Parcela 1 em aberto, R$ 945,60 em 24/10/2026, hoje 24/10/2026 | Pagamento aceito e guardado |
| CT-2 | CA-2 | unitário | Parcela 2 com a 1 em aberto | Recusado: "Pague antes a parcela 1" |
| CT-3 | CA-3 | unitário | R$ 945,59 na parcela de R$ 945,60 | Recusado: "Valor deve ser igual ao da parcela: R$ 945,60" |
| CT-4 | CA-3 | unitário | R$ 945,61 na parcela de R$ 945,60 | Recusado com a mensagem do CT-3 |
| CT-5 | CA-4 | unitário | Data 11/11/2026, vencimento 10/11/2026, hoje 11/11/2026 | Recusado: "Pagamento depois do vencimento ainda não pode ser registrado" |
| CT-6 | CA-1 | unitário | Data 10/11/2026, no dia do vencimento, hoje 10/11/2026 | Aceito |
| CT-7 | CA-5 | unitário | Data 25/10/2026, hoje 24/10/2026 | Recusado: "Data do pagamento deve ficar entre a liberação e hoje" |
| CT-8 | CA-5 | unitário | Data 09/09/2026, liberação 10/09/2026 | Recusado com a mensagem do CT-7 |
| CT-9 | CA-6 | unitário | Parcela 1 já com pagamento guardado | Recusado: "Parcela já paga" |
| CT-10 | CA-1 | unitário | Pagamento do CT-1 | Evento `custody.payment-registered` com contrato, parcela, data e valor entregue à tabela de saída |
| CT-11 | CA-1 | unitário | Contrato recebe o evento do CT-1 | Parcela 1 com `paid_on` 24/10/2026; saldo devedor R$ 9.254,40 |
| CT-12 | CA-7 | integração | O mesmo evento aplicado duas vezes pelo consumidor do contrato | Uma linha em `processed_event`; parcela paga uma vez |
| CT-13 | CA-1 | unitário | Parcela paga com vencimento já passado | Situação `PAID` |
| CT-14 | CA-1 | integração | Linha nova em `outbox_event` | Mensagem no tópico com a mesma chave e corpo; `published_at` preenchida |
| CT-15 | CA-1 | integração | Transação que grava o evento e depois falha | Nenhuma linha em `outbox_event` |
| CT-16 | CA-1 | integração | POST do pagamento do CA-1, depois GET do contrato | 201; em até 10 segundos a parcela 1 aparece `PAID` e `outstandingBalance` 9254.40 |
| CT-17 | CA-6 | integração | Segundo POST para a parcela 1 | 409 com "Parcela já paga" |
| CT-18 | CA-3 | integração | POST com `amount` 945.59 | 400 com a mensagem do CT-3 |
| CT-19 | CA-8 | integração | POST para UUID de contrato que não existe | 404 com "Contrato não encontrado" |
| CT-20 | CA-1 | integração | POST sem sessão | 401 |
| CT-21 | CA-1 | componente | Contrato com parcela 1 paga e 2 em aberto | "Registrar pagamento" só na linha da parcela 2, com valor dela e data de hoje de início |
| CT-22 | CA-1 | componente | Envio aceito | "Pagamento registrado." e nova consulta do contrato |
| CT-23 | CA-3 | componente | Envio recusado com 400 | Mensagem do servidor no formulário |
| CT-24 | CA-1 | ponta a ponta | Cadastrar contrato liberado há 30 dias e primeiro vencimento daqui a 15 dias; registrar o pagamento da parcela 1 hoje | Parcela 1 "Paga" e saldo devedor menor que o valor financiado |
| CT-25 | CA-3 | ponta a ponta | Registrar o pagamento da parcela 1 com um centavo a menos | Mensagem "Valor deve ser igual ao da parcela" na tela |
