---
status: aceito
atualizado: 2026-10-24
descricao: Os adaptadores de banco usam o JdbcClient do Spring com SQL escrito à mão, porque o esquema já é SQL das migrações e o domínio fica livre de mapeamento de banco.
---
# ADR-0015: Acesso a banco com JdbcClient e SQL explícito

**Decisão:** os adaptadores de saída leem e gravam no PostgreSQL com o `JdbcClient` do Spring (cliente que executa SQL escrito à mão e mapeia linhas para objetos), porque o esquema já nasce em SQL nas migrações do [ADR-0007](0007-postgresql-com-flyway.md) e assim o domínio não carrega anotação nem regra de banco.

## Contexto
O cadastro de contrato é a primeira gravação de negócio. O [ADR-0002](0002-monolito-modular-hexagonal-verificado-por-archunit.md) separa domínio e adaptadores, e as convenções de código do projeto proíbem framework no domínio.

## Alternativas descartadas
- **Spring Data JPA (Hibernate):** o framework gera o SQL a partir de classes anotadas e acompanha cada mudança nelas para gravar sozinho. Descartado porque, para manter o domínio sem anotação, cada tabela exigiria uma classe de banco espelhada, e o SQL gerado foge do que a migração mostra.
- **Spring Data JDBC:** repositórios gerados por convenção a partir de classes que espelham as tabelas, sem rastrear mudanças. Descartado porque ainda pede uma classe por tabela no formato que ele entende, e consulta fora da convenção volta a ser SQL à mão.

## Consequências
- **Ganhos:** o SQL que roda é o que está no código, revisável ao lado da migração; nenhum carregamento escondido.
- **Custos:** mapear linha para objeto é código do adaptador, escrito e testado à mão.
- **Passa a ser obrigatório:** todo adaptador de banco tem teste de integração contra PostgreSQL real ([ADR-0008](0008-testes-de-banco-contra-postgres-real.md)).
