---
paths:
  - "frontend/**"
---

# Convenções do frontend

## Estrutura
- Uma pasta por módulo de negócio em `src/app/<modulo>/`, com páginas, componentes e um serviço de API do módulo. O que dois módulos usam fica em `src/app/shared/`.
- Componentes standalone, injeção com `inject()`, controle de fluxo novo (`@if`, `@for`).
- A aplicação é zoneless: estado de tela em signals, para a tela atualizar.

## Dados e estado
- Chamada HTTP só no serviço do módulo, nunca no componente.
- Filtros e seleções da tela ficam na URL (query params): link compartilhável e botão voltar correto.
- Nenhuma regra de negócio no front: a validação daqui é experiência de uso; a que vale é a do backend.

## Testes
- Todo componente com comportamento tem teste de componente (Vitest + TestBed), verificando o que o usuário vê e faz, não detalhes internos.
- Teste ponta a ponta (Playwright) nasce na task de QA, não nas tasks de front.

## Idioma
- Identificadores e comentários em inglês; texto da tela em português.
