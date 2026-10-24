---
status: concluída
atualizado: 2026-10-24
descricao: Tela de entrada, moldura do portal com usuário e saída, e bloqueio das rotas sem login.
---
# 03 · front · Tela de entrada e moldura do portal

Spec: [Entrar no portal](../spec.md) · Depende de: 02

## Abordagem técnica
- Angular Material ([ADR-0014](../../../adr/0014-componentes-do-portal-com-angular-material.md)) para campos, botões e barra superior.
- `HttpClient` com o suporte a XSRF do Angular, que lê o cookie `XSRF-TOKEN` e manda o cabeçalho `X-XSRF-TOKEN`.
- Proxy do servidor de desenvolvimento manda `/api` ao backend, para portal e API terem a mesma origem.
- Guarda de rota pergunta `/api/auth/me` e leva a `/entrar` quando não há sessão; resposta 401 em qualquer chamada faz o mesmo.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `frontend/package.json` | alterado | Angular Material e CDK. |
| `frontend/proxy.conf.json` e `frontend/angular.json` | novo / alterado | Proxy de `/api` para `http://localhost:8080` no `ng serve`. |
| `frontend/src/app/app.config.ts` | alterado | `HttpClient` com XSRF e o interceptador de 401. |
| `frontend/src/app/app.routes.ts` | alterado | `/entrar` livre; `/` dentro da moldura, protegido pela guarda. |
| `frontend/src/app/app.ts`, `app.spec.ts` | alterado | Raiz só com a saída de rotas, sem a página de exemplo do Angular; `app.html` e `app.scss` saem. |
| `frontend/src/styles.scss` | alterado | Tema do Angular Material com a fonte do sistema. |
| `frontend/src/app/access/auth.service.ts` | novo | Login, sessão atual e saída; guarda o usuário logado num signal. |
| `frontend/src/app/access/auth.guard.ts` | novo | Libera a rota só com sessão. |
| `frontend/src/app/access/unauthorized.interceptor.ts` | novo | 401 leva a `/entrar`. |
| `frontend/src/app/access/login-page/` | novo | Tela Entrar. |
| `frontend/src/app/shared/shell/` | novo | Moldura: barra superior com nome, perfil e Sair. |
| `frontend/src/app/home/home-page/` | novo | Página inicial. |

## Testes desta task
- [x] Componente `login-page`: CT-9, CT-10, CT-11.
- [x] Guarda: CT-12.
- [x] Componente `shell`: CT-13, CT-14.

## Cenários cobertos
CT-9, CT-10, CT-11, CT-12, CT-13, CT-14

## Checklist
- [x] Testes de componente antes das telas.
- [x] `npm run lint`, `npm run format:check` e `npm test` verdes.
