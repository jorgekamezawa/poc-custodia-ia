---
status: ativo
atualizado: 2026-10-24
descricao: Objetivo, restrições, decisões, domínio, fluxo e fases da POC de desenvolvimento orientado a IA.
---
# POC: desenvolvimento orientado a IA (custódia de financiamento fictícia)

Documento de entrada para o agente. Resume as decisões já tomadas. Não é o `CLAUDE.md`: fica em `docs/` e só é lido quando necessário.

## 1. Objetivo

Demonstrar que, com padrões bem definidos por engenheiros, um time consegue desenvolver de forma muito orientada a IA dentro de um sistema já grande, sem começar do zero.

A demonstração mostra:
1. Projeto robusto, bem documentado e fácil de entender, com o contexto da área agregado num só lugar.
2. A IA buscando só a informação necessária, no momento certo, para refinar histórias e apoiar decisões. Ela traz contexto, opções e argumentos; a decisão é sempre humana.
3. Uma feature nova do início ao fim: história, refinamento, esboço de tela, spec, implementação, testes e PR.
4. Testes de back, front e ponta a ponta automatizados, com sensores antes da revisão humana.

Mensagem para cada público:
- **Negócio:** com o fluxo bem feito, uma ideia vira tela e entrega rápido e próxima do que se pediu.
- **Desenvolvimento:** isso só funciona porque engenheiros desenharam padrões, templates, fluxo e sensores. Exige investimento inicial (curva em J).

Custo e consumo de tokens são secundários nesta POC.

## 2. Restrições

- Domínio fictício e simplificado. Nenhum código, dado, documento, credencial ou nome de empresa real.
- Repositórios públicos no GitHub pessoal (exigido pelo GitHub Pages no plano Free).
- Backend: Java 21, Spring Boot 4.1, Gradle (Kotlin DSL), PostgreSQL, Testcontainers, JUnit 5, Mockito.
- Frontend: Angular.
- Arquitetura hexagonal no backend (domínio sem dependência de framework).
- Nenhum serviço pago além do plano Claude.

## 3. Decisões já tomadas (e por quê)

| Decisão | Motivo |
| --- | --- |
| Construir direto o estado ideal e fazer o sistema crescer rápido, em vez de simular um legado e migrá-lo. | A demonstração é do resultado. Como adotar em projeto existente vira um roteiro escrito. |
| `CLAUDE.md` na raiz, curto (cerca de 60 linhas), revisado por humano, só com o que o agente não descobre sozinho. | Contexto genérico não melhora o acerto e custa mais (ETH Zurich 2026; Radar Thoughtworks vol. 34). |
| Seguir as convenções do Claude Code: convenções de back e front em `.claude/rules/` com escopo por pasta, skills em `.claude/skills/`, subagentes em `.claude/agents/`. | A POC usa as melhores práticas da ferramenta em uso; adaptar para outra ferramenta é trabalho de quem for adotar. |
| Cada skill com SKILL.md curto e detalhe em `references/`. | Só a descrição fica sempre no contexto; o resto é lido quando preciso. |
| PO e tech lead como skills; busca de contexto e revisão como subagentes (`pesquisador-contexto`, `revisor`). | Skill roda na conversa e permite ir e voltar com o humano. Subagente faz tarefa fechada em contexto isolado e devolve só o relevante. |
| Backlog em GitHub Issues e Projects: fase = milestone, épico = issue `type: epic`, história = sub-issue `type: story`. | Simula o backlog real e liga história, spec e PR. |
| Spec por feature em `docs/specs/`, aprovada antes de qualquer código. | Spec-driven development (spec, plano, implementação). |
| Esboço de tela no Claude Design, a partir do repositório e de screenshots, sem `/design-sync`. | Aprovar a tela antes da spec. O `/design-sync` é voltado a React; o agente traduz o esboço para Angular. |
| Testes: unitários de domínio, integração com Testcontainers, componentes Angular, ponta a ponta com Playwright, teste de mutação (PIT) no domínio. | O humano define a estratégia, a IA executa. Mutação pega teste que passa sem testar nada. |
| Sensores (build, lint, testes, PIT) rodam no ciclo do agente e no GitHub Actions. | O agente se corrige antes da revisão humana. |
| Documentação híbrida em dois repositórios: a do projeto (ADRs, specs, runbooks) fica em `docs/` junto do código; a da área (visão, glossário, regras, áreas vizinhas) fica em `custodia-docs`, que publica no GitHub Pages um portal juntando as duas. | Doc de engenharia muda no mesmo PR do código; doc da área atravessa projetos. Simula o cenário real, em que o agente busca contexto num repositório que não é o do código. |
| Tabela de equivalência entre as ferramentas da POC e as do time (Claude Code → Devin, Claude Design → Figma com MCP). | O valor está no fluxo e nos artefatos do repositório; a ferramenta é encaixe. |

## 4. Domínio fictício

Banco fictício "Banco Exemplo". O sistema cobre a **custódia de financiamentos**: tudo o que acontece com o contrato depois de assinado (cronograma, pagamentos, encargos, antecipação, alterações, cancelamento e garantias). Simulação e contratação existem no portal, mas pertencem a "outro time" e ficam rasas.

