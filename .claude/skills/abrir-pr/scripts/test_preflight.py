import unittest

import preflight

READY = {"branch": "42-back-baixa-de-gravame", "dirty": "", "behind_main": False, "open_prs": []}


def problems(**changes):
    return preflight.problems({**READY, **changes})


class PreflightTest(unittest.TestCase):
    def test_ready_branch_has_no_problems(self):
        self.assertEqual(problems(), [])

    def test_main_branch_is_refused(self):
        self.assertEqual(len(problems(branch="main")), 1)

    def test_uncommitted_changes_are_refused(self):
        self.assertEqual(len(problems(dirty=" M backend/build.gradle.kts")), 1)

    def test_branch_behind_main_is_refused(self):
        self.assertEqual(len(problems(behind_main=True)), 1)

    def test_another_open_pr_is_refused(self):
        found = problems(open_prs=[{"number": 7, "headRefName": "outra-branch"}])
        self.assertEqual(len(found), 1)
        self.assertIn("#7", found[0])

    def test_all_problems_are_reported_together(self):
        self.assertEqual(len(problems(branch="main", dirty="?? x", behind_main=True,
                                      open_prs=[{"number": 7, "headRefName": "y"}])), 4)


if __name__ == "__main__":
    unittest.main()
