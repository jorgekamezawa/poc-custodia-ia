---
name: padrao-testes
description: Estratégia de testes da custódia: como transformar critérios de aceite em cenários de teste, em que camada cada cenário mora, quais bordas cobrir e como ligar regra, cenário, task e teste. Use ao escrever os cenários de uma spec, ao planejar os testes de uma task e ao implementar ou revisar testes.
---

# Padrão de testes

O humano define a estratégia; o agente a executa. As convenções de escrita do teste estão em `.claude/rules/backend.md` e `frontend.md`; aqui fica o que decide **qual** teste existe e **onde**.

## Rastreabilidade
Regra (`GAR-03`) → critério (`CA-n`) → cenário (`CT-n`) → task (seção "Cenários cobertos") → teste.
- Todo critério tem ao menos um cenário; todo cenário está em uma task.
- O nome do teste começa pelo número da spec e pelo cenário, porque `CT-n` se repete entre specs: `@DisplayName("GAR-03 0007/CT-4: ...")` no back, `it('0007/CT-4: ...')` no componente e `test('0007/CT-4: ...')` no Playwright.

## Em que camada fica cada cenário
A mais barata que prova o critério.

| Tipo | Prova | Mora em | Nasce na task |
|---|---|---|---|
| Unitário de domínio | regra, cálculo, borda | `<modulo>/domain`, sem Spring | back |
| Caso de uso | orquestração, com as portas mockadas (Mockito) | `<modulo>/application` | back |
| Adaptador de saída | cada método da porta contra o real: banco, Kafka ou módulo vizinho | `<modulo>/adapter/out` | back |
| API | contrato da spec: status, corpo, erros, perfil | `<modulo>/adapter/in` | back |
| Arquitetura | fronteira nova entre módulos ou camadas | `architecture/` | back |
| Componente | o que a tela mostra e faz | `frontend/src/app/<modulo>/` | front |
| Ponta a ponta | o caminho principal de cada critério com tela | `e2e/tests/` | qa |

- Na tabela de cenários da spec, caso de uso conta como `unitário` e API como `integração`.
- Ponta a ponta não repete borda já provada abaixo; cobre o fluxo atravessando tudo.
- **O cenário mora onde o código que o produz roda.** Se o resultado depende do que o adaptador grava ou lê (ex.: saldo que cai depois de marcar a parcela paga), o cenário é de adaptador, não de caso de uso: com a porta mockada, o caso de uso só prova que chamou a porta. Foi o que escondeu o 0007/CT-11, provado por um falso que usava o domínio enquanto o banco rodava outro SQL.
- Teste de mutação não se escreve: mede os testes de `domain` e `application`. Sobrou mutante? Escreva o teste que o mata ou, se ele não muda o comportamento (mutante equivalente), mostre por quê e peça para excluí-lo.

## Bordas que viram cenário próprio
- **Dinheiro:** zero, um centavo, arredondamento HALF_EVEN no meio (`0,125`, `0,135`).
- **Prazo:** o último dia dentro e o primeiro fora (ex.: 7.º e 8.º dia do arrependimento); fim de mês e 29 de fevereiro no vencimento.
- **Idempotência:** o mesmo evento aplicado duas vezes não duplica efeito.
- **Estado:** operação em contrato quitado, cancelado ou inexistente.
- **Perfil:** ação de supervisor tentada por operador.

Use só as que o critério toca.

## Escrever um cenário
Uma linha da tabela da spec: identificador, critério, camada, entrada com valores reais e resultado esperado verificável ("parcela 12 com R$ 1.034,57", não "valor correto").
- **Um caso por linha.** Dois valores de entrada com o mesmo resultado (ex.: "0 e 73 meses") ou um recusado e um aceito são cenários separados; a linha pode dizer "recusado com a mensagem do CT-n".

## Ao implementar
- Bug: primeiro o teste que reproduz, depois o conserto.
- Teste que falha nunca é enfraquecido ou apagado, nem o limite de mutação afrouxado: ataque a causa.
- Pronto é suíte verde com os comandos do `CLAUDE.md`, saída mostrada.
