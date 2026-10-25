---
paths:
  - "backend/**"
---

# Convenções do backend

## Estrutura
- Um pacote por módulo de negócio em `com.bancoexemplo.custody.<modulo>` (ex.: `contracts`, `payments`, `collateral`), com:
  - `api/`: a porta de entrada que o módulo oferece aos vizinhos, o único pacote que outro módulo pode importar. Só existe quando outro módulo chama este (módulo que só conversa por evento não tem) e contém só o que o vizinho usa: interfaces e os tipos que elas recebem e devolvem (`record`, `enum`, exceção). A implementação fica em `application`. O ArchUnit barra classe concreta na `api/` (inclusive aninhada) e tipo da `api/` que nenhum outro módulo usa; esta última conta só uso direto no código do vizinho, então um tipo que ele só repassa sem nomear (ex.: exceção que deixa propagar) precisa ser nomeado ou sair da `api/`.
  - Evento entre módulos não mora na `api/` nem no `shared`: quem publica e quem consome mantêm cada um o próprio `record` e o nome do tópico numa constante, e um teste de contrato no módulo consumidor manda o que o adaptador real publica para o ouvinte real (ex.: `PaymentRegisteredContractTest`); só esse teste pode usar a porta e o domínio do publicador, porque teste fica fora do ArchUnit. Pôr o evento na `api/` de quem publica cria ciclo entre módulos quando o consumidor já é chamado por ele.
  - `domain/<agregado>/` (ex.: `contracts/domain/contract/`): a entidade raiz, as entidades dela, os objetos de valor, as exceções e os serviços de domínio do agregado, juntos e sem subpastas por tipo (`entity/`, `vo/`), porque o `package-private` só vale dentro do mesmo pacote. Um pacote por agregado mesmo quando o módulo tem um só. Java puro.
  - `application/`: casos de uso e as portas (`port/`) que eles precisam.
  - `adapter/in/` (REST) e `adapter/out/` (banco, integrações).
- Regras de dependência verificadas por ArchUnit ([ADR-0002](../../docs/adr/0002-monolito-modular-hexagonal-verificado-por-archunit.md)): toda regra nova de fronteira vira teste em `architecture/`.
- **A porta pertence a quem consome:** o módulo que precisa declara a interface na linguagem dele; o adaptador de saída traduz e chama a `api/` do vizinho.
- Nenhum módulo usa repositório, entidade de banco ou tabela de outro.
- Integração externa é porta mais adaptador falso em `adapter/out/` ([ADR-0011](../../docs/adr/0011-integracoes-externas-por-adaptadores-falsos.md)).
- `com.bancoexemplo.custody.shared` é o núcleo que todo módulo pode usar, não um módulo: só entra o que serve a vários módulos e não tem dono (hoje, a base das exceções e o tratador de erro da API). Ele não depende de nenhum módulo (o ArchUnit barra).

## Exceções
- Toda recusa é uma exceção concreta com nome do negócio, num pacote `exception`: `<modulo>/domain/<agregado>/exception/` ou `<modulo>/application/exception/` (ex.: contrato inexistente numa consulta).
- Ela estende uma categoria de `shared/domain/exception/`, que decide a resposta: `BusinessRuleException` (400), `ConflictException` (409) ou `NotFoundException` (404). As categorias e a `DomainException` são abstratas; nunca se lança a categoria direto.
- Um único tratador, `shared/adapter/in/web/ApiErrorHandler`, traduz categoria em `ProblemDetail`. Exceção nova de uma categoria já sai com o status certo; não crie tratador por módulo.
- O adaptador de saída traduz o erro da tecnologia na exceção do domínio (ex.: `DuplicateKeyException` vira `DuplicateContractNumberException`).
- O ArchUnit barra exceção de `domain` ou `application` fora de um pacote `exception` ou fora da `DomainException`.

