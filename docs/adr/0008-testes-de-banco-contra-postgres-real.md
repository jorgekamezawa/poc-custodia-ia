---
status: aceito
atualizado: 2026-10-24
descricao: Testes que tocam banco rodam contra PostgreSQL real em contêiner, via Testcontainers.
---
# ADR-0008: Testes de banco contra PostgreSQL real

**Decisão:** todo teste que toca banco roda contra PostgreSQL real em contêiner, criado pelo Testcontainers (biblioteca que sobe contêineres Docker durante o teste), na versão fixada no código de teste, porque só o banco real mostra o comportamento que a produção teria.

## Contexto
Migrações, restrições e consultas usam recursos do PostgreSQL. O CI do GitHub já tem Docker disponível.

## Alternativas descartadas
- **H2 em memória:** banco Java que imita outros bancos. Descartado porque diverge do PostgreSQL em tipos, funções e restrições, e o teste passa onde a produção falharia.
- **Mock do repositório:** substitui o acesso a banco por um objeto falso. Descartado para testes de persistência porque não verifica SQL, migração nem restrição.

## Consequências
- **Ganhos:** o teste verifica migração, SQL e restrições de verdade.
- **Custos:** precisa de Docker para rodar os testes; a suíte fica alguns segundos mais lenta.
- **Passa a ser obrigatório:** fixar a versão da imagem do PostgreSQL (nunca `latest`) e não usar banco em memória.
