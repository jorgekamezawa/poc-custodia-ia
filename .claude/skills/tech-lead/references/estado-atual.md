# Levantamento do estado atual

Entrada para o `pesquisador-contexto`: o módulo afetado e a pergunta. Em cada passo, leia o campo `descricao` (ou o nome do arquivo) antes de abrir o conteúdo.

1. **Decisões:** `docs/adr/` e `../custodia-docs/adr/`. Abra só os que tocam a pergunta.
2. **Fronteiras:** `.claude/rules/backend.md` e `frontend.md`; testes ArchUnit em `backend/src/test/java/com/bancoexemplo/custody/architecture/`.
3. **Módulo no backend:** `backend/src/main/java/com/bancoexemplo/custody/<modulo>/`: o que está em `api/`, as portas em `application/port/` e os adaptadores em `adapter/`.
4. **Dados:** migrações em `backend/src/main/resources/db/migration/` que tocam as tabelas do módulo.
5. **Módulo no frontend:** `frontend/src/app/<modulo>/` e `shared/`.
6. **Histórico:** specs do módulo em `docs/specs/` (status `entregue`) e `../custodia-docs/regras/<modulo>.md`.
