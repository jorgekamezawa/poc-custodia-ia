---
status: aprovada
atualizado: 2026-10-24
descricao: Busca de contratos por número ou CPF e consulta de um contrato com saldo devedor e situação de cada parcela, no portal.
---
# Consulta de contrato

História: [#13 Consultar contrato e parcelas no portal](https://github.com/jorgekamezawa/poc-custodia-ia/issues/13)

## Problema
Depois de cadastrado ([CON-01 a CON-04](https://github.com/jorgekamezawa/custodia-docs/blob/main/regras/contratos.md)), o contrato precisa ser achado e consultado: o cliente liga, o operador busca pelo número ou pelo CPF e precisa ver o saldo devedor e quais parcelas estão vencidas.

## Fora de escopo
- Registrar pagamento e a situação "Paga": história #15.
- Encargos de atraso no valor da parcela vencida: história #16.
- Exportar ou imprimir o contrato.

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| adicionada | CON-05 | Parcela não paga está "Em aberto" até o dia do vencimento e "Vencida" a partir do dia seguinte. O dia de hoje é a data no horário de Brasília. |
| adicionada | CON-06 | O saldo devedor do contrato é o saldo depois da última parcela paga; sem parcela paga, é o valor financiado. |

Termos no [glossário](https://github.com/jorgekamezawa/custodia-docs/blob/main/glossario.md).

## Critérios de aceite
- **CA-1.** Dado o contrato CTR-0001 cadastrado, quando o operador busca por "CTR-00", então o contrato aparece na lista com número, cliente, CPF, valor financiado e data do cadastro.
- **CA-2.** Dado o contrato do cliente de CPF 123.456.789-09, quando o operador busca por esse CPF, com ou sem pontuação, então os contratos desse CPF aparecem.
- **CA-3.** Dada uma busca sem resultado, quando a lista responde, então mostra "Nenhum contrato encontrado.".
- **CA-4.** Dado o contrato CTR-0001 sem parcela paga, quando o operador o abre, então vê os dados do cadastro, o CET, o saldo devedor de R$ 10.000,00 e as 12 parcelas, cada uma com a sua situação.
- **CA-5.** Dado hoje 24/10/2026 e um contrato com parcelas vencendo em 10/10/2026, 24/10/2026 e 10/11/2026, quando é consultado, então a primeira está "Vencida" e as outras duas "Em aberto".
- **CA-6.** Dado um endereço de contrato que não existe, quando é aberto, então a tela mostra "Contrato não encontrado.".
- **CA-7.** Dada a lista sem busca, quando abre, então mostra os 50 contratos cadastrados mais recentes, do mais novo ao mais antigo.

## Telas
Sem esboço nesta história.
- **Menu do portal:** itens "Contratos" (`/contratos`) e "Cadastrar contrato".
- **Contratos** (`/contratos`): campo "Buscar por número ou CPF" e botão Buscar; a busca vai para o endereço (`?busca=`), para o link ser compartilhável. Cada linha leva ao contrato.
- **Contrato** (`/contratos/:id`): grupos Contrato, Cliente, Veículo e Financiamento; CET; saldo devedor; tabela de parcelas com a coluna Situação.
- **Depois de cadastrar:** botão "Ver contrato" leva à consulta.

## Contratos de API
### GET /api/contracts
Exige sessão. Parâmetro `q` opcional: com 11 dígitos depois de tirar pontuação, busca o CPF exato; senão, busca contratos cujo número começa com o texto, sem diferenciar maiúscula. Sem `q`, os mais recentes. No máximo 50, do cadastro mais novo ao mais antigo.

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| id | texto | sim | UUID | 3f2a... |
| number | texto | sim | | CTR-0001 |
| customerName | texto | sim | | Cliente Exemplo |
| customerCpf | texto | sim | 11 dígitos | 12345678909 |
| principal | número | sim | 2 casas | 10000.00 |
| registeredAt | instante | sim | UTC | 2027-01-10T13:00:00Z |

| Código | Quando |
|---|---|
| 200 | Lista, vazia ou não |
| 401 | Sem sessão |

### GET /api/contracts/{id}
Exige sessão. Corpo igual ao da resposta do `POST /api/contracts` (spec 0005), mais:

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| outstandingBalance | número | sim | 2 casas; CON-06 | 10000.00 |
| installments[].situation | texto | sim | `OPEN` ou `OVERDUE`; CON-05 | OPEN |

| Código | Quando |
|---|---|
| 200 | Contrato encontrado |
| 401 | Sem sessão |
| 404 | Não existe; corpo `{"detail": "Contrato não encontrado"}` |

## Dados
### contract (alterada)
Sem coluna nova. Índices novos para a busca:

| Índice | Colunas | Motivo |
|---|---|---|
| contract_customer_cpf_idx | `customer_cpf` | Busca por CPF exato |
| contract_registered_at_idx | `registered_at` | Lista dos mais recentes |

A busca por prefixo do número usa o índice único de `number` da spec 0005, comparando em maiúsculas.

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-5 | unitário | Vencimento 24/10/2026, hoje 24/10/2026 | `OPEN` |
| CT-2 | CA-5 | unitário | Vencimento 23/10/2026, hoje 24/10/2026 | `OVERDUE` |
| CT-3 | CA-5 | unitário | Vencimento 10/11/2026, hoje 24/10/2026 | `OPEN` |
| CT-4 | CA-5 | unitário | Relógio em 25/10/2026 às 02:00 UTC (23:00 de 24/10 em Brasília), vencimento 24/10/2026 | `OPEN` |
| CT-5 | CA-4 | unitário | Contrato de R$ 10.000,00 sem parcela paga | Saldo devedor R$ 10.000,00 |
| CT-6 | CA-4 | integração | GET /api/contracts/{id} do contrato do exemplo da spec 0005 | 200 com o número, `outstandingBalance` 10000.00 e 12 parcelas com `situation` |
| CT-7 | CA-6 | integração | GET /api/contracts/{id} com UUID que não existe | 404 com "Contrato não encontrado" |
| CT-8 | CA-1 | integração | Dois contratos CTR-<prefixo>-1 e -2 e um terceiro com outro prefixo; GET com `q` = o prefixo em minúsculas | Os dois do prefixo, o mais novo primeiro |
| CT-9 | CA-2 | integração | Contrato de CPF 52998224725; GET com `q` = 529.982.247-25 | O contrato aparece |
| CT-10 | CA-3 | integração | GET com `q` = NAO-EXISTE | 200 com lista vazia |
| CT-11 | CA-7 | integração | 51 contratos cadastrados; GET sem `q` | 50 itens, o mais novo primeiro |
| CT-12 | CA-1 | integração | GET /api/contracts sem sessão | 401 |
| CT-13 | CA-1 | componente | Lista com dois contratos e busca "CTR" | Duas linhas; o endereço fica com `?busca=CTR` |
| CT-14 | CA-3 | componente | Lista vazia | "Nenhum contrato encontrado." |
| CT-15 | CA-4 | componente | Contrato com saldo 10000 e parcelas `OPEN` e `OVERDUE` | "R$ 10.000,00", "Em aberto" e "Vencida" na tabela |
| CT-16 | CA-6 | componente | Consulta que responde 404 | "Contrato não encontrado." |
| CT-17 | CA-1, CA-4 | ponta a ponta | Cadastrar, buscar pelo número em Contratos, abrir o contrato | Saldo R$ 10.000,00 e 12 parcelas "Em aberto" |
| CT-18 | CA-5 | ponta a ponta | Cadastrar com liberação em 10/01/2026 e primeiro vencimento em 10/02/2026, depois abrir | Primeira parcela "Vencida" e última "Em aberto" |
