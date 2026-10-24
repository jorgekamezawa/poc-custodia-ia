# poc-custodia-ia

POC de desenvolvimento orientado a IA num sistema fictício de custódia de financiamentos.

## Como rodar localmente
- Frontend: `cd frontend && npm ci && npm start`, depois abrir http://localhost:4200.
- Backend: ainda sem banco local configurado; por enquanto roda só pelos testes.

## Como rodar os testes
- Backend: `cd backend && ./gradlew build` (precisa de Docker, por causa do Testcontainers).
- Frontend: `cd frontend && npm test -- --watch=false`.
- Ponta a ponta: `cd e2e && npm ci && npx playwright install chromium && npm test`. No WSL, o Chromium precisa de bibliotecas do sistema (`sudo npx playwright install-deps chromium`); sem elas, rode na imagem oficial: `docker run --rm --network host --user $(id -u):$(id -g) -e HOME=/tmp -v "$PWD":/work -w /work/e2e mcr.microsoft.com/playwright:v1.64.1-noble npx playwright test`.
- Documentação: `python3 ../custodia-docs/scripts/check_docs.py --root .`, com o `custodia-docs` clonado ao lado.

## Documentação
- Portal da área: https://jorgekamezawa.github.io/custodia-docs/
- Contexto e decisões da POC: [docs/poc-contexto.md](docs/poc-contexto.md)
