# Household Budget Tracker

A personal budget tracker, rebuilt from a spreadsheet into a web app. I design it and review every change; an AI coding assistant writes much of the code under that direction.

## Motivation

I've been tracking my household budget in a spreadsheet for a while now, with weekly rows and category columns. This project moves that into a proper web app: upload a bank CSV export, check the suggested categories, and save it without creating duplicates.

There's also a second reason behind this project. AI coding tools have made it easy to ship features fast without fully understanding what's being built — "vibe coding," where you accept whatever the AI outputs without stopping to ask why. This project uses AI (Claude Code), but on a short leash:

- Every design decision (table structure, duplicate rule, category tree) is discussed and decided by me before any code is written.
- The AI shows each change as a diff and explains why; nothing lands in the repo without my explicit approval.
- I review and commit every change myself, one small step at a time.
- Decisions, bugs and the roadmap are recorded in [docs/plan.md](docs/plan.md).

The goal isn't to avoid AI. It's to make sure I still understand every line of my own code.

## Status

🚧 Foundation done — database schema, migrations, tests and CI/CD are working, and the app is deployed. Currently building **CSV import** (ANZ NZ format first).

## Tech Stack

- **Language**: Java 25 (LTS)
- **Framework**: Spring Boot 4.1.1
- **Build tool**: Gradle 9.7.1
- **Database**: PostgreSQL 18 (Neon in production, Testcontainers in tests)
- **Frontend**: Thymeleaf pages planned first, React later (the REST API is kept separate for that)
- **Packaging**: JAR in a Docker image

### Dependencies

| Dependency | Why |
|---|---|
| Spring Web | REST API and server-rendered pages |
| Spring Data JPA | Entity-Repository data access |
| PostgreSQL Driver | Relational DB, well suited for transaction history |
| Flyway | Versioned database migrations (`db/migration/V*.sql`), applied at startup |
| Validation | Rejects invalid input at the DTO level |
| Lombok | Removes boilerplate (getters/setters/constructors) |
| Spring Boot Configuration Processor | Autocomplete for custom `application.yml` properties |
| SpringDoc OpenAPI | Auto-generated API documentation |
| Spring Boot Actuator | Health-check endpoint used by the deploy step |
| Spring Boot DevTools | Auto-restart on code changes during development |
| Testcontainers | Runs a real PostgreSQL in Docker for tests (some queries are Postgres-only) |

## Key Design Decisions

Full notes in [docs/plan.md](docs/plan.md).

**Built**
- **Two-level category tree** (group → item, e.g. Food → Grocery), seeded by migration, with duplicate names blocked per parent.
- **Duplicate-safe imports**: each CSV line is stored as-is with an occurrence number (the 1st, 2nd… identical line in a file), so re-uploading the same export never creates duplicates, while two genuinely identical purchases on the same day are both kept.
- **Cards belong to members**: the last 4 digits of the card in each row are mapped to a household member, so spending can be shown per person.
- **Money as integer cents**, never floating point.
- **Thin controllers, logic in services, DTOs out**: so the Thymeleaf pages can be replaced by React without touching the backend.

**Planned**
- **Merchant matching**: bank descriptions are truncated (`Pak N Save F`, `Pak N Save M`), so aliases match by longest prefix, and corrections teach new aliases.
- **Weekly normalisation** of recurring payments (insurance, subscriptions), because the original spreadsheet worked in weeks.
- **Price history** for recurring payments, so a price increase doesn't distort past budgets.

## Deployment

GCP Compute Engine VM (e2-micro) running Docker Compose, with the database on Neon. On every push to `main`, GitHub Actions runs the tests, builds a Docker image, pushes it to GitHub Container Registry and deploys it to the VM over SSH, then checks `/actuator/health`. Setup: [docs/infra.md](docs/infra.md).

## License

Personal project — not currently licensed for reuse.
