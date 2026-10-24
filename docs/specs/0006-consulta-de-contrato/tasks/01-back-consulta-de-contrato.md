---
status: aprovada
atualizado: 2026-10-24
descricao: Adiciona ao módulo contracts a busca por número ou CPF e a consulta do contrato com saldo devedor e situação das parcelas.
---
# 01 · back · Consulta de contrato

Spec: [Consulta de contrato](../spec.md) · Depende de: nenhuma

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/contracts/domain/InstallmentSituation` | novo | Situação da parcela a partir do vencimento e de hoje (CON-05). |
| `.../contracts/domain/Contract` | alterado | Saldo devedor (CON-06). |
| `.../contracts/domain/BusinessCalendar` | novo | Dia de hoje no horário de Brasília, a partir do `Clock`. |
| `.../contracts/application/ContractQueryService` | novo | Consulta por id com situações e saldo, e busca por número ou CPF. |
| `.../contracts/application/ContractView`, `ContractSummary` | novo | Contrato consultado e item da lista. |
| `.../contracts/application/ContractNotFoundException` | novo | Contrato que não existe. |
| `.../contracts/application/port/ContractRepository` | alterado | Busca por prefixo do número, por CPF e os mais recentes. |
| `.../contracts/adapter/out/persistence/JdbcContractRepository` | alterado | SQL das buscas, com limite de 50. |
| `.../contracts/adapter/in/web/ContractController` | alterado | `GET /api/contracts` e `GET /api/contracts/{id}`. |
| `.../contracts/adapter/in/web/ContractResponse`, `ContractSummaryResponse` | alterado / novo | Corpos com saldo, situação e o item da lista. |
| `.../contracts/adapter/in/web/ContractErrorHandler` | alterado | 404 com `ProblemDetail`. |
| `backend/src/main/resources/db/migration/V2__index_contract_search.sql` | novo | Índices de CPF, de data do cadastro e de número em maiúsculas para a busca. |
| `backend/src/test/java/com/bancoexemplo/custody/contracts/application/InMemoryContractRepository` | alterado | Falso em memória com as três buscas. |
| `backend/src/test/java/com/bancoexemplo/custody/contracts/ContractFixture` | novo | Contrato do exemplo da spec para os testes do módulo. |
| `docs/specs/0006-consulta-de-contrato/spec.md` | alterado | Seção Dados: barras invertidas tiradas e o índice de número em maiúsculas. |

## Testes desta task
- [x] Unitário de domínio `ContractQueryRulesTest`: CT-1 a CT-5.
- [x] Unitário de caso de uso `ContractQueryServiceTest`, com repositório falso e relógio fixo: situação e saldo na consulta, 404 e escolha entre CPF e número.
- [x] API `ContractQueryApiTest`: CT-6 a CT-12.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6, CT-7, CT-8, CT-9, CT-10, CT-11, CT-12

## Checklist
- [x] Testes de domínio e de caso de uso antes do código.
- [x] `./gradlew build` e `./gradlew pitest` verdes, mutação acima de 80%.
