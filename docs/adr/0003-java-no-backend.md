---
status: aceito
atualizado: 2026-10-24
descricao: O backend é escrito em Java, na versão LTS mais recente que o time de referência usa.
---
# ADR-0003: Java no backend

**Decisão:** o backend é escrito em Java, na versão LTS (de suporte longo) mais recente que o time de referência usa, porque a demonstração precisa ser diretamente aplicável à stack desse time.

## Contexto
A POC será apresentada a um time que desenvolve em Java e usa até a versão 21. Uma versão mais nova, mesmo suportada pelo framework, criaria a dúvida "isto funciona na nossa versão?". O número vigente fica no arquivo de build.

## Alternativas descartadas
- **Kotlin:** roda na mesma JVM, mais conciso. Descartado porque o time de referência não usa, e a demonstração deixaria de ser diretamente aplicável.
- **Java na LTS mais recente (25):** recursos novos de linguagem. Descartado enquanto o time de referência estiver no 21.

## Consequências
- **Ganhos:** código e convenções reaproveitáveis pelo time sem tradução.
- **Custos:** recursos de linguagem posteriores ao 21 ficam de fora.
- **Passa a ser obrigatório:** subir a versão quando o time de referência subir, alterando o arquivo de build, sem novo ADR.
