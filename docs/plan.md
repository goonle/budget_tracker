# Project plan & working notes

Read this first when picking the project back up (human or AI).
Last updated: 2026-11-08

## Working rules

- The user writes the core logic. AI is for boilerplate, review and planning.
- **Never create or change code without asking first.** Propose the change, explain why, wait for a yes.
- Commit or push only when asked.
- Every method gets a test.

## What the app does

1. The user downloads a transaction history CSV for a period from the bank website.
2. They upload it to the web app.
3. The backend gives each line a line number, maps it to a merchant through `MerchantAlias`, and suggests the merchant's default category.
4. A preview page lists the rows. The user checks them, fixes wrong categories, filters and sorts by category.
5. On save, the backend checks for duplicates and for continuity with data that's already saved.

## Architecture decisions

| Decision | Reason |
|---|---|
| Thymeleaf now with plain forms (no htmx), React later | Learn the backend first. Plain forms keep the throwaway code small. |
| Thin controllers. All logic lives in services. | Switching to React then only replaces the controllers and templates. |
| Services return DTOs, never entities | The same DTO works for Thymeleaf and later for React's JSON. It also avoids lazy-loading errors and JSON loops (`Category.children` → `parent`). |
| Packages `web/view` (Thymeleaf, replaced later) and `web/api` (JSON, kept) | Both call the same services. |
| React later is built into Spring `static/` (one jar, same domain) | No CORS, the session login keeps working, and it fits the Docker/VM deploy. |
| Postgres via Testcontainers for repository tests, not H2 | `CategoryRepository` uses recursive CTEs and `ARRAY`, which only Postgres supports. |
| Money is a `long` in cents. Parse with `BigDecimal`, never `double`. | No rounding errors. |

### Test conventions

- Services and parsers: JUnit 5 + AssertJ + Mockito unit tests.
- Repositories: `@DataJpaTest` + Testcontainers Postgres.
- Controllers: `@WebMvcTest`.
- One test class per class. Name tests `method_condition_expected`.

## Import and duplicate rules

- `line_no`: the physical line number in the uploaded file, set by the backend.
- `raw_line_hash`: SHA-256 of the normalised raw line (trimmed, spaces collapsed).
- `occurrence_no`: the nth time this exact normalised line appears in the file. Two identical $5 coffees on the same day are 1 and 2, and both are kept.
- Duplicate: `(user_id, raw_line_hash, occurrence_no)` already exists. A unique index enforces this in the database. Check on preview and again on save.
- Staging tables `import_batch` and `import_row` keep raw lines on the server so the browser can't change them. Batch status is PREVIEW, SAVED or DISCARDED.
- Continuity check:
  - Warn on a date gap between the last saved date and the file's first date.
  - Warn when the date ranges overlap but no rows matched (different account or changed bank format).
  - Report gaps in `line_no`, which are lines that failed to parse, so none are dropped silently.

## Proposed category tree (not final)

```
Housing        Rent · Utilities (Water, Gas & Electricity) · Internet · Mobile
Food           Groceries · Eating out · Alcohol
Transport      Fuel · Car insurance · Car maintenance · Parking & tolls · Public transport
Shopping       Household · Clothing · Big purchases
Leisure        Hobbies (Lego) · Travel · Activities · Social
Health & care  Gym · Health insurance · Medical · Beauty
Growth         Education
Not spending   Income · Transfer between own accounts · Refund
Uncategorised
```

Changes from the spreadsheet:
- The two "Insurance" columns are split by parent.
- "Daily" and "Luxury" describe why you bought something, not what it was, so they became Household and Big purchases.
- "Not spending" keeps bank credits out of spending totals.

## Known bugs (found 2026-11-08)

- [ ] `.github/workflow/` must be `.github/workflows/`. CI never triggers.
- [ ] Flyway is missing from `build.gradle` (`spring-boot-starter-flyway` + `flyway-database-postgresql`). With `ddl-auto: validate` the app won't start on an empty database.
- [ ] `V1__init.sql`: `transaction` references `merchant` before it exists. Table order should be app_user, member, category, merchant, merchant_alias, transaction.
- [ ] `V1__init.sql` doesn't match the entities:
  - transaction: `user_id`/`type`/`cent INT` versus `member_id`/`rawType`/`amountCents long`/`occurrenceNo`.
  - app_user: `username`/`password_hash` versus `accountId`/`hashPassword`/`userName`.
- [ ] `AppUser.transactions` uses `mappedBy="user"`, but `Transaction` has no `user` field. Hibernate fails at boot.
- [ ] `AppUserRepository.findByUsername` doesn't match the field `userName`.
- [ ] Testcontainers dependencies are missing (`spring-boot-testcontainers`, `testcontainers-postgresql`, `testcontainers-junit-jupiter`). The test class also needs `@Testcontainers`.
- [ ] The README says Maven and Cloud Run. The project actually uses Gradle and a GCP VM with docker compose.

## Roadmap (epics)

- **E0 Foundation & CI**: fix the bugs above. Write tests for the 4 `CategoryRepository` methods. First deploy dry run: VM compose file and `.env`, GHCR login, JVM memory limit for e2-micro.
- **E1 Categories**: finalise the tree, seed migration, `UNIQUE(parent_id, name)`, `CategoryService`, `GET /api/categories`.
- **E2 CSV import**: sample CSV fixtures, a parser for each bank format (use a CSV library), line numbering, hashing, occurrence counting, staging tables, upload endpoint.
- **E3 Merchant matching**: description normaliser, alias matcher (exact match, then longest prefix), merchant/alias admin, learning from corrections.
- **E4 Review & save**: preview page, row editing, duplicate detection, continuity check, idempotent save.
- **E5 Reports**: transaction list with category and subcategory filters, weekly summary like the spreadsheet, Goal/Net with a budget-goal history table, weekly normalisation of recurring payments.
- **E6 Accounts & security**: single-user mode first, then Spring Security with BCrypt. Make `card_last4` unique per user. HTTPS on the VM before any real bank data goes on it.
- **E7 Move to React**: replace `web/view` and the templates only.

Planning board (private): https://claude.ai/artifact/P9GeXPsDUbH64YWBnGSXnR

## Open questions

- Which bank's CSV format comes first?
- Is the occurrence-number rule above what was meant by checking that lines are continuous?
- Should categories be shared by all users, or should each user have their own?
