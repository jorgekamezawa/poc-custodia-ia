---
status: entregue
atualizado: 2026-10-24
descricao: Login no portal com os perfis operador e supervisor, saída, expiração da sessão e bloqueio de toda tela e API sem login.
---
# Entrar no portal

História: [#47 Entrar no portal como operador ou supervisor](https://github.com/jorgekamezawa/poc-custodia-ia/issues/47)

## Problema
Toda tela da custódia mexe com contrato de cliente, e algumas regras dependem do perfil de quem age ([ADR-0010](../../adr/0010-login-com-usuarios-fixos-e-dois-perfis.md)). Sem login, não há como saber quem fez o quê nem aplicar regra de perfil.

## Fora de escopo
- Cadastro, troca ou recuperação de senha: os usuários são fixos na configuração.
- Regras de perfil de cada operação (ex.: o que só o supervisor aprova): entram na spec de cada operação.
- Menu com as telas dos módulos: cada tela entra no menu na sua própria história.

## Regras
| Mudança | Regra | Enunciado |
|---|---|---|
| adicionada | GER-03 | Sem login, nenhuma tela do portal abre e nenhuma chamada à API é atendida. |
| adicionada | GER-04 | O supervisor pode fazer tudo o que o operador faz, além do que for só dele. |
| adicionada | GER-05 | A sessão termina depois de 30 minutos sem uso; a próxima ação pede login de novo. |

## Critérios de aceite
- **CA-1.** Dado o usuário `operador` com a senha certa, quando entra no portal, então vê o Início com o nome e o perfil "Operador".
- **CA-2.** Dado usuário ou senha errados, quando tenta entrar, então continua na tela de entrada com "Usuário ou senha inválidos", sem dizer qual dos dois errou.
- **CA-3.** Dado alguém sem login, quando abre qualquer endereço do portal, então é levado à tela de entrada; e a API responde 401 a qualquer chamada.
- **CA-4.** Dado um usuário logado, quando clica em Sair, então volta à tela de entrada e a sessão deixa de valer na API.
- **CA-5.** Dado o usuário `supervisor`, quando entra, então vê o perfil "Supervisor" e pode fazer tudo o que o operador faz (GER-04).
- **CA-6.** Dada uma sessão aberta, quando passam 30 minutos sem uso, então ela deixa de valer.
- **CA-7.** Dado um pedido que altera dado sem o cabeçalho de proteção contra CSRF (pedido forjado por outro site usando o cookie do usuário; [ADR-0013](../../adr/0013-sessao-com-cookie-e-csrf-no-portal.md)), quando chega à API, então é recusado.

## Telas
Sem esboço nesta história.
- **Entrar** (`/entrar`): campos Usuário e Senha, obrigatórios, e botão Entrar. Campo vazio mostra "Informe o usuário" ou "Informe a senha" e não envia. Durante o envio, o botão fica desabilitado.
- **Moldura do portal:** barra superior com "Custódia · Banco Exemplo", o nome e o perfil do usuário e o botão Sair.
- **Início** (`/`): "Bem-vindo, <nome>." e, enquanto não houver tela de módulo, "Nenhuma tela disponível ainda."

## Contratos de API
### POST /api/auth/login
Exposto pelo backend, chamado pelo portal. Corpo `application/x-www-form-urlencoded`; cabeçalho `X-XSRF-TOKEN` com o valor do cookie `XSRF-TOKEN`.

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| username | texto | sim | | operador |
| password | texto | sim | | operador-demo |

| Código | Quando |
|---|---|
| 200 | Login feito; corpo igual ao de `GET /api/auth/me` e cookie de sessão |
| 401 | Usuário ou senha inválidos; corpo `{"message": "Usuário ou senha inválidos"}` |
| 403 | Sem o cabeçalho de CSRF |

### GET /api/auth/me
Devolve quem está logado. Também entrega o cookie `XSRF-TOKEN` que o portal usa no login.

| Campo | Tipo | Obrigatório | Restrições | Exemplo |
|---|---|---|---|---|
| username | texto | sim | | supervisor |
| displayName | texto | sim | | Supervisor |
| roles | lista de texto | sim | `OPERATOR`, `SUPERVISOR` | ["OPERATOR", "SUPERVISOR"] |

| Código | Quando |
|---|---|
| 200 | Há sessão válida |
| 401 | Sem sessão ou sessão expirada |

### POST /api/auth/logout
Encerra a sessão. Cabeçalho `X-XSRF-TOKEN`.

| Código | Quando |
|---|---|
| 204 | Sessão encerrada |
| 403 | Sem o cabeçalho de CSRF |

## Cenários de teste
| ID | Critério | Camada | Entrada | Resultado esperado |
|---|---|---|---|---|
| CT-1 | CA-1 | integração | POST /api/auth/login com operador / operador-demo | 200, `roles` = ["OPERATOR"], `displayName` = "Operador" |
| CT-2 | CA-2 | integração | POST /api/auth/login com operador / senha-errada | 401 com "Usuário ou senha inválidos" |
| CT-3 | CA-2 | integração | POST /api/auth/login com usuario-inexistente / qualquer | 401 com a mesma mensagem do CT-2 |
| CT-4 | CA-3 | integração | GET /api/auth/me sem sessão | 401 |
| CT-5 | CA-4 | integração | Login, POST /api/auth/logout, depois GET /api/auth/me com o mesmo cookie | 204 e depois 401 |
| CT-6 | CA-5 | integração | Login com supervisor / supervisor-demo | 200, `roles` = ["OPERATOR", "SUPERVISOR"], `displayName` = "Supervisor" |
| CT-7 | CA-6 | integração | Login com operador | Sessão criada com expiração de 1.800 segundos sem uso |
| CT-8 | CA-7 | integração | POST /api/auth/login sem `X-XSRF-TOKEN` | 403 |
| CT-9 | CA-1 | componente | Tela Entrar com login aceito | Navega para `/` |
| CT-10 | CA-2 | componente | Tela Entrar com login recusado | Mostra "Usuário ou senha inválidos" e continua em `/entrar` |
| CT-11 | CA-2 | componente | Tela Entrar com Usuário vazio e depois Senha vazia | Mostra "Informe o usuário" e "Informe a senha"; nada é enviado |
| CT-12 | CA-3 | componente | Rota `/` sem usuário logado | Redireciona para `/entrar` |
| CT-13 | CA-1 | componente | Moldura com operador logado | Mostra "Operador" e o botão Sair |
| CT-14 | CA-4 | componente | Clique em Sair | Chama o logout e navega para `/entrar` |
| CT-15 | CA-1, CA-4 | ponta a ponta | Entrar como operador, ver o Início, sair | Início com "Bem-vindo, Operador."; depois de sair, `/entrar` |
| CT-16 | CA-2 | ponta a ponta | Entrar com senha errada | "Usuário ou senha inválidos" na tela |
| CT-17 | CA-3 | ponta a ponta | Abrir `/` sem login | Tela de entrada |
| CT-18 | CA-5 | ponta a ponta | Entrar como supervisor | Barra mostra "Supervisor" |
