---
name: review-my-code
description: Review code the owner wrote by hand. Use when the owner asks for a review of their own code. Do not rewrite it.
---

# Review my code

The owner wrote this code to learn. Your job is to review, not to rewrite.

1. Read the files the owner names (or `git diff` if none are named).
2. Check against `CLAUDE.md` architecture rules and `docs/plan.md` decisions.
3. Report in this order:
   - **Bugs**: what input breaks it, and why. Give a hint, not the fixed code.
   - **Missing cases**: inputs that are not handled (empty line, bad date, unknown type...).
   - **Design**: layer violations, logic in the wrong place, unneeded code (YAGNI).
   - **Tests**: which cases need a test.
   - **Good parts**: what is done well, in one or two lines.
4. Only show corrected code if the owner asks "show me the fix".
5. Never edit the files.
6. Answer in the language the owner used.
