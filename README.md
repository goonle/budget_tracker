# Household Budget Tracker

A personal budget tracker, rebuilt from a spreadsheet into a web app — core logic hand-coded and deliberately designed, with AI used only for boilerplate and review.

## Motivation

I've been tracking my household budget in a spreadsheet for a while now, with weekly rows and category columns. This project moves that into a proper web app.

There's also a second reason behind this project. AI coding tools have made it easy to ship features fast without fully understanding what's being built — "vibe coding," where you accept whatever the AI outputs without stopping to ask why. This project is a deliberate step in the other direction: every design decision (category structure, how recurring payments are normalized, how price changes are tracked over time) is reasoned through before it's implemented, and the core logic is written by hand. AI is used where it actually makes sense — boilerplate, code review, looking up unfamiliar APIs — not as a substitute for understanding my own code.

The goal isn't to prove I can avoid AI. It's to make sure I still know what I'm doing without it.

## Status

🚧 Project setup phase — repository initialized, tech stack and dependencies finalized. Implementation in progress.

## Tech Stack

- **Language**: Java 25 (LTS)
- **Framework**: Spring Boot 4.1.1
- **Build tool**: Maven
- **Database**: PostgreSQL
- **Packaging**: JAR (standalone, embedded Tomcat)

### Dependencies

| Dependency | Why |
|---|---|
| Spring Web | Core for building the REST API — Controller layer via MVC pattern |
| Spring Data JPA | Abstracts the DB access layer via Entity-Repository pattern |
| PostgreSQL Driver | Relational DB, well suited for transaction history tracking |
| Validation | Rejects invalid input (e.g. negative amounts) at the DTO level |
| Lombok | Removes boilerplate (getters/setters/constructors) |
| Spring Boot Configuration Processor | Autocomplete for custom `application.yml` properties |
| SpringDoc OpenAPI | Auto-generated API documentation |
| Spring Boot Actuator | Health-check endpoints for container monitoring (Cloud Run) |
| Spring Boot DevTools | Auto-restart on code changes during development |

## Key Design Decisions

A few decisions worth calling out up front (full rationale for each will live in `docs/adr/` as the project progresses):

- Recurring payments (insurance, subscriptions, gym membership) are normalized to a **weekly equivalent**, not monthly — the original spreadsheet tracked budget on a weekly basis, and normalizing to weeks keeps that consistent.
- Categories separate **what was bought** (Groceries vs. Outside Food) from **why it was bought** (Essentials vs. Fashion) rather than forcing everything into a single "Luxury" catch-all, which made it hard to tell what was actually driving budget overruns.
- Recurring payment amounts are tracked with a **history table**, so a price increase (e.g. gym membership going up) doesn't silently distort past budget calculations.
- Frequently repeated transactions (same merchant) can be auto-categorized via a **merchant mapping table**, learned from past manual categorization — reducing repetitive manual entry without needing fuzzy matching.

## Deployment (planned)

GCP Cloud Run + GitHub Actions + Workload Identity Federation + Secret Manager.

## License

Personal project — not currently licensed for reuse.
