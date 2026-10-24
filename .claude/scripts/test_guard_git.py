import io
import json
import time
import unittest
import uuid

import guard_git


def payload(command, session):
    return {"tool_name": "Bash", "tool_input": {"command": command}, "session_id": session}


def pre(command, session):
    stdout = io.StringIO()
    guard_git.run_pre(io.StringIO(json.dumps(payload(command, session))), stdout)
    return stdout.getvalue()


def post(command, session):
    guard_git.run_post(io.StringIO(json.dumps(payload(command, session))))


class GuardGitTest(unittest.TestCase):
    def setUp(self):
        self.session = f"test-{uuid.uuid4()}"

    def tearDown(self):
        guard_git.marker(self.session).unlink(missing_ok=True)

    def test_commit_push_and_pr_ask_for_confirmation(self):
        for command in ("git commit -m x", "git -C repo push", "gh pr create --title x"):
            decision = json.loads(pre(command, self.session))["hookSpecificOutput"]["permissionDecision"]
            self.assertEqual(decision, "ask", command)

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


if __name__ == "__main__":
    unittest.main()
