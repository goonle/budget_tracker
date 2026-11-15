# Budget Tracker

## Overview
A personal budget tracker, rebuilt from a spreadsheet into a web app.
The owner designs every feature and approves every change; Claude writes code only under that direction.
Decisions, bugs and roadmap: @docs/plan.md

## Working rules
- Never change a repo file without permission: show the exact code/diff first, explain why, wait for "yes".
- Small steps: one feature piece at a time; approve one file at a time if asked.
- Explain a command before running it.
- Never commit or push. The owner commits and pushes.

## Commands
- Build: ./gradlew build
- Test: ./gradlew cleanTest test (Docker must be running; report at build/reports/tests/test/index.html)

## Architecture rules
- Controller > Service > Repository > DB. Controllers stay thin; logic lives in services.
- Entity = DB table, DTO (record) = return object. Build DTOs inside @Transactional services.
- web/api = REST (kept), web/view = Thymeleaf (replaced by React later).
- All @ManyToOne are LAZY. Money is long cents, parsed with BigDecimal.
- Never edit an applied Flyway migration; add a new V*.sql.

## Things to avoid
- Don't create too much functionality: no methods, classes or options that aren't needed yet (YAGNI).
- Never open or read .env (contains passwords).
- sample/ holds real bank exports: never commit, never copy real card/account numbers into tests or docs.
  Test fixtures in src/test/resources/csv/ must be anonymized.