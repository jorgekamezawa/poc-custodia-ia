"""Lets a read-only subagent run only git commands that read: diff, log, show and status.

Usage:
  guard_readonly_git.py    Claude Code PreToolUse hook, declared in the subagent frontmatter

A subagent's tool list can't narrow Bash to some commands: a specifier there removes
the whole tool. So the subagent keeps Bash and this hook denies everything else.
"""

import json
import re
import sys

# Any shell metacharacter could chain or redirect a second command, so none is allowed.
ALLOWED = re.compile(r"\s*git\s+(diff|log|show|status)\b[^;&|<>`$\n]*")


def run_pre(stdin=None, stdout=None):
    stdin, stdout = stdin or sys.stdin, stdout or sys.stdout
    payload = json.load(stdin)
    if payload.get("tool_name") != "Bash":
        return 0
    command = payload.get("tool_input", {}).get("command", "")
    if ALLOWED.fullmatch(command):
        return 0
    print(json.dumps({"hookSpecificOutput": {
        "hookEventName": "PreToolUse",
        "permissionDecision": "deny",
        "permissionDecisionReason":
            "Este subagente só lê: use git diff, log, show ou status, sem encadear comandos.",
    }}, ensure_ascii=False), file=stdout)
    return 0


if __name__ == "__main__":
    sys.exit(run_pre())
