---
status: aceito
atualizado: 2026-10-24
descricao: Valores em dinheiro usam BigDecimal, com 2 casas e arredondamento para o par só na fronteira em que viram dinheiro.
---
# ADR-0009: Dinheiro em decimal exato

**Decisão:** todo valor em dinheiro é `BigDecimal`, guardado com 2 casas e arredondado para o par (HALF_EVEN, ou seja, 0,125 vira 0,12 e 0,135 vira 0,14) só na fronteira em que o cálculo vira dinheiro, porque cálculo de juros e parcelas não tolera erro de centavo.

## Contexto
Cronogramas SAC e Price, juros de mora e antecipação acumulam muitas operações. Um centavo de diferença entre parcela e saldo vira divergência de contrato.

## Alternativas descartadas
- **`double`:** ponto flutuante binário, rápido. Descartado porque não representa 0,10 exato e acumula erro.
- **Centavos em `long`:** inteiro exato. Descartado porque taxas e cálculos intermediários precisam de mais casas que o centavo, e a conversão espalharia arredondamentos pelo código.

## Consequências
- **Ganhos:** resultado exato e reproduzível; arredondamento num lugar só.
- **Custos:** `BigDecimal` é mais verboso e mais lento que tipos primitivos.
- **Passa a ser obrigatório:** comparar com `compareTo`, nunca `equals` (que diferencia 1,0 de 1,00); construir a partir de texto, nunca de `double`; no banco, coluna `numeric` com escala 2.
