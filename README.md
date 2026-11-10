# Tenge Tracker API

A Java 17 / Spring Boot REST API for tracking accounts, categories, budgets and financial transactions (4 related entities, 25 endpoints). PostgreSQL is the database; Docker Compose runs the backend and database on a private Compose network and persists database data in the named `postgres_data` volume.

## Requirements
- Docker Desktop (or Docker Engine) with the Compose plugin
- Postman (for the demonstration)

## Run it
1. Extract the project folder and open a terminal in it.
2. Optional: create a `.env` file to override defaults (`POSTGRES_PASSWORD` must match `DB_PASS`). Without one, `docker-compose.yaml` uses working defaults.
3. On first startup, PostgreSQL runs `db/init/01-seed.sql` automatically. It creates the tables (with foreign keys) and inserts 5 demo accounts, 19 demo transactions, 15 categories, and 5 budgets, so the API is populated immediately.
4. Start the complete application:
   ```bash
   docker compose up --build
   ```
5. Wait until the backend logs show that Spring Boot has started. The API base URL is `http://localhost:3000`.
6. Open Swagger UI at `http://localhost:3000/swagger-ui/index.html` to explore and try the API interactively. The OpenAPI JSON specification is available at `http://localhost:3000/v3/api-docs`.
7. Import `postman/TengeTracker.postman_collection.json` into Postman and run the requests in order. The Create account request saves its returned ID to the `accountId` collection variable.

Stop with `Ctrl+C`; then run `docker compose down` to remove containers. Data remains in the named volume. To delete the database data too, run `docker compose down -v` (destructive). PostgreSQL init scripts run only for an empty data directory. If you already started this project before adding the seed script and want the demo data, run `docker compose down -v` and then `docker compose up --build`; this deletes all existing database data, so do it only if you do not need that data.

## Architecture
- **backend**: custom multi-stage Dockerfile, Java 17 and Spring Boot; host port `3000` maps to container port `3000`.
- **db**: PostgreSQL 15 Alpine; backend connects to hostname `db` on port `5432` over Compose's default network. Do not use `localhost` as the database hostname from inside the backend container.
- **postgres_data**: named volume mounted at PostgreSQL's data directory so records survive container recreation.
- **database seeding**: `db/init/01-seed.sql` is mounted read-only into PostgreSQL's initialization directory and runs only when `postgres_data` is empty. It seeds 5 accounts, 19 transactions, 15 categories, and 5 budgets.
- **configuration**: Compose reads `.env`; values are passed to the backend as environment variables and Spring reads them from `src/main/resources/application.yaml`.

## Data model
Account 1→N Transaction, Category 1→N Transaction, Category 1→N Budget (JPA `@OneToMany`/`@ManyToOne` plus database foreign keys).

## Implemented API (25 endpoints)
| Resource | Endpoints |
|---|---|
| Accounts | `GET/POST /api/accounts`, `GET/PUT/DELETE /api/accounts/{id}`, `GET /api/accounts/{id}/transactions` |
| Categories | `GET/POST /api/categories`, `GET/PUT/DELETE /api/categories/{id}`, `GET /api/categories/{id}/transactions`, `GET /api/categories/{id}/budgets` |
| Budgets | `GET/POST /api/budgets`, `GET/PUT/DELETE /api/budgets/{id}`, `GET /api/budgets/{id}/status` |
| Transactions | `GET/POST /api/transactions`, `GET/PUT/DELETE /api/transactions/{id}` |
| Summary | `GET /api/summary?startDate=&endDate=` |

Full documentation (method, URL, parameters/body, responses, errors) is in [`docs/API.md`](docs/API.md). Swagger UI is at `/swagger-ui/index.html`. Errors use 400 (validation), 404 (not found) and 409 (conflict).

## Submission checklist
- Public GitHub repository contains source, `Dockerfile`, `docker-compose.yaml`, `README.md`, `docs/API.md`, and the exported Postman collection.
- - Before turning in, run `docker compose up --build`, run the Postman collection (Collection Runner, all 42 requests), and verify persistence by restarting containers without `-v`.
