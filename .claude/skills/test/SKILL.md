---
name: test
description: Run the project's tests and explain the results. Use when the owner types /test or asks to run the tests.
---

# Run tests

Typing /test is permission to run the commands below. Nothing else.

1. Check Docker is running: `docker info`. If it isn't, stop and tell the owner to start Docker Desktop
   (the repository tests need Testcontainers).
2. Run the tests, without the cache:
   `./gradlew cleanTest test`
   If `java` is not on PATH, set JAVA_HOME to the JDK in CLAUDE.local.md for this command.
   An optional argument limits the run, e.g. `/test CategoryServiceTest` → `--tests '*CategoryServiceTest'`.
3. Report:
   - Total, passed, failed, skipped (from `build/test-results/test/*.xml`).
   - For each failure: test name, what was expected vs. what happened, and the likely cause in plain words.
     Read the XML for the stack trace; quote only the important lines.
   - Where to look: `build/reports/tests/test/index.html`.
4. Do not fix anything. If a fix is obvious, describe it and wait for permission.
5. Answer in the language the owner used.
