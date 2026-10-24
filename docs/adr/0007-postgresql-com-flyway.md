---
status: aceito
atualizado: 2026-10-24
descricao: Os dados ficam em PostgreSQL, e toda mudança de esquema entra por migração Flyway em SQL.
---
# ADR-0007: PostgreSQL com Flyway

**Decisão:** os dados ficam em PostgreSQL, e o esquema evolui só por migrações Flyway escritas em SQL e numeradas, porque é um banco relacional gratuito e maduro, e migração versionada deixa o esquema reproduzível em qualquer ambiente.

## Contexto
O domínio é relacional (contrato, parcelas, pagamentos, garantias) e exige consistência transacional. Banco e ferramenta de migração formam uma decisão de duas partes: a ferramenta só existe por causa do banco.

## Alternativas descartadas
- **MySQL:** também relacional e gratuito. Descartado por ter menos recursos de tipos e restrições que o domínio usa (ex.: `numeric` exato, restrições de verificação).
- **Liquibase no lugar do Flyway:** migrações em XML, YAML ou SQL, com rollback declarado. Descartado porque migração em SQL puro é mais fácil de revisar na spec e no PR.

## Consequências
- **Ganhos:** esquema igual em teste, local e CI; a tabela da spec vira SQL direto.
- **Custos:** sem rollback automático; desfazer é uma nova migração.
- **Passa a ser obrigatório:** nenhuma alteração manual de esquema; toda mudança é uma migração nova, nunca edição de uma já aplicada.
