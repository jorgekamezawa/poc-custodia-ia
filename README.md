# poc-custodia-ia

POC de desenvolvimento orientado a IA num sistema fictício de custódia de financiamentos.

## Como rodar localmente
- Frontend: `cd frontend && npm ci && npm start`, depois abrir http://localhost:4200.
- Backend: ainda sem banco local configurado; por enquanto roda só pelos testes.

## Como rodar os testes
- Backend: `cd backend && ./gradlew build` (precisa de Docker, por causa do Testcontainers).
- Frontend: `cd frontend && npm test -- --watch=false`.
- Documentação: `python3 ../custodia-docs/scripts/check_docs.py --root .`, com o `custodia-docs` clonado ao lado.

## Documentação
- Portal da área: https://jorgekamezawa.github.io/custodia-docs/
- Contexto e decisões da POC: [docs/poc-contexto.md](docs/poc-contexto.md)
