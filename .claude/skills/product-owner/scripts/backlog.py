"""Creates milestones, epics and stories on GitHub and puts them on the board, in plan order.

Usage:
  python3 backlog.py plan.json --dry-run   shows what would be created, creates nothing
  python3 backlog.py plan.json             creates

plan.json:
{
  "milestones": [{"title": "3. Construção", "description": "..."}],
  "epics": [
    {"milestone": "3. Construção", "title": "Baixa de gravame", "body": "...",
     "stories": [{"title": "Dar baixa no gravame após quitação", "body": "..."}]}
  ]
}

Repository, board and IDs are discovered through gh. Needs the labels `type: epic` and
`type: story` and the `project` scope on the token (gh auth refresh -h github.com -s project).
"""

import json
import subprocess
import sys
import time

BACKLOG_OPTION = "Backlog"
BOARD_WAIT_SECONDS = 30


def gh(*args, stdin=None):
    out = subprocess.run(["gh", *args], input=stdin, capture_output=True, text=True)
    if out.returncode or '"errors"' in out.stdout:
        raise SystemExit(f"falhou: gh {' '.join(args)}\n{out.stderr}{out.stdout}")
    return out.stdout


def api(method, path, **fields):
    return json.loads(gh("api", "-X", method, path, "--input", "-", stdin=json.dumps(fields)) or "{}")


def gql(query, **variables):
    body = json.dumps({"query": query, "variables": variables})
    return json.loads(gh("api", "graphql", "--input", "-", stdin=body))["data"]


def validate(plan, existing):
    known = set(existing) | {m["title"] for m in plan.get("milestones", [])}
    errors = []
    for epic in plan["epics"]:
        if not epic.get("title", "").strip():
            errors.append(f"épico sem título no milestone {epic.get('milestone')}")
        if epic.get("milestone") not in known:
            errors.append(f"épico '{epic.get('title')}' aponta para milestone desconhecido: {epic.get('milestone')}")
        for story in epic.get("stories", []):
            if not story.get("title", "").strip():
                errors.append(f"história sem título no épico '{epic.get('title')}'")
    return errors


def summary(plan, existing):
    lines = [f"{'já existe' if m['title'] in existing else 'criar'} milestone: {m['title']}"
             for m in plan.get("milestones", [])]
    total = 0
    for epic in plan["epics"]:
        lines.append(f"criar épico [{epic['milestone']}]: {epic['title']}")
        total += 1
        for story in epic.get("stories", []):
            lines.append(f"  criar história: {story['title']}")
            total += 1
    lines.append(f"total: {total} issues")
    return lines


def discover():
    repo = gh("repo", "view", "--json", "nameWithOwner", "-q", ".nameWithOwner").strip()
    owner, name = repo.split("/")
    boards = gql("""query($o:String!,$n:String!){ repository(owner:$o,name:$n){
        projectsV2(first:10){ nodes{ id number title
          field(name:"Status"){ ... on ProjectV2SingleSelectField { id options{ id name } } } } } } }""",
                 o=owner, n=name)["repository"]["projectsV2"]["nodes"]
    if not boards:
        raise SystemExit("nenhum board ligado a este repositório")
    if len(boards) > 1:
        raise SystemExit("mais de um board ligado; resolva qual usar antes de rodar")
    board = boards[0]
    field = board["field"]
    option = next(o["id"] for o in field["options"] if o["name"] == BACKLOG_OPTION)
    return repo, board, field["id"], option


def existing_milestones(repo):
    return {m["title"]: m["number"] for m in json.loads(gh("api", f"repos/{repo}/milestones?state=all"))}


def board_items(board_number, owner):
    items, cursor = {}, None
    while True:
        page = gql("""query($o:String!,$n:Int!,$c:String){ user(login:$o){ projectV2(number:$n){
            items(first:100, after:$c){ pageInfo{ hasNextPage endCursor }
              nodes{ id content{ ... on Issue { id } } } } } } }""",
                   o=owner, n=board_number, c=cursor)["user"]["projectV2"]["items"]
        items.update({n["content"]["id"]: n["id"] for n in page["nodes"] if n.get("content")})
        if not page["pageInfo"]["hasNextPage"]:
            return items
        cursor = page["pageInfo"]["endCursor"]


def wait_for_items(board_number, owner, node_ids):
    # The board lists a newly added item only after a few seconds.
    deadline = time.time() + BOARD_WAIT_SECONDS
    while True:
        items = board_items(board_number, owner)
        if all(n in items for n in node_ids) or time.time() > deadline:
            return items
        time.sleep(3)


def main(argv):
    if not argv:
        raise SystemExit(__doc__)
    with open(argv[0], encoding="utf-8") as f:
        plan = json.load(f)
    repo, board, field_id, backlog_option = discover()
    owner = repo.split("/")[0]
    existing = existing_milestones(repo)

    errors = validate(plan, existing)
    if errors:
        raise SystemExit("plano inválido:\n" + "\n".join(f"- {e}" for e in errors))

    if "--dry-run" in argv:
        print(f"repositório: {repo}\nboard: #{board['number']} {board['title']}\n")
        print("\n".join(summary(plan, existing)))
        print("Nada foi criado.")
        return 0

    for m in plan.get("milestones", []):
        if m["title"] not in existing:
            created = api("POST", f"repos/{repo}/milestones", title=m["title"],
                          description=m.get("description", ""))
            existing[m["title"]] = created["number"]
            print(f"milestone criado: {m['title']}")

    created_issues = []
    for e in plan["epics"]:
        epic = api("POST", f"repos/{repo}/issues", title=e["title"], body=e.get("body", ""),
                   milestone=existing[e["milestone"]], labels=["type: epic"])
        created_issues.append(epic)
        print(f"épico #{epic['number']}: {epic['title']}")
        for s in e.get("stories", []):
            story = api("POST", f"repos/{repo}/issues", title=s["title"], body=s.get("body", ""),
                        milestone=existing[e["milestone"]], labels=["type: story"])
            api("POST", f"repos/{repo}/issues/{epic['number']}/sub_issues", sub_issue_id=story["id"])
            created_issues.append(story)
            print(f"  história #{story['number']}: {story['title']}")

    # The sub-issue automation may have added the children already; add only what is missing.
    on_board = board_items(board["number"], owner)
    for issue in created_issues:
        if issue["node_id"] not in on_board:
            gql("""mutation($p:ID!,$c:ID!){ addProjectV2ItemById(input:{projectId:$p,contentId:$c}){item{id}} }""",
                p=board["id"], c=issue["node_id"])

    items = wait_for_items(board["number"], owner, [i["node_id"] for i in created_issues])
    previous = None
    for issue in created_issues:
        item = items.get(issue["node_id"])
        if not item:
            print(f"aviso: #{issue['number']} não apareceu no board em {BOARD_WAIT_SECONDS} s; "
                  "ordene na tela, não rode o script de novo (ele recriaria as issues)")
            continue
        gql("""mutation($p:ID!,$i:ID!,$f:ID!,$o:String!){ updateProjectV2ItemFieldValue(input:{projectId:$p,
            itemId:$i,fieldId:$f,value:{singleSelectOptionId:$o}}){projectV2Item{id}} }""",
            p=board["id"], i=item, f=field_id, o=backlog_option)
        if previous:
            gql("""mutation($p:ID!,$i:ID!,$a:ID!){ updateProjectV2ItemPosition(input:{projectId:$p,itemId:$i,
                afterId:$a}){items(first:1){totalCount}} }""", p=board["id"], i=item, a=previous)
        previous = item
    print(f"\n{len(created_issues)} issues no board, em Backlog, na ordem do plano.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
