---
status: aprovada
atualizado: 2026-10-24
descricao: Cria o módulo contracts com o cadastro do contrato, o cálculo pelo módulo calculator, a gravação no PostgreSQL e o POST /api/contracts.
---
# 01 · back · Cadastro de contrato

Spec: [Cadastro de contrato](../spec.md) · Depende de: nenhuma

## Abordagem técnica
- Banco com `JdbcClient` e SQL à mão ([ADR-0015](../../../adr/0015-acesso-a-banco-com-jdbcclient.md)); tabelas pela migração Flyway `V1`.
- O contrato pede cronograma e CET por uma porta própria (`FinancingCalculator`); o adaptador de saída traduz para a `api/` do módulo calculator e converte a recusa dele em recusa do cadastro.
- Erros da API em `ProblemDetail` (formato padrão do Spring para erro em JSON), com a mensagem em `detail`.
- `Clock` como bean da aplicação, para o instante do cadastro ser fixado nos testes.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/ClockConfig` | novo | Bean `Clock` em UTC. |
| `.../contracts/domain/Contract` | novo | Contrato cadastrado: dados, cronograma, CET, situação, quem e quando cadastrou (CON-03, CON-04). |
| `.../contracts/domain/ContractInstallment` | novo | Parcela guardada com o contrato. |
| `.../contracts/domain/ContractNumber`, `Cpf`, `ChassisNumber`, `TextField` | novo | Validação da CON-02, cada um com a sua mensagem. |
| `.../contracts/domain/ContractStatus` | novo | Situação do contrato; por ora `ACTIVE`. |
| `.../contracts/domain/InvalidContractException` | novo | Recusa de dado do cadastro, com a mensagem para o operador. |
| `.../contracts/domain/DuplicateContractNumberException` | novo | Recusa da CON-01. |
| `.../contracts/application/RegisterContractService` | novo | Caso de uso: valida, recusa número repetido, pede cronograma e CET, monta e grava o contrato. |
| `.../contracts/application/RegisterContractCommand` | novo | Dados do cadastro e o usuário que cadastra. |
| `.../contracts/application/port/ContractRepository` | novo | Porta de gravação e busca por número. |
| `.../contracts/application/port/FinancingCalculator` | novo | Porta do cálculo de cronograma e CET, na linguagem do contrato. |
| `.../contracts/adapter/out/persistence/JdbcContractRepository` | novo | Grava e lê contrato e parcelas com `JdbcClient`. |
| `.../contracts/adapter/out/calculator/CalculatorFinancingAdapter` | novo | Chama `ScheduleCalculator` e `EffectiveCostCalculator` do calculator. |
| `.../contracts/adapter/in/web/ContractController` | novo | `POST /api/contracts`. |
| `.../contracts/adapter/in/web/RegisterContractRequest`, `ContractResponse` | novo | Corpos do contrato de API. |
| `.../contracts/adapter/in/web/ContractErrorHandler` | novo | Recusas em 400 e 409 com `ProblemDetail`. |
| `backend/src/main/resources/db/migration/V1__create_contract.sql` | novo | Tabelas `contract` e `contract_installment` da seção Dados. |
| `backend/build.gradle.kts` | alterado | Starter de JDBC declarado (vinha só de forma indireta pelo Flyway). |
| `.../contracts/application/port/FinancingRequest`, `Financing` | novo | Pedido e resposta da porta de cálculo. |
| `.../contracts/domain/AmortizationSystem` | novo | Sistema de amortização do contrato, traduzido pelo adaptador para o do calculator. |

## Testes desta task
- [x] Unitário de domínio `CpfTest`, `ChassisNumberTest`, `ContractNumberTest`, `TextFieldTest`: CT-3 a CT-11.
- [x] Unitário de caso de uso `RegisterContractServiceTest`, com repositório e calculador falsos em memória e relógio fixo: CT-1, CT-2, CT-12.
- [x] Integração `JdbcContractRepositoryTest`: CT-13 e CT-14.
- [x] API `ContractApiTest`: CT-15 a CT-21.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6, CT-7, CT-8, CT-9, CT-10, CT-11, CT-12, CT-13, CT-14, CT-15, CT-16, CT-17, CT-18, CT-19, CT-20, CT-21

## Checklist
- [x] Testes de domínio e de caso de uso antes do código.
- [x] `./gradlew build` e `./gradlew pitest` verdes, mutação acima de 80%.
