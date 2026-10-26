---
status: aceito
atualizado: 2026-10-25
descricao: Os adaptadores de banco dos agregados passam a usar Spring Data JPA, com entidade de persistência separada do domínio, porque o time lê e mantém esse código com mais facilidade que o SQL mapeado à mão.
---
# ADR-0018: Acesso a banco dos agregados com Spring Data JPA

**Decisão:** os adaptadores de saída que gravam e leem os agregados (contrato, pagamento) usam Spring Data JPA (repositórios gerados sobre o Hibernate), com uma entidade de persistência própria no adaptador, separada da entidade de domínio, porque o time que mantém o sistema lê e escreve JPA com mais facilidade do que o mapeamento de linha para objeto escrito à mão. As tabelas da infraestrutura de eventos (`outbox_event`, `processed_event`) continuam com `JdbcClient`.

## Contexto
O [ADR-0015](0015-acesso-a-banco-com-jdbcclient.md) escolheu `JdbcClient` com SQL à mão. Com o domínio já separado em fábricas (`create`, `reconstruct`), a maior parte do código de persistência virou mapeamento de colunas para objetos, repetido em cada consulta nova. O esquema segue nascendo nas migrações Flyway ([ADR-0007](0007-postgresql-com-flyway.md)). As tabelas de eventos dependem de SQL do PostgreSQL (`FOR UPDATE SKIP LOCKED`, `ON CONFLICT DO NOTHING`), que o JPA só executaria como consulta nativa, sem ganho.

## Alternativas descartadas
- **Manter o `JdbcClient` em tudo:** o SQL que roda fica visível ao lado da migração. Descartado porque o mapeamento escrito à mão é o trecho menos legível para quem mantém, e cada consulta nova o repete.
- **Anotar a própria entidade de domínio com JPA:** elimina o mapeador. Descartado porque põe anotação de persistência no domínio, que fica sem framework ([ADR-0002](0002-monolito-modular-hexagonal-verificado-por-archunit.md)), e amarra a regra de negócio ao ciclo de vida do Hibernate (objetos substitutos gerados por ele, dados lidos do banco só quando acessados).
- **Spring Data JDBC:** repositórios gerados, sem rastrear mudanças. Descartado porque pede classes no formato que ele entende, como o JPA, e é menos conhecido pelo time.

## Consequências
- **Ganhos:** repositórios e consultas derivadas do nome do método, sem SQL à mão; a aplicação não sobe se a entidade divergir da tabela; código mais próximo do que o time conhece.
- **Custos:** uma dependência nova; um mapeador entre entidade de persistência e domínio; os cuidados do JPA: a escrita só chega ao banco no `flush` (quando o Hibernate descarrega as mudanças) e uma lista pode disparar uma consulta extra por item (o problema N+1).
- **Passa a ser obrigatório:** o Flyway continua dono do esquema, e todo adaptador de banco segue com teste contra PostgreSQL real ([ADR-0008](0008-testes-de-banco-contra-postgres-real.md)). As convenções de uso do JPA ficam nas regras de código do backend.

## Substitui / Substituído por (só quando houver)
Substitui o [ADR-0015](0015-acesso-a-banco-com-jdbcclient.md).
