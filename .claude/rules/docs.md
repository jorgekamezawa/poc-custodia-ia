---
paths:
  - "docs/**"
---

# Ao escrever documento

1. Siga `../custodia-docs/padroes/documentacao.md` e comece do template do tipo em `../custodia-docs/templates/`.
2. O hook roda `check_docs.py` ao salvar; corrija todo erro antes de seguir.
3. Ao terminar, chame o subagente `revisor-docs` com o caminho do documento e corrija o que proceder.
4. Só então mostre para aprovação, com as decisões que você tomou sem a pessoa ter definido.
