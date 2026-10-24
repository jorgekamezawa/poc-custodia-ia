"""Merges the pull request of the current branch only after its CI checks exist and all pass.

Usage:
  merge_when_green.py    waits for the checks, prints them, merges or exits 1

`gh pr checks --watch` returns at once when the checks have not been created yet,
so merging right after it can merge before CI ran. This waits for them to exist.
"""

import subprocess
import sys
import time

WAIT_FOR_CHECKS_SECONDS = 300
POLL_SECONDS = 10


def gh(*args):
    return subprocess.run(["gh", *args], capture_output=True, text=True)


def checks_exist():
    out = gh("pr", "checks")
    return "no checks reported" not in (out.stdout + out.stderr)


def main():
    deadline = time.time() + WAIT_FOR_CHECKS_SECONDS
    while not checks_exist():
        if time.time() > deadline:
            print("Sem checagens no PR: não mergeado.")
            return 1
        time.sleep(POLL_SECONDS)
    gh("pr", "checks", "--watch", "--interval", "20")
    result = gh("pr", "checks")
    print(result.stdout.strip())
    if result.returncode != 0:
        print("CI vermelho ou incompleto: não mergeado.")
        return 1
    merged = gh("pr", "merge", "--squash", "--delete-branch")
    if merged.returncode != 0:
        print(merged.stderr.strip())
        return 1
    print("Mergeado.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
