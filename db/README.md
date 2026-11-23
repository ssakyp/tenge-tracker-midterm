# Database initialization

`init/01-seed.sql` is mounted into PostgreSQL's `/docker-entrypoint-initdb.d/` by Compose and runs only when the `postgres_data` volume is empty. It creates the schema (with foreign keys, the one-to-one unique key and the many-to-many join table) and seeds:

| Table | Rows |
|---|---|
| users | 10 (password `password123` for all, stored as BCrypt hash) |
| user_profiles | 10 |
| accounts | 16 (ids 1–5 belong to `demo`) |
| categories | 15 |
| tags | 10 |
| transactions | 30 |
| transaction_tags | 21 |
| budgets | 12 (current month) |

To re-run it: `docker compose down -v` then `docker compose up --build`. WARNING: `-v` permanently deletes all database data.
