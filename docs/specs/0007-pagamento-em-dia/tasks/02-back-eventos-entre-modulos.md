---
status: aprovada
atualizado: 2026-10-24
descricao: Cria o módulo events, com a tabela de saída, o publicador que leva os eventos ao Kafka e o registro dos eventos já aplicados.
---
# 02 · back · Eventos entre módulos

Spec: [Pagamento de parcela em dia](../spec.md) · Depende de: 01

## Abordagem técnica
- Tabela de saída e consumidor idempotente conforme o [ADR-0017](../../../adr/0017-outbox-e-consumidor-idempotente.md), com `JdbcClient` ([ADR-0015](../../../adr/0015-acesso-a-banco-com-jdbcclient.md)).
- O publicador roda agendado a cada 500 ms, pega até 100 eventos não publicados com `FOR UPDATE SKIP LOCKED` (linhas que outra execução já pegou ficam de fora), envia e espera a confirmação do Kafka antes de marcar.
- Corpo do evento em JSON; a chave da mensagem garante a ordem por contrato.

## Peças
| Caminho | Novo / alterado | Responsabilidade |
|---|---|---|
| `backend/src/main/java/com/bancoexemplo/custody/events/api/EventPublisher` | novo | Grava um evento na tabela de saída, dentro da transação de quem chama. |
| `.../events/api/ProcessedEvents` | novo | Diz se um evento ainda não foi aplicado por um consumidor e o anota, na transação do efeito. |
| `.../events/adapter/out/JdbcEventPublisher` | novo | Implementa `EventPublisher` em `outbox_event`. |
| `.../events/adapter/out/JdbcProcessedEvents` | novo | Implementa `ProcessedEvents` em `processed_event`. |
| `.../events/adapter/out/OutboxRelay` | novo | Publica no Kafka o que ainda não saiu e marca `published_at`. |
| `.../events/adapter/out/SchedulingConfig` | novo | Liga o agendamento do publicador. |
| `backend/src/main/resources/db/migration/V3__create_outbox_and_processed_event.sql` | novo | Tabelas `outbox_event` e `processed_event` da seção Dados. |

## Testes desta task
- [x] Integração `EventsTest`, com PostgreSQL e Kafka reais: CT-14, CT-15, o evento gravado com tópico, chave e corpo, e o registro de evento aplicado por consumidor.

## Cenários cobertos
CT-14, CT-15

## Checklist
- [x] `./gradlew build` e `./gradlew pitest` verdes.
