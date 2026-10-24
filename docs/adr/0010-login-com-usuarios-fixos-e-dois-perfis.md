---
status: aceito
atualizado: 2026-10-24
descricao: O portal tem login com usuários fixos na configuração e dois perfis, operador e supervisor, sem servidor de identidade.
---
# ADR-0010: Login com usuários fixos e dois perfis

**Decisão:** o portal tem login com usuários definidos na configuração da aplicação e dois perfis, operador e supervisor, sem servidor de identidade, porque isso basta para ter regras de permissão sem custo de infraestrutura.

## Contexto
Algumas regras de negócio dependem de quem age (ex.: só o supervisor aprova uma renegociação). A POC não tem usuários reais e não pode usar serviço pago.

## Alternativas descartadas
- **Servidor de identidade (ex.: Keycloak):** cadastro, senha e perfis num serviço à parte. Descartado por ser mais um contêiner e mais configuração sem ganho para a demonstração.
- **Sem login:** todos agem como o mesmo usuário. Descartado porque as regras por perfil não poderiam ser mostradas nem testadas.

## Consequências
- **Ganhos:** permissões testáveis desde a primeira feature; nada além da aplicação para subir.
- **Custos:** não existe cadastro de usuário; trocar por servidor de identidade exige um novo ADR.
- **Passa a ser obrigatório:** as senhas são de demonstração, públicas no repositório, e nunca reaproveitadas fora da POC.
