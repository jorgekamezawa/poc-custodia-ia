---
status: concluída
atualizado: 2026-10-24
descricao: Formulário de registro de pagamento na tela Contrato, com a situação Paga e a recarga até o contrato receber o aviso.
---
# 04 · front · Registro de pagamento na consulta

Spec: [Pagamento de parcela em dia](../spec.md) · Depende de: 03

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `frontend/src/app/payments/payment-api.service.ts` | novo | `POST /api/contracts/{contractId}/payments`. |
| `frontend/src/app/payments/register-payment-form/` | novo | Formulário com data e valor, recusa e confirmação. |
| `frontend/src/app/contracts/contract-detail-page/` | alterado | Botão na parcela não paga mais antiga, situação "Paga" e recarga até a parcela aparecer paga. |
| `frontend/src/app/contracts/contract-api.service.ts` | alterado | Situação `PAID` e `paidOn` na parcela. |

## Testes desta task
- [x] Componente `contract-detail-page`: CT-21.
- [x] Componente `register-payment-form`: CT-22, CT-23.

## Cenários cobertos
CT-21, CT-22, CT-23

## Checklist
- [x] Testes de componente antes das telas.
- [x] `npm run lint`, `npm run format:check`, `npm test` e `npm run build` sem aviso.
