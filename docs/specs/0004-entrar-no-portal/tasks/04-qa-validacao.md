---
status: concluída
atualizado: 2026-10-24
descricao: Validação final da feature Entrar no portal contra a spec.
---
# 04 · qa · Validação

Spec: [Entrar no portal](../spec.md) · Depende de: 01, 02, 03

## Verificação contra a spec
| Critério / cenário | Como verificado | Resultado | Evidência |
|---|---|---|---|
| CA-1 (CT-1, CT-9, CT-13, CT-15) | API, componente e Playwright | passou | [CI run 40113799075](https://github.com/jorgekamezawa/poc-custodia-ia/actions/runs/40113799075): `backend`, `frontend`, `mutation`, `docs` e `e2e` verdes |
| CA-2 (CT-2, CT-3, CT-10, CT-11, CT-16) | API, componente e Playwright | passou | idem |
| CA-3 (CT-4, CT-12, CT-17) | API, servidor real, componente e Playwright | passou | idem |
| CA-4 (CT-5, CT-14, CT-15) | API, componente e Playwright | passou | idem |
| CA-5 (CT-6, CT-18) | API e Playwright | passou | idem |
| CA-6 (CT-7) | API | passou | sessão com 1.800 segundos |
| CA-7 (CT-8) | API e servidor real | passou | 403 também atrás do servidor |

## Boas práticas
- [x] Convenções do projeto seguidas.
- [x] Sensores verdes (build, lint, testes, PIT): CI run 40113799075; local `./gradlew test --tests '*Authentication*'` 10 de 10 e `npm test` 9 de 9.
- [x] GER-03 a GER-05 publicadas em `regras/gerais.md` do custodia-docs no mesmo ciclo.

## Testes ponta a ponta criados
- `e2e/tests/entrar-no-portal.spec.ts`: CT-15 a CT-18.
- `backend/.../access/adapter/in/AuthenticationServerTest`: CT-8 e a página de erro, por servidor real (reproduz o defeito do 401).

## Relatório
O ponta a ponta achou dois defeitos que os testes de API e de componente não pegavam, e os dois foram corrigidos aqui:
- **Login sem cookie de CSRF:** quem abre `/entrar` direto (ou acabou de sair) não tinha o cookie, e o login saía sem o cabeçalho. O `AuthService` passa a pedir `/api/auth/me` antes de enviar.
- **403 que virava 401:** no servidor real, a recusa de CSRF era repassada à página de erro, protegida, e chegava como 401. O repasse de erro ficou livre; `AuthenticationServerTest` cobre CT-8 e a página de erro chamada direto (que segue exigindo login).

Pendente: o navegador não roda nesta máquina (WSL sem bibliotecas do Chromium); a evidência do ponta a ponta é o CI.
