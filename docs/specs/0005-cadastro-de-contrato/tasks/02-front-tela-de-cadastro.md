---
status: aprovada
atualizado: 2026-10-24
descricao: Tela Cadastrar contrato no portal, com o resultado do cadastro, e o item no menu da moldura.
---
# 02 · front · Tela de cadastro de contrato

Spec: [Cadastro de contrato](../spec.md) · Depende de: 01

## Abordagem técnica
- Formulário reativo com Angular Material; datas em campo de data do navegador, sem biblioteca de calendário.
- Valores em reais e percentuais exibidos no formato brasileiro, com os dados de localidade `pt-BR` do Angular registrados na aplicação.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `frontend/src/app/app.config.ts` | alterado | Localidade `pt-BR` para moeda, número e data. |
| `frontend/src/app/app.routes.ts` | alterado | Rota `/contratos/novo` dentro da moldura. |
| `frontend/src/app/shared/shell/` | alterado | Menu com "Cadastrar contrato". |
| `frontend/src/app/home/home-page/` | alterado | Início aponta para o menu em vez de "Nenhuma tela disponível ainda". |
| `frontend/src/app/contracts/contract-api.service.ts` | novo | `POST /api/contracts` e os tipos do contrato de API. |
| `frontend/src/app/contracts/register-contract-page/` | novo | Tela de cadastro, recusa no topo e resultado com CET e parcelas. |

## Testes desta task
- [ ] Componente `register-contract-page`: CT-22, CT-23, CT-24.

## Cenários cobertos
CT-22, CT-23, CT-24

## Checklist
- [ ] Testes de componente antes da tela.
- [ ] `npm run lint`, `npm run format:check` e `npm test` verdes.