| Módulo | O que faz | Profundidade |
| --- | --- | --- |
| Calculadoras (lib) | SAC, Price, carência, parcela balão, CET | Funda |
| Simulação | Simular e salvar proposta | Rasa (outro time) |
| Contratação | Proposta vira contrato e segue para a custódia | Rasa (outro time) |
| Contratos | Receber, gerar cronograma, consultar, extrato, histórico de eventos | Funda |
| Pagamentos | Baixa de parcela; atraso com multa, juros de mora e juros remuneratórios | Funda |
| Antecipação | Amortização parcial (reduz prazo ou parcela) e quitação | Funda |
| Alteração de plano | Troca de vencimento, renegociação | Funda |
| Cancelamento | Arrependimento em até 7 dias, estorno | Média |
| Garantias | Veículo: gravame em SNG simulado, baixa em até 10 dias após quitação, alertas | Funda |
| Imóvel e empréstimo | Produto e cálculo | Rasa |

Regras inspiradas em normas reais (CDC, Lei 10.931, Res. CONTRAN 807/2020), simplificadas. Cada regra é confirmada em fonte oficial no discovery ou na spec, nunca suposta.

Regras gerais:
- Valores monetários em `BigDecimal`, 2 casas, arredondamento HALF_EVEN.
- Evento aplicado duas vezes não duplica efeito (idempotência).
- Um chassi não pode ter dois gravames ativos.

As regras finas de cada módulo são definidas no discovery (fase 2) e nas specs.

## 5. Sistema

- **Backend:** monolito modular hexagonal num único módulo Gradle; os módulos de negócio da seção 4 são pacotes, e as regras de dependência entre camadas são verificadas por ArchUnit.
- **Código:** identificadores em inglês, pacote base `com.bancoexemplo.custody`; documentos em português, com o glossário ligando os termos.
- **Portal Angular:** cerca de 12 telas de formulário e tabela para o operador (simulação, contratação, contrato e cronograma, pagamento, antecipação, alteração, cancelamento, garantias e alertas).
- **Login:** usuários fixos com dois perfis, operador e supervisor. Sem servidor de identidade.
- **Integrações externas** (SNG, registradora) simuladas por adaptadores falsos.
- **Documentação:** do projeto em `docs/`, da área no repositório `custodia-docs`; o portal no GitHub Pages junta as duas e é publicado por GitHub Actions, que junta os repositórios. A estrutura e os templates são definidos na fase 1.
- **Repositório único** com backend e frontend.

## 6. Fluxo de uma feature

1. **História** no board.
2. **Refinamento** com a skill `product-owner`, que aciona o subagente `pesquisador-contexto` para trazer só a doc relevante. Ela traz opções e perguntas; o humano decide.
3. **Esboço de tela** no Claude Design. O humano aprova.
4. **Spec**: regras, critérios de aceite, link do esboço e cenários de teste de back, front e ponta a ponta. O humano aprova.
5. **Plano técnico** com a skill `tech-lead`, depois implementação pelo agente.
6. **Sensores** (build, lint, testes, PIT, Playwright) e subagente `revisor`.
7. **PR**, revisão humana e board atualizado.

## 7. Fases

| Fase | Entrega | Critério de pronto |
| --- | --- | --- |
| 0. Esqueleto | Backend Gradle (Java), Angular, Testcontainers, GitHub Actions com build e testes de back e front; repositório `custodia-docs` com o portal no GitHub Pages. | Build e testes verdes local e no CI; portal publicado. |
| 1. Padrões | Estrutura de docs e templates, `CLAUDE.md`, convenções, skills, subagentes, sensores (lint, PIT, Playwright), board. | Humano revisou cada arquivo; sensores rodando no CI. |
| 2. Discovery e backlog | Visão, glossário, regras gerais; épicos e histórias no board. | Documentos aprovados; backlog criado. |
| 3. Construção | Features em lotes pelo fluxo da seção 6; spike do Claude Design na primeira tela. Uma feature fica reservada para a demo. | Histórias entregues com sensores verdes; spike registrado. |
| 4. Demonstração | Feature reservada pelo fluxo completo e gravada; GitHub Pages; roteiro de adoção para projeto existente; tabela de equivalência; roteiro da reunião. | Site publicado e demo gravada. |

Em todas as fases, um diário curto registra tempo gasto e rodadas de correção por entrega, para mostrar a curva em J.

## 8. Como trabalhar comigo

- Sempre planejar antes e esperar minha aprovação para escrever código.
- Uma fase por vez. Ao fim de cada fase, atualizar o diário e parar.
- Se algo neste documento estiver ambíguo ou parecer errado, perguntar em vez de supor.
- Explicações curtas, em português do Brasil.

## 9. Fontes principais

- ETH Zurich, Evaluating AGENTS.md (2026, vale para `CLAUDE.md`): https://arxiv.org/abs/2602.11988
- Thoughtworks Technology Radar vol. 34, Técnicas (abr/2026): https://www.thoughtworks.com/radar/techniques
- DORA, ROI of AI-assisted Software Development (2026): https://www.infoq.com/news/2026/05/dora-roi-ai-assisted-dev-report/
- METR, atualização (fev/2026): https://metr.org/blog/2026-02-24-uplift-update/
- Böckeler, Spec-Driven Development: https://www.martinfowler.com/articles/exploring-gen-ai/sdd-3-tools.html
- Devin e Figma: https://docs.devin.ai/use-cases/gallery/figma-design-implementation
- Claude Design: https://support.claude.com/en/articles/14604416-get-started-with-claude-design
- GitHub Pages: https://docs.github.com/en/pages/getting-started-with-github-pages/creating-a-github-pages-site
- CDC (Lei 8.078/1990): https://www.planalto.gov.br/ccivil_03/leis/l8078compilado.htm
- Lei 10.931/2004 (CCB): https://www.planalto.gov.br/ccivil_03/_ato2004-2006/2004/lei/l10.931.htm
- Res. CONTRAN 807/2020 (registro de contrato e gravame): https://www.fortes.adv.br/2021/07/08/garantia-fiduciaria-de-veiculos-e-a-resolucao-contran-807/
