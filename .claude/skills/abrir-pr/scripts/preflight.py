"""Checks that the current branch can become a pull request.

Usage:
  preflight.py    prints what is missing and exits 1, or prints nothing and exits 0

Runs the checks that don't need judgment, so the skill text doesn't have to be trusted
for them: not on main, nothing uncommitted, rebased on origin/main, no other PR open.
"""

import json
import subprocess
import sys


def run(*args):
    return subprocess.run(args, capture_output=True, text=True)


def collect():
    run("git", "fetch", "-q", "origin", "main")
    prs = run("gh", "pr", "list", "--state", "open", "--json", "number,headRefName")
    if prs.returncode:
        raise SystemExit(f"falhou: gh pr list\n{prs.stderr}")
    return {
        "branch": run("git", "branch", "--show-current").stdout.strip(),
        "dirty": run("git", "status", "--porcelain").stdout.rstrip(),
        "behind_main": run("git", "merge-base", "--is-ancestor", "origin/main", "HEAD").returncode != 0,
        "open_prs": json.loads(prs.stdout or "[]"),
    }


def problems(state):
    found = []
    if state["branch"] in ("main", ""):
        found.append("Está na main ou sem branch: crie a branch da entrega.")
    if state["dirty"]:
        found.append("Há mudança sem commit:\n" + state["dirty"])
    if state["behind_main"]:
        found.append("A branch não contém a origin/main: git rebase origin/main.")
    for pr in state["open_prs"]:
        found.append(f"PR #{pr['number']} ({pr['headRefName']}) ainda aberto: um PR aberto por vez.")
    return found


def main():
    found = problems(collect())
    for problem in found:
        print(f"- {problem}")
    return 1 if found else 0


if __name__ == "__main__":
    sys.exit(main())
