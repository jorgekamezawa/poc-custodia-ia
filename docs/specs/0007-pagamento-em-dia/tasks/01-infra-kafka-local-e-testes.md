---
status: aprovada
atualizado: 2026-10-24
descricao: Sobe o Kafka no ambiente local junto com o PostgreSQL e nos testes de integração pelo Testcontainers.
---
# 01 · infra · Kafka no ambiente local e nos testes

Spec: [Pagamento de parcela em dia](../spec.md) · Depende de: nenhuma

## Abordagem técnica
- Kafka em modo KRaft (sem ZooKeeper), um nó só, imagem oficial da Apache no `compose.yaml`; porta só em localhost.
- O Spring Boot 4.1 não liga o Kafka do Compose sozinho, então o endereço local fica na configuração; nos testes, o Testcontainers liga sozinho (`@ServiceConnection`).
- Starter do Kafka do Spring Boot; o consumo e a publicação ficam para as tasks seguintes.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `compose.yaml` | alterado | Serviço `kafka`, porta 19092 em localhost, volume nomeado. |
| `backend/build.gradle.kts` | alterado | Starter do Kafka e o Testcontainers de Kafka nos testes. |
| `backend/src/main/resources/application.properties` | alterado | Endereço do Kafka local, sobrescrito por variável de ambiente. |
| `backend/src/test/java/com/bancoexemplo/custody/TestcontainersConfiguration` | alterado | Contêiner de Kafka ao lado do PostgreSQL. |
| `docs/runbooks/0001-rodar-localmente.md` | alterado | Porta 19092 e o volume do Kafka. |

## Testes desta task
- [x] `./gradlew build` sobe o contexto com PostgreSQL e Kafka do Testcontainers.
- [x] `./gradlew bootRun` sobe PostgreSQL e Kafka do Compose e a aplicação conecta.

## Cenários cobertos
Nenhum: a task prepara o Kafka dos cenários das tasks 02 e 03.

## Checklist
- [x] Ambiente local sobe e desce sem apagar volume.
- [x] CI verde.
