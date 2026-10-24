---
status: aceito
atualizado: 2026-10-24
descricao: O backend é construído com Gradle e scripts em Kotlin DSL, pelo wrapper versionado no repositório.
---
# ADR-0005: Gradle com Kotlin DSL como ferramenta de build

**Decisão:** o backend usa Gradle com scripts em Kotlin DSL, executado pelo wrapper (`gradlew`, script que baixa a versão certa do Gradle), porque é a ferramenta que o mantenedor domina e a POC precisa sair rápido.

## Contexto
O time de referência usa Maven. A POC demonstra o fluxo de desenvolvimento com IA, que independe da ferramenta de build; adaptar para Maven fica para quem adotar.

## Alternativas descartadas
- **Maven:** build declarado em XML (`pom.xml`), usado pelo time de referência. Descartado pelo prazo: o mantenedor é mais rápido em Gradle.
- **Gradle com Groovy DSL:** mesmo Gradle, scripts em Groovy. Descartado porque o Kotlin DSL tem autocompletar e checagem de tipo no editor.

## Consequências
- **Ganhos:** velocidade de quem mantém; wrapper garante a mesma versão local e no CI.
- **Custos:** quem adotar no time de referência traduz o build para Maven.
- **Passa a ser obrigatório:** rodar o build sempre pelo `gradlew`, nunca por um Gradle instalado.
