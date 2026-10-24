---
status: aceito
atualizado: 2026-10-24
descricao: O portal mantém o login numa sessão do servidor, identificada por cookie, com proteção contra CSRF, porque portal e API saem da mesma origem e isso dispensa guardar token no navegador.
---
# ADR-0013: Sessão com cookie e proteção contra CSRF no portal

**Decisão:** depois do login, a aplicação guarda a sessão na própria memória e o navegador a identifica por um cookie que o JavaScript não lê, com proteção contra CSRF (pedido forjado por outro site usando o cookie do usuário), porque portal e API saem da mesma origem (mesmo protocolo, domínio e porta), a POC roda uma instância só e assim nenhum token de acesso fica exposto ao código da página.

## Contexto
O [ADR-0010](0010-login-com-usuarios-fixos-e-dois-perfis.md) definiu usuários fixos e dois perfis, mas não como o portal prova, a cada chamada à API, quem está logado. O portal Angular chama a API pela mesma origem (em desenvolvimento, por um proxy, que repassa as chamadas do servidor do Angular para a API).

## Alternativas descartadas
- **Token JWT (token assinado que carrega os dados do usuário) emitido pela própria API:** o login devolve um token assinado que o portal guarda e manda em cada chamada. Descartado porque o token fica ao alcance de qualquer script da página e sair do sistema exige lista de tokens revogados.
- **Usuário e senha em toda chamada (HTTP Basic):** o portal guarda a senha e a envia a cada pedido. Descartado porque a senha fica na memória da página o tempo todo e não existe "sair" de verdade.

## Consequências
- **Ganhos:** sair invalida a sessão no servidor na hora; o cookie de sessão não é lido pela página; o Angular já envia o cabeçalho de CSRF sozinho.
- **Custos:** reiniciar o backend desloga todos; mais de uma instância exigiria sessão compartilhada.
- **Passa a ser obrigatório:** portal e API na mesma origem; toda chamada que altera dado leva o cabeçalho de CSRF.
