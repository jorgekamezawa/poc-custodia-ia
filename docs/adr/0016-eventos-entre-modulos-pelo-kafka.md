---
status: aceito
atualizado: 2026-10-24
descricao: Módulos avisam uns aos outros por eventos publicados no Kafka, porque cada consumidor lê no seu ritmo e pode reler o histórico de eventos, o que serve ao gravame, ao histórico do contrato e aos alertas.
---
# ADR-0016: Eventos entre módulos pelo Kafka

**Decisão:** quando um módulo precisa avisar outro de algo que aconteceu (ex.: pagamento registrado, contrato quitado), ele publica um evento num tópico do Kafka (registro de eventos em ordem, que cada leitor percorre no seu ritmo), porque cada módulo consumidor guarda a própria posição de leitura e pode reler eventos antigos, o que o histórico do contrato e os alertas precisam.

## Contexto
O pagamento precisa marcar a parcela do contrato como paga, e o contrato não pode depender do módulo de pagamentos, que já depende dele: chamada direta nos dois sentidos fecharia um ciclo entre módulos ([ADR-0002](0002-monolito-modular-hexagonal-verificado-por-archunit.md)). Vêm aí mais consumidores dos mesmos fatos: baixa do gravame, histórico do contrato e alertas. A POC também precisa demonstrar troca de mensagens entre módulos (escopo do [mandato 1](../decisoes/0001-mandato-1.md)).

## Alternativas descartadas
- **Eventos em memória do Spring (`ApplicationEvent`):** o módulo publica um objeto e os ouvintes do mesmo processo o recebem na hora. Descartado porque o evento some se a aplicação cair antes de o ouvinte terminar, e ninguém consegue reler o que já passou.
- **RabbitMQ:** fila por consumidor; a mensagem sai da fila quando é confirmada. Descartado porque, sem releitura, montar o histórico do contrato a partir de eventos antigos exigiria guardá-los à parte.

## Consequências
- **Ganhos:** módulos sem dependência direta entre si; consumidor novo lê desde o começo do tópico.
- **Custos:** mais um contêiner no ambiente local; o Spring Boot 4.1 não liga o Kafka do `compose.yaml` sozinho, então o endereço local fica na configuração; o contrato vê o pagamento alguns instantes depois de registrado.
- **Passa a ser obrigatório:** todo evento tem identificador único. O Kafka pode entregar o mesmo evento mais de uma vez; como tratar a repetição fica em aberto para decisão própria.
