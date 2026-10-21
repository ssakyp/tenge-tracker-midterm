# Tenge Tracker API

A Java 17 / Spring Boot REST API for tracking accounts and financial transactions. PostgreSQL is the database; Docker Compose runs the backend and database on a private Compose network and persists database data in the named `postgres_data` volume.

## Requirements
- Docker Desktop (or Docker Engine) with the Compose plugin
- Postman (for the demonstration)

## Run it
1. Extract the project folder and open a terminal in `tenge-tracker/`.
2. The supplied `.env` is configured for local coursework/demo use. For a fresh setup, copy `.env.example` to `.env` and ensure `POSTGRES_PASSWORD` matches `DB_PASS`.
3. Start the complete application:
   ```bash
   docker compose up --build
   ```
4. Wait until the backend logs show that Spring Boot has started. The API base URL is `http://localhost:3000`.
5. Open Swagger UI at `http://localhost:3000/swagger-ui/index.html` to explore and try the API interactively. The OpenAPI JSON specification is available at `http://localhost:3000/v3/api-docs`.
6. Import `postman/TengeTracker.postman_collection.json` into Postman and run the requests in order. The Create account request saves its returned ID to the `accountId` collection variable.

Stop with `Ctrl+C`; then run `docker compose down` to remove containers. Data remains in the named volume. To delete the database data too, run `docker compose down -v` (destructive).

## Architecture
- **backend**: custom multi-stage Dockerfile, Java 17 and Spring Boot; host port `3000` maps to container port `3000`.
- **db**: PostgreSQL 15 Alpine; backend connects to hostname `db` on port `5432` over Compose's default network. Do not use `localhost` as the database hostname from inside the backend container.
- **postgres_data**: named volume mounted at PostgreSQL's data directory so records survive container recreation.
- **configuration**: Compose reads `.env`; values are passed to the backend as environment variables and Spring reads them from `src/main/resources/application.yaml`.

## Implemented API
Interactive API documentation is provided by Swagger UI at `/swagger-ui/index.html`, generated from the OpenAPI specification at `/v3/api-docs`. The required list/get/create/update/delete operations are implemented for `/api/accounts`. Transactions currently support list and create only. A concise endpoint reference is also available in [`docs/API.md`](docs/API.md).

## Submission checklist
- Public GitHub repository contains source, `Dockerfile`, `docker-compose.yaml`, `README.md`, `docs/API.md`, and the exported Postman collection.
- Submit the separate `.env` ZIP alongside the repository link. `.env` contains coursework-only local credentials; do not reuse them outside local development.
- Before turning in, run `docker compose up --build`, demonstrate account CRUD requests in Postman, and verify persistence by restarting containers without `-v`.
