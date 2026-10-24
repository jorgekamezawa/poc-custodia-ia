---
status: aprovada
atualizado: 2026-10-24
descricao: Cria o módulo payments com o registro do pagamento em dia e faz o contrato marcar a parcela paga ao receber o evento.
---
# 03 · back · Registro de pagamento

Spec: [Pagamento de parcela em dia](../spec.md) · Depende de: 02

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/contracts/api/ContractInstallments`, `ContractSchedule` | novo | O que outro módulo pode ler do contrato: liberação e parcelas com vencimento e valor. Quais estão pagas o módulo de pagamentos sabe pelos próprios registros. |
| `.../contracts/application/ContractInstallmentsService` | novo | Implementa `ContractInstallments`. |
| `.../contracts/application/MarkInstallmentPaidService` | novo | Marca a parcela paga. |
| `.../contracts/adapter/in/messaging/PaymentRegisteredListener` | novo | Consome `custody.payment-registered` e aplica uma vez por evento (CA-7), na mesma transação do registro do evento aplicado. |
| `.../contracts/domain/ContractInstallment`, `Contract`, `InstallmentSituation` | alterado | Data do pagamento, situação `PAID` (CON-05) e saldo depois da última paga (CON-06). |
| `.../contracts/adapter/out/persistence/JdbcContractRepository` | alterado | Lê e grava `paid_on`. |
| `.../payments/domain/Payment`, `PaymentRules`, `InvalidPaymentException`, `InstallmentAlreadyPaidException`, `ContractNotFoundForPaymentException` | novo | Pagamento, a PAG-01 a PAG-04 e a recusa de contrato que não existe (CA-8). |
| `.../contracts/application/ContractQueryService` | alterado | Usa a situação da parcela com `PAID`. |
| `.../payments/application/RegisterPaymentService`, `RegisterPaymentCommand` | novo | Valida, guarda e publica o evento na mesma transação. |
| `.../payments/application/port/PaymentRepository`, `ContractToPay`, `PaymentEvents` | novo | Portas de gravação, de leitura do contrato e de publicação do evento. |
| `.../payments/adapter/out/persistence/JdbcPaymentRepository` | novo | Tabela `payment`. |
| `.../payments/adapter/out/contracts/ContractsAdapter` | novo | Lê o contrato pela `api/` de contracts. |
| `.../payments/adapter/out/events/PaymentEventsAdapter` | novo | Publica pelo `EventPublisher` do módulo events. |
| `.../payments/adapter/in/web/PaymentController`, `RegisterPaymentRequest`, `PaymentErrorHandler` | novo | `POST /api/contracts/{contractId}/payments` e os erros. |
| `backend/src/main/resources/db/migration/V4__create_payment_and_paid_on.sql` | novo | Tabela `payment` e a coluna `paid_on`. |
| `backend/src/main/resources/application.properties` | alterado | Consumidor novo lê o tópico desde o começo. |
| `backend/src/test/java/.../payments/application/PaymentFakes` | novo | Falsos em memória das portas de pagamento. |
| `backend/src/test/java/.../contracts/application/InMemoryContractRepository`, `FixedFinancingCalculator` | alterado | Marca parcela paga; primeira parcela com os valores reais do Price. |

## Testes desta task
- [x] Unitário de caso de uso `RegisterPaymentServiceTest`, com falsos em memória e relógio fixo: CT-1 a CT-10.
- [x] Unitário de caso de uso `MarkInstallmentPaidServiceTest`: CT-11.
- [x] Unitário de domínio `InstallmentPaidTest`: CT-13, situação e saldo com parcela paga.
- [x] Unitário de caso de uso `ContractInstallmentsServiceTest`: o que a `api/` expõe.
- [x] Integração `PaymentRegisteredListenerTest`: CT-12.
- [x] API `PaymentApiTest`, com PostgreSQL e Kafka reais: CT-16 a CT-20.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6, CT-7, CT-8, CT-9, CT-10, CT-11, CT-12, CT-13, CT-16, CT-17, CT-18, CT-19, CT-20

## Checklist
- [x] Testes de domínio e de caso de uso antes do código.
- [x] `./gradlew build` e `./gradlew pitest` verdes, mutação acima de 80%.
