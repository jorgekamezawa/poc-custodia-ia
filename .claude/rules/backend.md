---
paths:
  - "backend/**"
---

# Convenções do backend

## Estrutura
- Um pacote por módulo de negócio em `com.bancoexemplo.custody.<modulo>` (ex.: `contracts`, `payments`, `collateral`), com:
  - `api/`: o que outro módulo pode importar (interfaces e tipos de valor). Nada além disso é visível fora do módulo.
  - `domain/`: entidades, objetos de valor e regras. Java puro.
  - `application/`: casos de uso e as portas (`port/`) que eles precisam.
  - `adapter/in/` (REST) e `adapter/out/` (banco, integrações).
- Regras de dependência verificadas por ArchUnit ([ADR-0002](../../docs/adr/0002-monolito-modular-hexagonal-verificado-por-archunit.md)): toda regra nova de fronteira vira teste em `architecture/`.
- **A porta pertence a quem consome:** o módulo que precisa declara a interface na linguagem dele; o adaptador de saída traduz e chama a `api/` do vizinho.
- Nenhum módulo usa repositório, entidade de banco ou tabela de outro.
- Integração externa é porta mais adaptador falso em `adapter/out/` ([ADR-0011](../../docs/adr/0011-integracoes-externas-por-adaptadores-falsos.md)).

## Domínio
- Sem anotação nem import de framework.
- Dinheiro: `BigDecimal`, construído de texto, comparado com `compareTo`, arredondado HALF_EVEN só na fronteira ([ADR-0009](../../docs/adr/0009-dinheiro-em-decimal-exato.md)).
- Data de negócio (vencimento, data-com) é `LocalDate`; instante do sistema é `Instant` em UTC.
- Data e hora atuais vêm de um `Clock` injetado, nunca de `now()` sem argumento: o teste fixa o instante.

## Banco
- Toda mudança de esquema é migração Flyway nova em `src/main/resources/db/migration`; nunca editar migração aplicada ([ADR-0007](../../docs/adr/0007-postgresql-com-flyway.md)).
- Tabelas, colunas e restrições seguem exatamente a seção Dados da spec.

## Testes
- Teste de domínio e de caso de uso roda sem Spring, sem banco e sem rede. Dependência externa entra como implementação falsa em memória escrita no projeto, não biblioteca de mock: o teste verifica comportamento, não chamada.
- Teste que toca banco usa PostgreSQL real via Testcontainers ([ADR-0008](../../docs/adr/0008-testes-de-banco-contra-postgres-real.md)).
- Teste que verifica uma regra de negócio tem `@DisplayName` começando pelo identificador da regra, o número da spec e o cenário (ex.: `"GAR-03 0007/CT-4: baixa recusada sem quitação"`).
- Teste parametrizado usa `@ParameterizedTest(name = "{displayName} [{arguments}]")`: o nome padrão do JUnit (`[1] term = 0`) tira o cenário do relatório.

## Comentário e idioma
- Código, identificadores e comentários em inglês; mensagem que o usuário lê em português.
- Comentário só para o porquê não óbvio, em uma linha. Nunca citar número de ADR nem de regra em comentário: a referência apodrece quando o documento muda.
- Sem código comentado, histórico em comentário ou TODO sem issue.

## Ferramenta
- O formato é do formatador (Palantir: 4 espaços, 120 colunas), não do editor. Rode `spotlessApply` antes de mostrar código; o commit é barrado se o formato estiver fora.
- Aviso de compilação quebra o build. Corrija a causa; supressão só com o motivo numa linha.
- Teste de mutação abaixo de 80% em `domain` e `application` falha o CI: escreva o teste que mata o mutante, não afrouxe o limite.

## Configuração
- Endereços e senhas vêm de variável de ambiente; o perfil `local` traz valores fixos para rodar sem preparar nada.
