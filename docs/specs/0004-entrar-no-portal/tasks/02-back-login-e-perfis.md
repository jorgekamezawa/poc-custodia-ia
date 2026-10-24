---
status: aprovada
atualizado: 2026-10-24
descricao: Protege a API com login por sessão, usuários fixos e perfis, e expõe login, sessão atual e saída.
---
# 02 · back · Login e perfis

Spec: [Entrar no portal](../spec.md) · Depende de: 01

## Abordagem técnica
- Spring Security com sessão e CSRF para SPA (`csrf.spa()`), conforme o [ADR-0013](../../../adr/0013-sessao-com-cookie-e-csrf-no-portal.md).
- Usuários lidos da configuração (`custody.security.users`), com senha em hash BCrypt; os de demonstração ficam no `application.properties`.
- Hierarquia de perfis: `SUPERVISOR` inclui `OPERATOR` (GER-04).
- Login por formulário com respostas em JSON (sem redirecionamento), para o portal tratar 200 e 401.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/build.gradle.kts` | alterado | Spring Security e seu suporte a teste. |
| `backend/src/main/java/com/bancoexemplo/custody/access/adapter/in/SecurityConfig` | novo | Cadeia de segurança: tudo exige login (GER-03), login e saída em JSON, CSRF para SPA, hierarquia de perfis. |
| `.../access/adapter/in/ConfiguredUsers` | novo | Lê os usuários da configuração e os entrega ao Spring Security. |
| `.../access/adapter/in/CurrentUserController` | novo | `GET /api/auth/me`. |
| `.../access/adapter/in/CurrentUserResponse` | novo | Corpo de `/api/auth/me` e do login. |
| `.../access/adapter/in/CurrentUsers` | novo | Monta a resposta do usuário logado com todos os perfis que a hierarquia dá. |
| `.../access/adapter/in/CsrfCookieFilter` | novo | Carrega o token de CSRF em toda resposta, para o cookie chegar ao portal antes do login; o `csrf.spa()` sozinho só o grava quando alguém lê o token. |
| `backend/src/test/java/com/bancoexemplo/custody/TestcontainersConfiguration` | alterado | Passa a público, para testes de outros pacotes usarem o PostgreSQL do Testcontainers. |
| `backend/src/main/resources/application.properties` | alterado | Usuários de demonstração e expiração da sessão em 30 minutos (GER-05). |
| `backend/src/test/java/com/bancoexemplo/custody/access/adapter/in/AuthenticationApiTest` | novo | Testes de API dos cenários CT-1 a CT-8. |

## Testes desta task
- [x] API `AuthenticationApiTest`: CT-1 a CT-8, com a aplicação inteira e PostgreSQL do Testcontainers.

## Cenários cobertos
CT-1, CT-2, CT-3, CT-4, CT-5, CT-6, CT-7, CT-8

## Checklist
- [x] Testes escritos antes da configuração.
- [x] `./gradlew build` verde.
