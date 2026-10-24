# Calibração dos subagentes revisores

Casos reais do mandato 1 (`docs/decisoes/0001-mandato-1.md`), com o veredito certo. Quando o prompt de um revisor mudar, rode-o contra o caso (o PR ou o arquivo na versão citada) e confira se ele chega ao mesmo veredito; divergência é sinal de que a mudança piorou o revisor.

## revisor-docs
| Caso | Onde | Veredito certo |
|---|---|---|
| "juros remuneratórios (chamados só de juros nesta spec)" | spec 0001, PR #30 | Violação da regra 8 (apelido local). Na época foi aceito; na spec 0002 foi recusado. |
| Cenário "prazo 0 e 73 meses" numa linha só | spec 0001, CT-11 | Violação: um caso por linha. Na época foi aceito. |
| CT-6 e CT-7 juntando liberação no vencimento e depois dele | spec 0003, primeira versão | Violação: um caso por linha (apontado certo). |
| `vehicleChassis` na API com `chassisNumber` no glossário | spec 0005, primeira versão | Violação da regra 8 (apontado certo). |
| `fine` e `lateInterest` com `LateFee` e `DefaultInterest` no glossário | spec 0008, primeira versão | Violação da regra 8 (apontado certo). |
| PAG-05 marcada "adicionada" e ainda ausente em `regras/` | spec 0008 | Não é violação. |

## revisor-codigo
| Caso | Onde | Veredito certo |
|---|---|---|
| `docs/decisoes/0001-mandato-1.md` fora das peças da task | PRs #44 e #54 | Não é violação: com mandato, registro e diário mudam em todo PR. Foi apontado errado. |
| CT-13 da moldura passava sem o perfil na tela (nome igual ao perfil) | PR #55 | Violação: o teste não prova o resultado (apontado certo). |
| CT-17 do ponta a ponta achava "R$ 10.000,00" do valor financiado, não do saldo | PR #74 | Violação: o teste não prova o resultado (apontado certo). |
| Sessão no `custodia-docs` ao revisar branch do projeto | PR #64, primeira passada | Revisão impossível: sem o diff não se confere peça nem teste apagado. |

## pesquisador-contexto
| Caso | Onde | Veredito certo |
|---|---|---|
| "Não há teste ArchUnit" | refinamento da #8 | Errado: `backend/src/test/java/.../architecture/ArchitectureTest.java` existia. |
| "`e2e/tests` não existe" | levantamento da #12 | Errado: `e2e/tests/smoke.spec.ts` existia. |
