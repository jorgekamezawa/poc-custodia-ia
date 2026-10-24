---
status: aceito
atualizado: 2026-10-24
descricao: O backend usa Spring Boot, na versão mais recente com suporte gratuito.
---
# ADR-0004: Spring Boot como framework do backend

**Decisão:** o backend usa Spring Boot, na versão mais recente com suporte gratuito, porque é o framework do time de referência e tem integração pronta com Testcontainers, Flyway e Postgres.

## Contexto
A linha 3 do Spring Boot perdeu o suporte gratuito em junho de 2026. A versão vigente fica no arquivo de build.

## Alternativas descartadas
- **Quarkus:** inicia mais rápido e gasta menos memória. Descartado porque o time de referência não usa.
- **Micronaut:** injeção de dependência em tempo de compilação. Descartado pelo mesmo motivo.

## Consequências
- **Ganhos:** padrões e bibliotecas conhecidos pelo time; suporte a Testcontainers pela anotação `@ServiceConnection`.
- **Custos:** a linha 4 mudou nomes de starters em relação à 3 (ex.: `starter-webmvc`), o que exige atenção ao comparar com projetos antigos.
- **Passa a ser obrigatório:** subir de versão antes do fim do suporte gratuito da versão em uso.
