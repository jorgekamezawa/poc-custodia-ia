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

## Corrigido no caminho
- **Título de task repetido no board:** "02 · qa · Validação" se repetiria em toda spec; as issues #31 e #32 ganharam o prefixo `0001 · `. O `board.md` passa a pedir o prefixo no PR de material.
- **Link para o custodia-docs:** o `check_docs.py` agora acusa link relativo que sai do repositório e o padrão manda usar a URL do GitHub ([custodia-docs#6](https://github.com/jorgekamezawa/custodia-docs/pull/6)).
- **Teste parametrizado sem o cenário no relatório:** o JUnit nomeia `[1] term = 0`; os testes da spec 0001 usam `name = "{displayName} [{arguments}]"` (task 02 da spec 0001); a convenção vai para a rule do backend.
- **Exemplo de nome de teste divergente:** `backend.md` mostra `GAR-03 CT-4` e a `padrao-testes` mostra `GAR-03 0007/CT-4`; vale o da skill, e a rule é corrigida no PR de material.

## Em aberto
- **pesquisador-contexto deixou passar arquivo:** no refinamento da #8 disse que não havia teste ArchUnit, mas `backend/src/test/java/.../architecture/ArchitectureTest.java` existe. Ver se o roteiro `estado-atual.md` aponta a pasta de teste de um jeito que o Glob ache.
