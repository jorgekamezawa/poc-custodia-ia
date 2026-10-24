---
status: aceito
atualizado: 2026-10-24
descricao: Integrações externas (SNG, registradora) são simuladas por adaptadores falsos dentro da aplicação, atrás de portas.
---
# ADR-0011: Integrações externas por adaptadores falsos

**Decisão:** cada integração externa é uma porta (interface na camada de aplicação) com um adaptador falso dentro da própria aplicação, de comportamento determinístico (mesma entrada, mesma resposta), porque os sistemas reais não estão disponíveis e o fluxo precisa rodar e ser testado sem rede.

## Contexto
O domínio depende de sistemas de terceiros, como o SNG (sistema de gravames da B3) e registradoras (entidades autorizadas pelo Banco Central a registrar operações de crédito). Não há ambiente de testes público deles. O contrato de cada integração é descrito na spec que a introduz.

## Alternativas descartadas
- **Serviço simulado à parte (ex.: WireMock):** um servidor HTTP que responde como o sistema real. Descartado por ser mais um processo para subir sem ganho para a demonstração.
- **Ambiente de testes real:** exige credenciais e contrato com terceiros. Descartado por não existir acesso.

## Consequências
- **Ganhos:** testes e demonstração rodam sem rede; erros do sistema externo são simulados por dados de entrada conhecidos.
- **Custos:** o adaptador falso não pega diferenças de protocolo, como timeout e formato de erro real.
- **Passa a ser obrigatório:** descrever na spec o comportamento do adaptador falso, inclusive os casos de erro.
