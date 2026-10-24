---
status: aceito
atualizado: 2026-10-24
descricao: As telas do portal usam os componentes do Angular Material, porque eles já trazem acessibilidade, tema e formulários prontos e são mantidos pelo time do Angular.
---
# ADR-0014: Componentes do portal com Angular Material

**Decisão:** as telas do portal são montadas com os componentes do Angular Material (biblioteca de botões, campos, tabelas e diálogos do próprio time do Angular), porque eles já trazem acessibilidade, tema e integração com formulários, e acompanham cada versão do Angular.

## Contexto
O portal terá cerca de 12 telas de back-office, com formulários, tabelas de parcelas e mensagens de erro. O [ADR-0006](0006-angular-no-frontend.md) escolheu o Angular, mas não com que componentes as telas são feitas.

## Alternativas descartadas
- **Componentes próprios com CSS escrito à mão:** cada campo, tabela e diálogo é feito no projeto. Descartado porque acessibilidade (teclado, leitor de tela) e estados de erro teriam de ser feitos e testados um a um.
- **Biblioteca de terceiros (ex.: PrimeNG):** catálogo maior de componentes prontos. Descartado porque segue outro calendário de versões, o que pode atrasar a atualização do Angular.

## Consequências
- **Ganhos:** telas consistentes desde a primeira; campos com mensagem de erro e acessibilidade prontos; testes de componente usam os harnesses (objetos de teste que leem o componente como o usuário vê) da própria biblioteca.
- **Custos:** a aparência segue o Material Design, e fugir dela exige sobrescrever o tema.
- **Passa a ser obrigatório:** telas novas do portal usam os componentes do Angular Material.
