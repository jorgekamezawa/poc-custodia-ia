import unittest

import backlog

PLAN = {
    "milestones": [{"title": "3. Construção"}],
    "epics": [
        {"milestone": "3. Construção", "title": "Baixa de gravame",
         "stories": [{"title": "Dar baixa no gravame após quitação"}]},
        {"milestone": "2. Discovery e backlog", "title": "Visão da área"},
    ],
}


class BacklogTest(unittest.TestCase):
    def test_plan_with_known_milestones_is_valid(self):
        self.assertEqual(backlog.validate(PLAN, existing={"2. Discovery e backlog": 1}), [])

    def test_epic_with_unknown_milestone_is_reported(self):
        errors = backlog.validate(PLAN, existing={})
        self.assertEqual(len(errors), 1)
        self.assertIn("Visão da área", errors[0])

    def test_epic_and_story_without_title_are_reported(self):
        plan = {"epics": [{"milestone": "3. Construção", "title": "", "stories": [{"title": " "}]}]}
        self.assertEqual(len(backlog.validate(plan, existing={"3. Construção": 1})), 2)

    def test_summary_lists_issues_in_plan_order(self):
        lines = backlog.summary(PLAN, existing={"2. Discovery e backlog": 1})
        self.assertEqual(lines, [
            "criar milestone: 3. Construção",
            "criar épico [3. Construção]: Baixa de gravame",
            "  criar história: Dar baixa no gravame após quitação",
            "criar épico [2. Discovery e backlog]: Visão da área",
            "total: 3 issues",
        ])

    def test_summary_skips_existing_milestone(self):
        lines = backlog.summary(PLAN, existing={"3. Construção": 4, "2. Discovery e backlog": 1})
        self.assertEqual(lines[0], "já existe milestone: 3. Construção")


if __name__ == "__main__":
    unittest.main()
