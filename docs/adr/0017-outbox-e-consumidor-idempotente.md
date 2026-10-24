---
status: aceito
atualizado: 2026-10-24
descricao: Eventos saem por uma tabela de saída gravada na mesma transação do fato e são aplicados uma vez só por consumidor, porque publicar direto no Kafka pode perder ou inventar evento quando o banco ou o broker falha no meio.
---
# ADR-0017: Tabela de saída (outbox) e consumidor idempotente

**Decisão:** o módulo grava o evento numa tabela de saída na mesma transação em que grava o fato, e um processo separado publica no Kafka o que ainda não saiu; cada consumidor anota os eventos que já aplicou e ignora repetições, porque assim o evento sai se, e só se, o fato foi gravado, e reenvio não duplica efeito.

## Contexto
Com eventos pelo Kafka ([ADR-0016](0016-eventos-entre-modulos-pelo-kafka.md)), gravar no banco e publicar no broker (o servidor do Kafka que guarda e entrega as mensagens) são duas operações que podem falhar separadas. O Kafka entrega cada mensagem pelo menos uma vez, então o mesmo evento pode chegar duas vezes ao consumidor.

## Alternativas descartadas
- **Publicar no Kafka dentro do caso de uso:** grava no banco e envia ao broker no mesmo método. Descartado porque, se o envio falha depois do commit, o fato existe sem evento; se o commit falha depois do envio, o evento conta um fato que não aconteceu.
- **Transação distribuída entre banco e Kafka:** um coordenador confirma as duas coisas juntas. Descartado porque o Kafka não participa desse tipo de transação com o PostgreSQL, e o custo de operação não cabe na POC.

## Consequências
- **Ganhos:** nenhum evento perdido nem inventado; reprocessar um tópico não duplica pagamento, baixa ou histórico.
- **Custos:** duas tabelas a mais (saída e eventos aplicados) e um publicador que roda a cada fração de segundo; o evento chega com um pequeno atraso.
- **Passa a ser obrigatório:** publicar evento só pela tabela de saída, e consumidor registrar o evento aplicado na mesma transação do efeito.
