# Database initialization

`init/01-seed.sql` is mounted into PostgreSQL's `/docker-entrypoint-initdb.d/` directory by Compose. PostgreSQL executes it only when the `postgres_data` named volume is initialized for the first time. It creates the schema and seeds 100 accounts, 500 transactions, 8 categories, and 24 budgets.

To re-run it during local development, remove the volume with `docker compose down -v` and start again with `docker compose up --build`. WARNING: removing the volume permanently deletes all database data in it.
