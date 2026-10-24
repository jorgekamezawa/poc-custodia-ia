import io
import json
import subprocess
import unittest

import guard_format


class FakeRunner:
    """Answers git with the staged files and each formatter with the given exit code."""

    def __init__(self, staged, java_ok=True, front_ok=True):
        self.staged, self.java_ok, self.front_ok = staged, java_ok, front_ok
        self.calls = []

    def __call__(self, command, cwd):
        self.calls.append(command)
        if command[0] == "git":
            return subprocess.CompletedProcess(command, 0, "\n".join(self.staged), "")
        ok = self.java_ok if command[0] == "./gradlew" else self.front_ok
        return subprocess.CompletedProcess(command, 0 if ok else 1, "", "")


def decision(command, runner):
    payload = {"tool_name": "Bash", "tool_input": {"command": command}}
    stdout = io.StringIO()
    guard_format.run_pre(io.StringIO(json.dumps(payload)), stdout, runner)
    return stdout.getvalue()


class GuardFormatTest(unittest.TestCase):
    def test_unformatted_java_denies_the_commit(self):
        out = json.loads(decision("git commit -m x", FakeRunner(["backend/src/A.java"], java_ok=False)))
        self.assertEqual(out["hookSpecificOutput"]["permissionDecision"], "deny")
        self.assertIn("spotlessApply", out["hookSpecificOutput"]["permissionDecisionReason"])

    def test_unformatted_frontend_denies_the_commit(self):
        out = json.loads(decision("git commit -m x", FakeRunner(["frontend/src/app/a.ts"], front_ok=False)))
        self.assertIn("npm run format", out["hookSpecificOutput"]["permissionDecisionReason"])

    def test_formatted_code_passes(self):
        runner = FakeRunner(["backend/src/A.java", "frontend/src/app/a.ts"])
        self.assertEqual(decision("git commit -m x", runner), "")

    def test_docs_only_commit_runs_no_formatter(self):
        runner = FakeRunner(["docs/adr/0001-x.md"], java_ok=False, front_ok=False)
        self.assertEqual(decision("git commit -m x", runner), "")
        self.assertEqual([c[0] for c in runner.calls], ["git"])

    def test_frontend_check_receives_only_staged_frontend_files(self):
        runner = FakeRunner(["frontend/src/app/a.ts", "frontend/README.md", "backend/x.txt"])
        decision("git commit -m x", runner)
        self.assertEqual(runner.calls[-1], ["npx", "prettier", "--check", "src/app/a.ts"])

    def test_other_commands_are_ignored(self):
        runner = FakeRunner(["backend/src/A.java"], java_ok=False)
        self.assertEqual(decision("git status", runner), "")
        self.assertEqual(runner.calls, [])


if __name__ == "__main__":
    unittest.main()
