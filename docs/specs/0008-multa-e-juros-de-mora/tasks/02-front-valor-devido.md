---
status: concluída
atualizado: 2026-10-24
descricao: Formulário de pagamento busca o valor devido na data escolhida e mostra multa e juros de mora.
---
# 02 · front · Valor devido no formulário de pagamento

Spec: [Multa e juros de mora](../spec.md) · Depende de: 01

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `frontend/src/app/payments/payment-api.service.ts` | alterado | Simulação do valor devido. |
| `frontend/src/app/contracts/contract-detail-page/contract-detail-page.spec.ts` | alterado | Os testes que abrem o formulário respondem à simulação. |
| `frontend/src/app/payments/register-payment-form/` | alterado | Simulação ao abrir e a cada data; mostra parcela, multa, juros e total; preenche o valor. |

## Testes desta task
- [x] Componente `register-payment-form`: CT-11.

## Cenários cobertos
CT-11

## Checklist
- [x] Testes de componente antes da tela.
- [x] `npm run lint`, `npm run format:check`, `npm test` e `npm run build` sem aviso.
