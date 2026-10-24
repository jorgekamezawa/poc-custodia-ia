---
status: registrado
atualizado: 2026-10-24
descricao: Decisões do mandato 1 de autonomia, sobre escopo, cálculo, arquitetura, infraestrutura e correções no material de trabalho.
---
# Registro-0001: Mandato 1 de autonomia

**Cobre:** as decisões que iriam para a pessoa durante o mandato 1 (lote 1, mensageria, banco não relacional, alertas de gravame), de 2026-10-24 até o fim do mandato. Atualizado no PR de cada entrega enquanto o mandato vale; congelado no fim.

## Decisões

### 1. O que é a "lib" Calculadoras? (2026-10-24)
| Opção | A favor | Contra |
|---|---|---|
| **Pacote `calculator` do monólito, exposto pela `api/`** | Segue o ADR-0002; ArchUnit protege a fronteira | Não é reutilizável fora do backend |
| Artefato Gradle separado | Reuso por outros sistemas | Contraria o ADR-0002 (um módulo Gradle); ninguém de fora consome hoje |

- **Escolha:** pacote `calculator`, sem banco nem REST; outros módulos chamam a interface da `api/`.
- **Motivo:** o único consumidor é o próprio backend.

### 2. Regras do cálculo Price (2026-10-24)
- **Escolha:** taxa efetiva mensal em percentual; parcela, juros e amortização arredondados ao centavo; resíduo na última parcela; vencimento no mesmo dia do mês, com último dia do mês quando faltar; limites de R$ 1.000,00 a R$ 1.000.000,00, 0% a 10% ao mês e 1 a 72 meses (CAL-01 a CAL-03 na [spec 0001](../specs/0001-cronograma-price/spec.md)).
- **Motivo:** é a convenção de mercado para Price; os limites são fictícios da POC, faixa típica de financiamento de veículo.

### 3. Refinamento das histórias #9 e #10 (2026-10-24)
- **Escolha:** reaproveitar o levantamento do pesquisador da #8, que cobriu o módulo Calculadoras inteiro (#8 a #10).
- **Motivo:** o contexto não mudou entre uma história e outra; um segundo levantamento repetiria o primeiro.

### 4. Como calcular o CET (2026-10-24)
| Opção | A favor | Contra |
|---|---|---|
| **Bisseção com desconto em `double`, resultado em `BigDecimal`** | Simples; erro de 10⁻¹⁵ muito abaixo das 2 casas do resultado | Usa `double` perto de dinheiro |
| Logaritmo e exponencial em `BigDecimal` escritos à mão | Tudo decimal | Código de série numérica para manter e testar |
| Biblioteca de matemática decimal | Tudo decimal sem código próprio | Dependência nova para uma fórmula |

- **Escolha:** bisseção em `double`; parcelas e valor liberado seguem `BigDecimal` e só a taxa é procurada em `double` (task 01 da [spec 0003](../specs/0003-custo-efetivo-total/spec.md)).
- **Motivo:** o CET é uma taxa arredondada a 2 casas, não um valor em dinheiro; o ADR-0009 trata de valores em reais.
- **Fonte:** [Resolução CMN 4.881/2020](https://www.bcb.gov.br/content/estabilidadefinanceira/especialnor/Resolu%C3%A7%C3%A3o4881.pdf), art. 4º, consultada em 2026-10-24.
- **Escopo:** IOF e seguros entram como custos já somados; o cálculo do IOF fica fora.
- **Mutantes equivalentes:** 3 de 54 sobrevivem no `TotalEffectiveCostRate`, todos trocando `>` por `>=` (ou `<` por `<=`) nos laços da bisseção; só mudariam algo num empate exato de ponto flutuante, e uma volta a mais não muda o resultado em 2 casas. Mutação em 94%, acima do limite; não foram excluídos da configuração.

## Corrigido no caminho
- **Título de task repetido no board:** "02 · qa · Validação" se repetiria em toda spec; as issues #31 e #32 ganharam o prefixo `0001 · `. O `board.md` passa a pedir o prefixo (PR de material do mandato 1).
- **Link para o custodia-docs:** o `check_docs.py` agora acusa link relativo que sai do repositório e o padrão manda usar a URL do GitHub ([custodia-docs#6](https://github.com/jorgekamezawa/custodia-docs/pull/6)).
- **Teste parametrizado sem o cenário no relatório:** o JUnit nomeia `[1] term = 0`; os testes da spec 0001 usam `name = "{displayName} [{arguments}]"` (task 02 da spec 0001); a convenção entrou na rule do backend (PR de material do mandato 1).
- **Exemplo de nome de teste divergente:** `backend.md` mostra `GAR-03 CT-4` e a `padrao-testes` mostra `GAR-03 0007/CT-4`; vale o da skill; rule corrigida (PR de material do mandato 1).

- **pesquisador-contexto deixou passar arquivo:** disse que não havia teste ArchUnit, mas o roteiro já apontava a pasta; o subagente passa a fazer Glob do caminho exato antes de declarar ausência (PR de material do mandato 1).

## Em aberto
- **revisor-docs inconsistente entre specs:** na spec 0001 sugeriu escrever "juros remuneratórios (chamados só de juros nesta spec)"; na 0002 acusou esse mesmo apelido como violação da regra 8. Segui a leitura estrita (nome completo do glossário). Vale o padrão dizer se apelido local de termo é permitido.
- **revisor-docs aceita e recusa o mesmo formato de cenário:** nas specs 0001 e 0002 aceitou cenários com dois casos numa linha (ex.: 0001/CT-8); na 0003 recusou. Separei um caso por cenário daqui em diante; as specs entregues ficam como estão.
- **revisor-codigo não conhece o registro do mandato:** acusou o `docs/decisoes/0001-mandato-1.md` como arquivo fora das peças da task (PR #44); recusei com motivo. Vale o subagente saber que, com mandato, o registro muda em todo PR.
