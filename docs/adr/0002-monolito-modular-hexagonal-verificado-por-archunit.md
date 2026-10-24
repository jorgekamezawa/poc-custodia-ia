---
status: aceito
atualizado: 2026-10-24
descricao: O backend é um monolito modular hexagonal num único módulo Gradle, com as fronteiras verificadas por ArchUnit.
---
# ADR-0002: Monolito modular hexagonal, verificado por ArchUnit

**Decisão:** o backend é uma aplicação só, dividida em módulos de negócio por pacote, cada um em camadas hexagonais (domínio, aplicação com portas, adaptadores), num único módulo Gradle, com as regras de dependência verificadas por ArchUnit (biblioteca que testa dependências entre pacotes), porque dá fronteiras claras sem o custo de distribuir.

## Contexto
O sistema precisa parecer grande (vários módulos, muitas regras) para a demonstração, mas o que se quer mostrar é o fluxo de desenvolvimento com IA, não a arquitetura distribuída. O domínio não pode depender de framework.

## Alternativas descartadas
- **Um módulo Gradle por camada:** o compilador impede o domínio de importar Spring. Descartado por multiplicar arquivos de build para uma garantia que o ArchUnit já dá no teste.
- **Microserviços:** um serviço por módulo, cada um com banco e pipeline. Descartado por acrescentar comunicação entre serviços e infraestrutura sem mostrar nada novo sobre o fluxo.

## Consequências
- **Ganhos:** um build, um deploy, um banco; fronteiras visíveis na estrutura de pacotes.
- **Custos:** a fronteira só é garantida se o teste de arquitetura cobrir a regra; violação aparece no teste, não na compilação.
- **Passa a ser obrigatório:** toda regra de dependência entre camadas ou módulos vira teste ArchUnit, que roda na suíte comum.
