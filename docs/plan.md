# Project plan & working notes

Read this first when picking the project back up (human or AI).
Last updated: 2026-11-10

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
| All `@ManyToOne` are LAZY. Build DTOs inside `@Transactional` service methods. | Avoids loading whole parent chains. Lazy fields can't be read after the transaction ends. |
| Card numbers in a separate `member_card` table (V2) | A member can have several cards, and cards get replaced. Unique per user, not globally. |
| `transaction.card_last4` (nullable) is kept even when no member is assigned | Show "•••• 1234" for unknown cards. Back-fill `member_id` with one query once the card is assigned. Unassigned cards are asked about again on each import. |
| Categories are seeded by a Flyway migration (V3). Names are unique per parent. | Every database (local, tests, VM) gets the same tree. |
| Database is Neon (managed Postgres), not a container on the VM | Keeps the e2-micro's 1 GB for the app. |

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

## Category tree (seeded in V3)

```
Living         Rent · Power · Wifi · Mobile
Food           Grocery · Outside Food · Alcohol
Shopping       Household · Personal
Car            Petrol · Car Insurance · Rego · WOF
Lifestyle      Travel · Activity · Social · Education
Health         Health Insurance · Treatment · Beauty
Not spending   Income · Transfer · Refund
Uncategorised
```

"Not spending" keeps bank credits out of spending totals. "Uncategorised" holds rows that match no merchant.

## Bug fixes (found and fixed 2026-11-08)

Status: fixed in code and committed. The code compiles, but `./gradlew test` hasn't run yet because Docker wasn't running.

- [x] `.github/workflow/` renamed to `.github/workflows/` so CI triggers.
- [x] Added `spring-boot-starter-flyway` + `flyway-database-postgresql` so V1 runs at startup.
- [x] Added `spring-boot-testcontainers`, `testcontainers-postgresql`, `testcontainers-junit-jupiter`, and `@Testcontainers` on the test class.
- [x] `V1__init.sql`: tables reordered (app_user, member, category, merchant, merchant_alias, transaction).
- [x] `V1__init.sql`: `transaction` columns now match the entity: `user_id → app_user`, `member_id → member`, `raw_type`, `amount_cents BIGINT`, `occurrence_no`, and `UNIQUE (user_id, raw_line, occurrence_no)`.
- [x] `member.card_last4` changed from `CHAR(4)` to `VARCHAR(4)` with `CHECK (^[0-9]{4}$)`, because Hibernate validation rejects CHAR for a String. (Moved to `member_card` in V2.)
- [x] `AppUser`: fields `username` and `passwordHash` match the columns. `accountId` removed.
- [x] `Transaction.user` added, so `AppUser.transactions` (`mappedBy="user"`) works.
- [x] `AppUserRepository.findByUsername` returns `Optional<AppUser>`.
- [x] `@CreationTimestamp` / `@UpdateTimestamp` in all entities. Without them Hibernate inserted NULL into the NOT NULL timestamp columns.
- [x] `@ManyToOne` is LAZY everywhere. `findByParentId` no longer has an unneeded `@Param`.
- [x] README: Gradle and the VM deploy, plus Flyway and Testcontainers rows.

### Still to verify
- [ ] `./gradlew test` passes, which runs Flyway V1–V3, Hibernate `validate`, and 14 tests (`BudgettrackerApplicationTests`, `CategoryRepositoryTest`, `MemberCardRepositoryTest`). Watch for `Instant` ↔ `TIMESTAMPTZ`.
- [ ] Neon runs PostgreSQL 15+ (V3 uses `UNIQUE NULLS NOT DISTINCT`).
- [ ] The VM's `/opt/budgettracker/docker-compose.yml` matches `deploy/docker-compose.yml`, and the VM `.env` has `DB_URL`, `DB_USER`, `DB_PASSWORD`.

### Known gaps (not fixed yet)
- [ ] Local run: a `.env` exists (gitignored), but Spring Boot doesn't read `.env` by itself. It works when something loads it (the VS Code launch config's `envFile`, or `docker compose`). `./gradlew bootRun` won't see it.
- [x] Deploy docs: `deploy/docker-compose.yml` and `deploy/.env.example` are in the repo, and the VM setup steps are in `docs/infra.md`.

## Roadmap (epics)

- **E0 Foundation & CI**: ~~fix the bugs~~, ~~`member_card` table (V2)~~, ~~repository tests written~~, ~~deploy docs~~ (done). Left: run `./gradlew test`, local run profile, confirm the first deploy.
- **E1 Categories**: ~~tree, seed migration, `UNIQUE(parent_id, name)` (V3)~~ (done). Left: `CategoryService`, `GET /api/categories`.
- **E2 CSV import**: sample CSV fixtures, a parser for each bank format (use a CSV library), line numbering, hashing, occurrence counting, staging tables, upload endpoint.
- **E3 Merchant matching**: description normaliser, alias matcher (exact match, then longest prefix), merchant/alias admin, learning from corrections.
- **E4 Review & save**: preview page, row editing, duplicate detection, continuity check, idempotent save.
- **E5 Reports**: transaction list with category and subcategory filters, weekly summary like the spreadsheet, Goal/Net with a budget-goal history table, weekly normalisation of recurring payments.
- **E6 Accounts & security**: single-user mode first, then Spring Security with BCrypt. HTTPS on the VM before any real bank data goes on it.
- **E7 Move to React**: replace `web/view` and the templates only.

Planning board (private): https://claude.ai/artifact/P9GeXPsDUbH64YWBnGSXnR

## Open questions

- Which bank's CSV format comes first?
- Is the occurrence-number rule above what was meant by checking that lines are continuous?
- Should categories be shared by all users, or should each user have their own?