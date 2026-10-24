---
status: ativo
atualizado: 2026-10-24
descricao: Como subir, usar e derrubar backend, banco e portal na máquina local, quando for desenvolver ou testar a mão.
---
# Runbook: Rodar localmente

**Quando usar:** para desenvolver ou testar o portal na própria máquina.

## Pré-requisitos
- Docker rodando, Java 21 e Node 24.
- Portas livres: 8080 (backend), 4200 (portal) e 15432 (banco).

## Passos
1. `cd backend && ./gradlew bootRun`: sobe o PostgreSQL do `compose.yaml` e o backend; espere "Started CustodyApplication".
2. Em outro terminal, `cd frontend && npm start`: o portal abre em `http://localhost:4200` e repassa `/api` ao backend.
3. Para derrubar, `Ctrl+C` nos dois terminais: o backend para o contêiner do banco ao sair.

## Como confirmar que deu certo
- `docker ps` mostra `poc-custodia-ia-postgres-1` enquanto o backend roda, e nada depois de sair.
- `http://localhost:4200` abre o portal.

## Se der errado
- **Porta ocupada ou processo que sobrou** (o `bootRun` deixa um Java filho quando é interrompido de fora): `kill $(ss -ltnp 'sport = :8080' | grep -o 'pid=[0-9]*' | cut -d= -f2)`, trocando a porta por 4200 para o portal. Não use `pkill -f`: o padrão casa com o próprio comando e mata o terminal.
- **Banco com dado velho:** os dados ficam no volume `poc-custodia-ia-postgres-data`. Para zerar, com o backend parado: `docker rm poc-custodia-ia-postgres-1 && docker volume rm poc-custodia-ia-postgres-data`. Nunca `docker compose down -v`, que apaga os volumes de todos os arquivos da linha.
