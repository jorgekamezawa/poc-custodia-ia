import io
import json
import unittest

import guard_readonly_git


def pre(command):
    stdout = io.StringIO()
    payload = {"tool_name": "Bash", "tool_input": {"command": command}}
    guard_readonly_git.run_pre(io.StringIO(json.dumps(payload)), stdout)
    return stdout.getvalue()


class GuardReadonlyGitTest(unittest.TestCase):
    def test_git_read_commands_pass_silently(self):
        for command in ("git diff main...HEAD", "git log --oneline -5", "git show HEAD:README.md",
                        "git status", "  git diff --stat -- backend/"):
            self.assertEqual(pre(command), "", command)

    def test_other_commands_are_denied(self):
        for command in ("git push", "git commit -m x", "git checkout main", "rm -rf build",
                        "./gradlew build", "gh pr create"):
            decision = json.loads(pre(command))["hookSpecificOutput"]["permissionDecision"]
            self.assertEqual(decision, "deny", command)

    def test_read_command_chained_with_another_is_denied(self):
        for command in ("git diff; rm x", "git diff && git push", "git log | sh",
                        "git diff > out.txt", "git show $(rm x)", "git show `rm x`", "git diff\nrm x"):
            self.assertNotEqual(pre(command), "", command)


if __name__ == "__main__":
    unittest.main()
