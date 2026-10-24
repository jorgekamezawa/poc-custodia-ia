import io
import json
import tempfile
import time
import unittest
import uuid
from datetime import date, timedelta
from pathlib import Path

import guard_git


def payload(command, session):
    return {"tool_name": "Bash", "tool_input": {"command": command}, "session_id": session}


def decision(output):
    return json.loads(output)["hookSpecificOutput"]["permissionDecision"] if output else ""


def pre(command, session):
    stdout = io.StringIO()
    guard_git.run_pre(io.StringIO(json.dumps(payload(command, session))), stdout)
    return stdout.getvalue()


def post(command, session):
    guard_git.run_post(io.StringIO(json.dumps(payload(command, session))))


class GuardGitTest(unittest.TestCase):
    def setUp(self):
        self.session = f"test-{uuid.uuid4()}"
        self.tmp = tempfile.TemporaryDirectory()
        self.original_mandate = guard_git.MANDATE
        guard_git.MANDATE = Path(self.tmp.name) / "mandato.json"

    def tearDown(self):
        guard_git.marker(self.session).unlink(missing_ok=True)
        guard_git.MANDATE = self.original_mandate
        self.tmp.cleanup()

    def mandate(self, until):
        guard_git.MANDATE.write_text(json.dumps({"ate": until.isoformat(), "escopo": "x"}), encoding="utf-8")

    def test_commit_push_and_pr_ask_for_confirmation(self):
        for command in ("git commit -m x", "git -C repo push", "gh pr create --title x"):
            self.assertEqual(decision(pre(command, self.session)), "ask", command)

    def test_other_commands_pass_silently(self):
        for command in ("git status", "git log --oneline", "gh pr view 1", "echo commit"):
            self.assertEqual(pre(command, self.session), "", command)

    def test_approved_action_opens_the_window_for_the_others(self):
        post("git commit -m x", self.session)
        self.assertEqual(pre("git push", self.session), "")
        self.assertEqual(pre("gh pr create --title x", self.session), "")

    def test_window_is_per_session(self):
        post("git commit -m x", self.session)
        other = f"test-{uuid.uuid4()}"
        self.assertNotEqual(pre("git push", other), "")

    def test_window_closes_after_five_minutes(self):
        post("git commit -m x", self.session)
        later = time.time() + guard_git.WINDOW_SECONDS + 1
        self.assertFalse(guard_git.window_is_open(self.session, now=later))

    def test_valid_mandate_lets_publishing_pass(self):
        self.mandate(date.today())
        for command in ("git commit -m x", "git push -u origin x", "gh pr create --title x"):
            self.assertEqual(pre(command, self.session), "", command)

    def test_expired_mandate_asks_again(self):
        self.mandate(date.today() - timedelta(days=1))
        self.assertEqual(decision(pre("git commit -m x", self.session)), "ask")

    def test_unreadable_mandate_asks(self):
        guard_git.MANDATE.write_text("{quebrado", encoding="utf-8")
        self.assertEqual(decision(pre("git commit -m x", self.session)), "ask")

    def test_force_push_to_main_is_denied_even_with_mandate(self):
        self.mandate(date.today())
        for command in ("git push --force origin main", "git push -f origin main", "git push origin +main"):
            self.assertEqual(decision(pre(command, self.session)), "deny", command)

    def test_force_push_to_a_task_branch_follows_the_mandate(self):
        self.mandate(date.today())
        self.assertEqual(pre("git push --force-with-lease origin 31-back-contrato", self.session), "")

    def test_direct_merge_is_denied_with_or_without_mandate(self):
        output = pre("gh pr merge 3 --squash --delete-branch", self.session)
        self.assertEqual(decision(output), "deny")
        self.assertIn("merge_when_green.py", json.loads(output)["hookSpecificOutput"]["permissionDecisionReason"])
        self.mandate(date.today())
        self.assertEqual(decision(pre("gh pr merge 3", self.session)), "deny")

    def test_command_named_inside_a_message_is_not_blocked(self):
        command = "git commit -q -F - <<'EOF'\nHook nega `gh pr merge` direto e pkill -f\nEOF"
        self.assertNotEqual(decision(pre(command, self.session)), "deny")

    def test_merge_chained_after_another_command_is_denied(self):
        self.assertEqual(decision(pre("cd repo && gh pr merge 3", self.session)), "deny")

    def test_merge_through_the_script_is_not_blocked(self):
        self.mandate(date.today())
        self.assertEqual(pre("python3 .claude/skills/abrir-pr/scripts/merge_when_green.py", self.session), "")

    def test_pkill_by_pattern_is_denied(self):
        output = pre("pkill -f CustodyApplication", self.session)
        self.assertEqual(decision(output), "deny")
        self.assertIn("porta", json.loads(output)["hookSpecificOutput"]["permissionDecisionReason"])

    def test_kill_by_pid_is_not_blocked(self):
        self.assertEqual(pre("kill 12345", self.session), "")


if __name__ == "__main__":
    unittest.main()
