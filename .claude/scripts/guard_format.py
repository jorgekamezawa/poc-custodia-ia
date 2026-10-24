"""Blocks a commit whose staged backend or frontend files are not formatted.

Usage:
  guard_format.py --pre   Claude Code PreToolUse hook (reads the tool call JSON on stdin)

The formatter is a machine decision, so the commit is denied instead of asked:
running one command fixes it. Each stack is checked only when one of its files is
staged, which keeps a documentation commit free of it.
"""

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

SEGMENT = r"[^;&|\n]*"
COMMIT = re.compile(rf"\bgit\b{SEGMENT}\bcommit\b")
FRONTEND_SUFFIXES = (".ts", ".html", ".scss", ".css", ".json")


def is_commit(payload):
    if payload.get("tool_name") != "Bash":
        return False
    return bool(COMMIT.search(payload.get("tool_input", {}).get("command", "")))


def run(command, cwd):
    return subprocess.run(command, cwd=cwd, capture_output=True, text=True, check=False)


def staged_files(runner=run):
    result = runner(["git", "diff", "--cached", "--name-only", "--diff-filter=ACMR"], ROOT)
    return result.stdout.splitlines()


def problems(files, runner=run):
    found = []
    if any(f.startswith("backend/") and f.endswith(".java") for f in files):
        if runner(["./gradlew", "spotlessCheck", "-q"], ROOT / "backend").returncode != 0:
            found.append("Java fora do formato: rode ./gradlew spotlessApply em backend/")
    front = [f[len("frontend/"):] for f in files if f.startswith("frontend/") and f.endswith(FRONTEND_SUFFIXES)]
    if front and runner(["npx", "prettier", "--check", *front], ROOT / "frontend").returncode != 0:
        found.append("Frontend fora do formato: rode npm run format em frontend/")
    return found


def run_pre(stdin=None, stdout=None, runner=run):
    stdin, stdout = stdin or sys.stdin, stdout or sys.stdout
    payload = json.load(stdin)
    if not is_commit(payload):
        return 0
    found = problems(staged_files(runner), runner)
    if found:
        print(json.dumps({"hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": "; ".join(found) + ". Depois, comite de novo.",
        }}, ensure_ascii=False), file=stdout)
    return 0


if __name__ == "__main__":
    sys.exit(run_pre())
