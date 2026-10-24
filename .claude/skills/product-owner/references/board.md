# Operar issues e board pelo gh

Nada aqui é fixo de um repositório: repositório, board e IDs são descobertos na hora.

## Conteúdo
- Descobrir repositório, board e IDs
- Criar issue, sub-issue e adicionar ao board
- Task como sub-issue da história
- Mudar ordem
- Armadilhas

## Descobrir repositório, board e IDs
```bash
gh repo view --json nameWithOwner -q .nameWithOwner
gh project list --owner <owner> --format json -q '.projects[] | "\(.number) \(.title)"'
# campo Status e opções (IDs das colunas)
gh api graphql -f query='{ user(login:"<owner>"){ projectV2(number:<n>){ id
  field(name:"Status"){ ... on ProjectV2SingleSelectField { id options{ id name } } } } } }'
```
Colunas do board: Backlog, Pronto, Em andamento, Em revisão, Feito.
Labels de tipo: `type: epic`, `type: story`, `type: task`, `type: bug`.
Ler e mexer no board exige o scope `project` no token: `gh auth status`; se faltar, `gh auth refresh -h github.com -s project`.

## Criar issue, sub-issue e adicionar ao board
```bash
gh issue create --title "<título>" --body "<corpo>" --label "type: story" --milestone "3. Construção"
```
Uma flag `--label` por label, nunca várias numa string só.

O `gh` não cria sub-issue; o vínculo é por API, usando o **id** da issue (não o número):
```bash
gh api repos/<owner>/<repo>/issues/<numero_do_filho> -q .id
gh api -X POST repos/<owner>/<repo>/issues/<numero_do_pai>/sub_issues -F sub_issue_id=<id_do_filho>
```

Adicionar ao board e marcar a coluna:
```bash
gh api graphql -f query='mutation{ addProjectV2ItemById(input:{projectId:"<PVT_...>",contentId:"<node_id_da_issue>"}){item{id}} }'
gh api graphql -f query='mutation{ updateProjectV2ItemFieldValue(input:{projectId:"<PVT_...>",itemId:"<PVTI_...>",
  fieldId:"<PVTSSF_...>",value:{singleSelectOptionId:"<id_da_opção>"}}){projectV2Item{id}} }'
```

## Task como sub-issue da história
Cada arquivo de `docs/specs/NNNN-titulo/tasks/` aprovado e mergeado vira uma issue:
- título igual ao do arquivo (`01 · back · Registro de gravame`);
- corpo com o link do arquivo na `main`;
- label `type: task` e o milestone da história;
- ligada como sub-issue da história, pelo comando acima.

A automação de sub-issues já põe a task no board.

## Mudar ordem
A ordem no board é posição do item, não número da issue:
```bash
gh api graphql -f query='mutation{ updateProjectV2ItemPosition(input:{projectId:"<PVT_...>",itemId:"<este>",
  afterId:"<vem depois deste>"}){items(first:1){totalCount}} }'
```

## Armadilhas
- **Renomear coluna, nunca substituir.** Em `updateProjectV2Field`, mandar as opções sem o `id` apaga as antigas e cria outras; as automações que apontavam para elas quebram e são desligadas. Passe sempre o `id` de cada opção.
- **A API não cria automação.** Só existe `deleteProjectV2Workflow`. Um board novo nasce como cópia (`copyProjectV2`) de um que já tenha as automações certas; o "Auto-add to project" copiado aponta para o repositório antigo e é reapontado na tela.
- **A API não agrupa view.** "Group by" é na tela.
- **Item recém-adicionado demora a aparecer** na listagem do board; espere e liste de novo, nunca recrie a issue.
- **A automação de sub-issues já adiciona os filhos ao board.** "Content already exists in this project" significa item já existente.
- **Número de issue nunca é reaproveitado.** Apagar deixa buraco; por isso a estrutura é aprovada antes de criar.
