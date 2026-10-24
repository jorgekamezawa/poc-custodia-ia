---
status: entregue
atualizado: 2026-10-24
descricao: Cadastro do contrato de financiamento de veículo já assinado, com cronograma e CET calculados e guardados, pelo portal.
---
# Cadastro de contrato

História: [#12 Cadastrar contrato assinado com seu cronograma](https://github.com/jorgekamezawa/poc-custodia-ia/issues/12)

## Problema
A custódia começa quando o contrato assinado chega do time de contratação. Hoje o operador digita os dados numa planilha e calcula o cronograma à mão. O cadastro no portal guarda o contrato com o cronograma e o CET calculados pela custódia, ponto de partida de pagamento, quitação e gravame.

## Fora de escopo
- Receber o contrato por integração com o time de contratação: o operador digita.
- Registrar o gravame: história #20, disparada pelo cadastro.
- Consultar e listar contratos: história #13.
- Alterar ou excluir um contrato cadastrado.

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| adicionada | CON-01 | O número do contrato é único na custódia; um segundo cadastro com o mesmo número é recusado com "Número do contrato já cadastrado". |
| adicionada | CON-02 | Dados obrigatórios do cadastro: número do contrato (1 a 30 letras, números ou hífen), nome do cliente (até 120 caracteres), CPF com dígitos verificadores válidos, chassi com 17 letras e números sem I, O e Q, e descrição do veículo (até 120 caracteres). Campo fora disso é recusado com a mensagem do campo. |
| adicionada | CON-03 | O cronograma e o CET são calculados no cadastro, pela [CAL-01 a CAL-06](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/calculadoras.md), e guardados com o contrato, que nasce ativo. |
| adicionada | CON-04 | O cadastro guarda quem cadastrou e quando. |

Termos no [glossário](https://github.com/jorgekamezawa/custodia-docs/blob/main/glossario.md). Quem pode cadastrar: operador e supervisor ([GER-04](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/gerais.md)).

## Critérios de aceite
- **CA-1.** Dado o contrato CTR-0001 de R$ 10.000,00 a 2% ao mês em 12 meses pelo Price, liberado em 10/01/2027, primeiro vencimento em 10/02/2027 e sem custos pagos antecipadamente, quando o operador o cadastra, então ele fica ativo, com 12 parcelas (as 11 primeiras de R$ 945,60 e a última de R$ 945,55) e CET de 26,95% ao ano.
- **CA-2.** Dado que o CTR-0001 já existe, quando alguém tenta cadastrar outro contrato com o mesmo número, então o cadastro é recusado com "Número do contrato já cadastrado".
- **CA-3.** Dado um campo fora da CON-02 (ex.: CPF 123.456.789-00), quando o cadastro é enviado, então é recusado com a mensagem do campo e nada é guardado.
- **CA-4.** Dado valor, taxa, prazo, datas ou custos fora da CAL-03 ou da CAL-06, quando o cadastro é enviado, então é recusado com a mensagem da regra de Calculadoras e nada é guardado.
- **CA-5.** Dado o operador logado, quando cadastra um contrato, então o contrato guarda o usuário `operador` e o instante do cadastro.
- **CA-6.** Dado o cadastro aceito, quando a tela responde, então mostra o número, o CET e a tabela de parcelas com vencimento, valor, juros remuneratórios, amortização e saldo devedor.

## Telas
Sem esboço nesta história.
- **Menu da moldura:** item "Cadastrar contrato" que leva a `/contratos/novo`.
- **Cadastrar contrato** (`/contratos/novo`): grupos Contrato (número), Cliente (nome, CPF), Veículo (chassi, descrição) e Financiamento (sistema Price ou SAC, valor financiado, taxa ao mês em %, prazo em meses, data de liberação, primeiro vencimento, custos pagos antecipadamente, que começa em 0). Campo vazio mostra "Campo obrigatório" e não envia. Recusa do servidor aparece no topo do formulário, com a mensagem dele.
- **Depois de cadastrar:** "Contrato CTR-0001 cadastrado.", o CET e a tabela de parcelas; botão "Cadastrar outro" limpa o formulário.

## Contratos de API
### POST /api/contracts
Exposto pelo backend, chamado pelo portal. Exige sessão e o cabeçalho `X-XSRF-TOKEN` ([ADR-0013](../../adr/0013-sessao-com-cookie-e-csrf-no-portal.md)). Corpo JSON:

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| number | texto | sim | CON-02 | CTR-0001 |
| customerName | texto | sim | CON-02 | Cliente Exemplo |
| customerCpf | texto | sim | 11 dígitos, sem pontuação; CON-02 | 12345678909 |
| chassisNumber | texto | sim | CON-02 | 9BWZZZ377VT004251 |
| vehicleDescription | texto | sim | CON-02 | Hatch 1.0 2026 |
| amortizationSystem | texto | sim | `PRICE` ou `SAC` | PRICE |
| principal | número | sim | CAL-03; 2 casas | 10000.00 |
| monthlyRatePercent | número | sim | CAL-03 | 2 |
| termMonths | inteiro | sim | CAL-03 | 12 |
| releaseDate | data | sim | CAL-06 | 2027-01-10 |
| firstDueDate | data | sim | CAL-06 | 2027-02-10 |
| upfrontCosts | número | sim | CAL-06 | 0 |

Resposta 201, com `Location: /api/contracts/{id}`:

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| id | texto | sim | UUID | 3f2a... |
| number | texto | sim | | CTR-0001 |
| status | texto | sim | `ACTIVE` | ACTIVE |
| annualCetPercent | número | sim | 2 casas | 26.95 |
| registeredBy | texto | sim | | operador |
| registeredAt | instante | sim | UTC | 2027-01-10T13:00:00Z |
| installments | lista | sim | number, dueDate, amount, interest, amortization, balanceAfter | |

Também ecoa os campos do pedido.

| Código | Quando |
|---|---|
| 201 | Cadastrado |
| 400 | Campo fora da CON-02, da CAL-03 ou da CAL-06; corpo `{"detail": "<mensagem>"}` |
| 401 | Sem sessão |
| 403 | Sem o cabeçalho de CSRF |
| 409 | Número já cadastrado (CON-01); corpo `{"detail": "Número do contrato já cadastrado"}` |

## Dados
### contract (criada)
| Coluna | Tipo | Nulo | Restrições | Descrição |
|---|---|---|---|---|
| id | uuid | não | chave primária | Identificador interno |
| number | varchar(30) | não | único | Número do contrato |
| customer_name | varchar(120) | não | | Nome do cliente |
| customer_cpf | char(11) | não | | CPF só com dígitos |
| chassis_number | char(17) | não | | Chassi |
| vehicle_description | varchar(120) | não | | Descrição do veículo |
| amortization_system | varchar(10) | não | `PRICE` ou `SAC` | Sistema de amortização |
| principal | numeric(15,2) | não | | Valor financiado |
| monthly_rate_percent | numeric(7,4) | não | | Taxa ao mês, em % |
| term_months | integer | não | | Prazo em meses |
| release_date | date | não | | Data de liberação |
| first_due_date | date | não | | Primeiro vencimento |
| upfront_costs | numeric(15,2) | não | | Custos pagos antecipadamente |
| annual_cet_percent | numeric(9,2) | não | | CET ao ano, em % |
| status | varchar(20) | não | `ACTIVE` | Situação |
| registered_by | varchar(60) | não | | Usuário que cadastrou |
| registered_at | timestamptz | não | | Instante do cadastro |

### contract_installment (criada)
| Coluna | Tipo | Nulo | Restrições | Descrição |
|---|---|---|---|---|
| contract_id | uuid | não | chave estrangeira para `contract` | Contrato |
| number | integer | não | | Número da parcela |
| due_date | date | não | | Vencimento |
| amount | numeric(15,2) | não | | Valor |
| interest | numeric(15,2) | não | | Juros remuneratórios |
| amortization | numeric(15,2) | não | | Amortização |
| balance_after | numeric(15,2) | não | | Saldo devedor depois da parcela |

Chaves e índices: `contract_installment` tem chave primária (`contract_id`, `number`); `contract.number` tem índice único.

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-1 | unitário | Cadastro do CA-1 com calculador falso que devolve 12 parcelas e CET 26,95 | Contrato `ACTIVE` com as 12 parcelas e o CET guardados |
| CT-2 | CA-2 | unitário | Cadastro de CTR-0001 quando o repositório falso já tem CTR-0001 | Recusado: "Número do contrato já cadastrado"; nada guardado |
| CT-3 | CA-3 | unitário | CPF 12345678900 | Recusado: "CPF inválido" |
| CT-4 | CA-3 | unitário | CPF 11111111111 (dígitos iguais) | Recusado: "CPF inválido" |
| CT-5 | CA-3 | unitário | Chassi com 16 caracteres | Recusado: "Chassi deve ter 17 letras e números, sem I, O e Q" |
| CT-6 | CA-3 | unitário | Chassi 9BWZZZ377VT00425I (com I) | Recusado com a mensagem do CT-5 |
| CT-7 | CA-3 | unitário | Número com 31 caracteres | Recusado: "Número do contrato deve ter de 1 a 30 letras, números ou hífen" |
| CT-8 | CA-3 | unitário | Número "CTR 0001" (com espaço) | Recusado com a mensagem do CT-7 |
| CT-9 | CA-3 | unitário | Nome do cliente vazio | Recusado: "Nome do cliente deve ter de 1 a 120 caracteres" |
| CT-10 | CA-3 | unitário | Nome do cliente com 121 caracteres | Recusado com a mensagem do CT-9 |
| CT-11 | CA-3 | unitário | Descrição do veículo vazia | Recusado: "Descrição do veículo deve ter de 1 a 120 caracteres" |
| CT-12 | CA-5 | unitário | Cadastro pelo usuário `operador` com relógio fixo em 2027-01-10T13:00:00Z | `registeredBy` = operador; `registeredAt` = 2027-01-10T13:00:00Z |
| CT-13 | CA-1 | integração | Repositório grava e lê o contrato do CA-1 | Mesmos dados e 12 parcelas de volta |
| CT-14 | CA-2 | integração | Banco recebe dois contratos com o número CTR-0001 | O segundo é recusado pelo índice único |
| CT-15 | CA-1 | integração | POST /api/contracts com o corpo do exemplo, logado como operador | 201, `Location`, `status` ACTIVE, `annualCetPercent` 26.95, parcela 12 com `amount` 945.55 |
| CT-16 | CA-2 | integração | POST repetido com o número CTR-0001 | 409 com "Número do contrato já cadastrado" |
| CT-17 | CA-3 | integração | POST com `customerCpf` 12345678900 | 400 com "CPF inválido"; nada guardado |
| CT-18 | CA-4 | integração | POST com `termMonths` 73 | 400 com "Prazo deve estar entre 1 e 72 meses"; nada guardado |
| CT-19 | CA-4 | integração | POST com `releaseDate` igual a `firstDueDate` | 400 com "Data de liberação deve ser anterior ao primeiro vencimento" |
| CT-20 | CA-5 | integração | POST logado como operador | `registeredBy` = operador |
| CT-21 | CA-1 | integração | POST sem sessão | 401 |
| CT-22 | CA-3 | componente | Enviar com os campos vazios | "Campo obrigatório" nos campos; nada é enviado |
| CT-23 | CA-6 | componente | Envio aceito com a resposta do CT-15 | "Contrato CTR-0001 cadastrado.", CET "26,95%" e 12 linhas na tabela |
| CT-24 | CA-2 | componente | Envio recusado com 409 | Mensagem "Número do contrato já cadastrado" no topo; dados mantidos |
| CT-25 | CA-1, CA-6 | ponta a ponta | Operador entra, abre Cadastrar contrato pelo menu e cadastra o exemplo do CA-1 com número único | Confirmação com o número, CET 26,95% e 12 parcelas, a última de R$ 945,55 |
| CT-26 | CA-2 | ponta a ponta | Cadastra de novo o mesmo número | "Número do contrato já cadastrado" na tela |
