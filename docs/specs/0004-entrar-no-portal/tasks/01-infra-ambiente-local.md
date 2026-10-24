---
status: concluída
atualizado: 2026-10-24
descricao: Sobe o banco local por Docker Compose junto com o backend e faz o ponta a ponta subir backend e portal.
---
# 01 · infra · Ambiente local e ponta a ponta com backend

Spec: [Entrar no portal](../spec.md) · Depende de: nenhuma

## Abordagem técnica
- `compose.yaml` na raiz com o PostgreSQL; o suporte a Docker Compose do Spring Boot o sobe junto com `bootRun` e liga a aplicação a ele, sem endereço nem senha a configurar.
- O suporte entra só no desenvolvimento (`developmentOnly`): não vai no jar nem nos testes, que seguem com Testcontainers.
- O Playwright sobe backend e portal antes dos testes e reaproveita os que já estiverem de pé fora do CI.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `compose.yaml` | novo | PostgreSQL local, com volume nomeado e porta só em localhost. |
| `backend/build.gradle.kts` | alterado | Dependência de desenvolvimento do suporte a Docker Compose. |
| `backend/src/main/resources/application.properties` | alterado | Aponta o suporte a Docker Compose para o `compose.yaml` da raiz. |
| `e2e/playwright.config.ts` | alterado | Sobe o backend (`bootRun`) e o portal, esperando os dois responderem. |
| `.github/workflows/ci.yml` | alterado | Job `e2e` com o Java do backend para o `bootRun`. |
| `docs/runbooks/0001-rodar-localmente.md` | novo | Como subir, usar e derrubar o ambiente local sem apagar o volume. |

## Testes desta task
- [x] `./gradlew bootRun` sobe o PostgreSQL e a aplicação responde em `http://localhost:8080`.
- [x] `cd e2e && npm test` sobe backend e portal e o teste de fumaça que já existe passa no CI (aqui o Chromium não abre, ver registro do mandato).

## Cenários cobertos
Nenhum: a task prepara o ambiente dos cenários de ponta a ponta da task 04.

## Checklist
- [x] Ambiente local sobe e desce sem apagar volume.
- [x] CI verde com o job `e2e` subindo o backend.
