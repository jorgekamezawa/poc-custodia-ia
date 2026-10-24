---
status: aprovada
atualizado: 2026-10-24
descricao: Calcula multa e juros de mora, aceita o pagamento atrasado com multa e juros de mora e expõe a simulação do valor devido.
---
# 01 · back · Multa e juros de mora

Spec: [Multa e juros de mora](../spec.md) · Depende de: nenhuma

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/payments/domain/LateCharges` | novo | Multa (PAG-05), juros de mora (PAG-06) e total devido numa data. |
| `.../payments/domain/PaymentRules` | alterado | PAG-02 com o valor devido e as duas mensagens; PAG-03 sem a recusa depois do vencimento. |
| `.../payments/domain/Payment` | alterado | Multa e juros de mora cobrados. |
| `.../payments/application/RegisterPaymentService` | alterado | Valida contra o valor devido e guarda multa e juros de mora; simulação com as mesmas recusas. |
| `.../payments/application/PaymentQuote` | novo | Resultado da simulação. |
| `.../payments/adapter/out/persistence/JdbcPaymentRepository` | alterado | Grava `late_fee` e `default_interest`. |
| `.../payments/adapter/in/web/PaymentController` | alterado | `GET /api/contracts/{contractId}/payments/quote`. |
| `backend/src/main/resources/db/migration/V5__add_payment_charges.sql` | novo | Colunas `late_fee` e `default_interest`. |
| `backend/src/test/java/.../payments/application/RegisterPaymentServiceTest` | alterado | O 0007/CT-5 passa a provar o pagamento atrasado aceito com multa e juros de mora, porque a recusa foi revogada. |

## Testes desta task
- [ ] Unitário de domínio `LateChargesTest`: CT-1 a CT-4.
- [ ] Unitário de caso de uso `RegisterPaymentServiceTest`: CT-5 a CT-7.
- [ ] API `PaymentApiTest`: CT-8 a CT-10.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6, CT-7, CT-8, CT-9, CT-10

## Checklist
- [ ] Testes de domínio e de caso de uso antes do código.
- [ ] `./gradlew build` e `./gradlew pitest` verdes, mutação acima de 80%.