## Domínio
- Sem anotação nem import de framework.
- **Entidade** é classe, nunca `record`: construtor privado, igualdade e `hashCode` pelo id (a entidade interna do agregado, pela identidade local, ex.: número da parcela), getters `getX()`/`isX()` e nenhum setter. O estado muda por método com nome do negócio (`payInstallment`), que aplica a regra antes de mudar.
  - `create(...)`: nasce a entidade nova; gera o id, define o estado inicial e aplica as regras de criação.
  - `reconstruct(...)`: devolve a entidade como foi gravada, sem gerar id nem validar regra de criação, porque a regra pode ter mudado depois e o dado antigo continua válido. Só existe quando a entidade é lida do banco.
  - O ArchUnit barra construtor público em classe de `domain` e `record` de `domain` com campo `id`.
- **Objeto de valor** é `record` (ou `enum`) e se valida no construtor: um objeto de valor inválido não existe (`new Cpf("12345678900")` recusa com "CPF inválido"). O comportamento dele mora nele.
- **Serviço de domínio**, quando a regra é grande ou envolve mais de um agregado: classe `final` sem estado, com construtor privado, que recebe dados e nunca repositório. Quem consulta o banco é o caso de uso, que entrega o resultado ao domínio.
- **Validação em camadas**, cada uma com a sua parte:
  - adaptador de entrada: o contrato da API (campo presente, tipo), com Bean Validation no request; recusa antes de chamar o caso de uso e nunca cria exceção de domínio (o ArchUnit barra);
  - caso de uso: o que depende de consulta ou contexto (ex.: número de contrato já cadastrado, contrato inexistente);
  - domínio: a regra de negócio e o que ela exige, inclusive campo obrigatório para a regra.
- **Persistência separada do domínio:** a entidade de banco (JPA ou linha do JDBC) é outra classe, no adaptador de saída, que lê com `reconstruct` e grava pelos getters. Nunca anotação de persistência na entidade de domínio.
- Dinheiro: `BigDecimal`, construído de texto, comparado com `compareTo`, arredondado HALF_EVEN só na fronteira ([ADR-0009](../../docs/adr/0009-dinheiro-em-decimal-exato.md)).
- Data de negócio (vencimento, data-com) é `LocalDate`; instante do sistema é `Instant` em UTC.
- Data e hora atuais vêm de um `Clock` injetado, nunca de `now()` sem argumento: o teste fixa o instante.
- O `Clock` do projeto está no fuso de Brasília (`ClockConfig`): `LocalDate.now(clock)` já é o dia de negócio, e `Instant.now(clock)` continua universal. Nunca converta o fuso no código nem mude o fuso padrão da JVM; o `LocalDate.now()` sem argumento já quebra a compilação (Error Prone). O `Clock` de teste é fixado em `ZoneId.of("America/Sao_Paulo")`, como o de produção.

## Banco
- Toda mudança de esquema é migração Flyway nova em `src/main/resources/db/migration`; nunca editar migração aplicada ([ADR-0007](../../docs/adr/0007-postgresql-com-flyway.md)).
- Tabelas, colunas e restrições seguem exatamente a seção Dados da spec.

## Testes
- Teste de domínio e de caso de uso roda sem Spring, sem banco e sem rede.
- Domínio e objeto de valor entram sempre reais, nunca mockados.
- No caso de uso, a porta entra como mock do Mockito (`@ExtendWith(MockitoExtension.class)`), não como classe falsa escrita à mão: um falso com lógica própria faz o teste provar o falso, não a produção.
  - Consulta (`findById`, `paidInstallments`) se prepara com `when(...)` e se prova pelo resultado; para conferir o que foi passado a ela, prepare com o argumento exato, nunca `verify`.
  - `verify` confere só comando: gravar, publicar, ou nada ter sido chamado depois de uma recusa (`never()`, `verifyNoInteractions`).
- Todo método público de adaptador de saída (`adapter/out`) é chamado pelo teste com o nome do adaptador (`JdbcPaymentRepositoryTest` para `JdbcPaymentRepository`), contra o que está atrás dele de verdade: PostgreSQL, Kafka ou o módulo vizinho. De 1 a 3 casos por método: grava e lê de volta igual, restrição ou erro traduzido, filtro e ordenação. Variação de regra de negócio fica no teste unitário. O `ArchitectureTest` barra método sem teste.
- O teste de adaptador chama pela porta (`ContractRepository`), não pela classe concreta, para continuar valendo se a tecnologia de persistência mudar.
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
