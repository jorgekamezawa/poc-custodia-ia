"""Asks for confirmation before publishing work: commit, push, opening and merging a pull request.

Usage:
  guard_git.py --pre    Claude Code PreToolUse hook (reads the tool call JSON on stdin)
  guard_git.py --post   Claude Code PostToolUse hook, opens the approval window

CLAUDE.md says publishing needs approval, but a memory file is context, not
configuration. A PreToolUse hook runs before any permission mode, so the prompt
appears in every session even when permissions are relaxed.

The three actions almost always happen together, so the first approved one opens a
short window in which the others don't ask again. The hook never learns the answer
itself: PostToolUse only fires when the command succeeded, which means it was approved.

A valid mandate (.claude/mandato.json, local and untracked, written only after the person
approves its scope) delegates these approvals until its "ate" date. Force push to main is
denied with or without one.
"""

import hashlib
import json
import re
import sys
import tempfile
import time
from datetime import date
from pathlib import Path

WINDOW_SECONDS = 5 * 60
MANDATE = Path(__file__).resolve().parent.parent / "mandato.json"

# Matched loosely on purpose: an extra confirmation costs a keystroke, a missed one
# breaks the rule. Shell separators bound the match so only the same segment counts.
SEGMENT = r"[^;&|\n]*"
GUARDED = [
    (re.compile(rf"\bgit\b{SEGMENT}\bcommit\b"), "commit"),
    (re.compile(rf"\bgit\b{SEGMENT}\bpush\b"), "push"),
    (re.compile(rf"\bgh\s+pr{SEGMENT}\bcreate\b"), "abertura de PR"),
    (re.compile(rf"\bgh\s+pr{SEGMENT}\bmerge\b"), "merge de PR"),
]
OTHERS = {
    "commit": "push e abrir PR",
    "push": "abrir PR",
    "abertura de PR": "push",
    "merge de PR": "push",
}
FORCE_PUSH_MAIN = re.compile(
    rf"\bgit\b{SEGMENT}\bpush\b(?={SEGMENT}(\s--force\S*|\s-f\b))(?={SEGMENT}\bmain\b)"
    rf"|\bgit\b{SEGMENT}\bpush\b{SEGMENT}\s\+main\b"
)


def marker(session_id):
    name = hashlib.sha256((session_id or "sem-sessao").encode()).hexdigest()[:16]
    return Path(tempfile.gettempdir()) / f"claude-guard-git-{name}"


def window_is_open(session_id, now=None):
    path = marker(session_id)
    if not path.exists():
        return False
    age = (now or time.time()) - path.stat().st_mtime
    return 0 <= age < WINDOW_SECONDS


def mandate_is_valid(today=None):
    try:
        until = date.fromisoformat(json.loads(MANDATE.read_text(encoding="utf-8"))["ate"])
    except (OSError, ValueError, KeyError, TypeError):
        return False
    return (today or date.today()) <= until


def decision(kind, reason):
    return json.dumps({"hookSpecificOutput": {
        "hookEventName": "PreToolUse",
        "permissionDecision": kind,
        "permissionDecisionReason": reason,
    }}, ensure_ascii=False)


def guarded_action(payload):
    if payload.get("tool_name") != "Bash":
        return None
    command = payload.get("tool_input", {}).get("command", "")
    for pattern, action in GUARDED:
        if pattern.search(command):
            return action
    return None


def run_pre(stdin=None, stdout=None):
    stdin, stdout = stdin or sys.stdin, stdout or sys.stdout
    payload = json.load(stdin)
    if FORCE_PUSH_MAIN.search(payload.get("tool_input", {}).get("command", "")):
        print(decision("deny", "Force push na main é proibido, com ou sem mandato."), file=stdout)
        return 0
    action = guarded_action(payload)
    if not action or mandate_is_valid() or window_is_open(payload.get("session_id")):
        return 0
    minutes = WINDOW_SECONDS // 60
    print(decision("ask", f"Aprovar {action}? Vale também para {OTHERS[action]} nos próximos {minutes} min."),
          file=stdout)
    return 0


def run_post(stdin=None):
    payload = json.load(stdin or sys.stdin)
    if guarded_action(payload):
        marker(payload.get("session_id")).write_text(str(time.time()), encoding="utf-8")
    return 0


def main(argv):
    if argv[:1] == ["--post"]:
        return run_post()
    return run_pre()


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
