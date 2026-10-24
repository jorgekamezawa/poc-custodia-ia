---
status: concluída
atualizado: 2026-10-24
descricao: Telas Contratos (busca) e Contrato (consulta) no portal, com os itens no menu e o atalho depois do cadastro.
---
# 02 · front · Lista e consulta de contrato

Spec: [Consulta de contrato](../spec.md) · Depende de: 01

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `frontend/src/app/contracts/contract-api.service.ts` | alterado | Busca e consulta por id, com os tipos novos. |
| `frontend/src/app/contracts/cpf.pipe.ts` | novo | Mostra o CPF como 123.456.789-09. |
| `frontend/src/app/contracts/contract-list-page/` | novo | Busca com o termo no endereço, lista e estado vazio. |
| `frontend/src/app/contracts/contract-detail-page/` | novo | Dados, CET, saldo, parcelas com situação e "Contrato não encontrado.". |
| `frontend/src/app/contracts/register-contract-page/` | alterado | Botão "Ver contrato" depois do cadastro. |
| `frontend/src/app/app.routes.ts` | alterado | Rotas `/contratos` e `/contratos/:id`. |
| `frontend/src/app/shared/shell/` | alterado | Item "Contratos" no menu. |
| `backend/src/test/java/com/bancoexemplo/custody/contracts/adapter/in/web/ContractQueryApiTest` | alterado | Nome do CT-8 sem a regra errada (sobrou da task 01). |

## Testes desta task
- [x] Componente `contract-list-page`: CT-13, CT-14.
- [x] Componente `contract-detail-page`: CT-15, CT-16.

## Cenários cobertos
CT-13, CT-14, CT-15, CT-16

## Checklist
- [x] Testes de componente antes das telas.
- [x] `npm run lint`, `npm run format:check`, `npm test` e `npm run build` sem aviso.
