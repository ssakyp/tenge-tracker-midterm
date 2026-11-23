# Tenge Tracker — Fullstack App with JWT Authentication (HW4)

Personal finance tracker: accounts, income/expense/transfer transactions, tags, categories and monthly budgets.

| Layer | Tech | Container | URL |
|---|---|---|---|
| Frontend | React 19 + React Router 7 (Vite), served by nginx | `frontend` | http://localhost:8080 |
| Backend | Java 17, Spring Boot 3.2, Spring Security, JJWT | `backend` | http://localhost:3000 (Swagger: `/swagger-ui/index.html`) |
| Database | PostgreSQL 15 | `db` | localhost:5432 |

## Run it

Requirements: Docker Desktop (or Docker Engine + Compose plugin). Nothing else: Java, Maven and Node are used only inside the build containers.

```bash
git clone <this repo>
cd tenge-tracker
cp .env.example .env          # optional: docker-compose.yaml has working defaults
docker compose up --build
```

Wait until the backend log shows `Started TengeTrackerApplication`, then open **http://localhost:8080**.

**Demo login:** `demo` / `password123` (all 10 seeded users use `password123`: `demo`, `aruzhan`, `daniyar`, `madina`, `nursultan`, `aigerim`, `timur`, `dana`, `yerlan`, `saule`).

Stop with `Ctrl+C` and `docker compose down`. Data stays in the `postgres_data` volume.

> **Upgrading from the midterm version?** The seed script runs only on an empty database, and the schema changed (users, profiles, tags). Reset once with `docker compose down -v && docker compose up --build`. `-v` deletes all database data.

### Postman
Import `postman/TengeTracker.postman_collection.json` and run the whole collection with the **Collection Runner** (83 requests, in order). It signs up a fresh user, stores the JWT in the `token` collection variable (used by collection-level Bearer auth), tests every CRUD verb, the 401/403/400/404/409 cases, username and password change (including old-token revocation), then deletes what it created. It can be run repeatedly.

### Development without Docker (optional)
```bash
docker compose up db                       # database only
cd backend && ./mvnw spring-boot:run       # API on :3000
cd frontend && npm install && npm run dev  # UI on :5173, /api proxied to :3000
cd backend && ./mvnw test                  # auth integration tests on in-memory H2
```

## Project structure
```
backend/            Spring Boot API (Dockerfile, pom.xml, src/)
  security/         JwtService, JwtAuthenticationFilter, SecurityConfig, AuthUser
frontend/           React app (Dockerfile, nginx.conf, src/)
  src/auth/         AuthContext (token + user state), ProtectedRoute, token helpers
  src/api/          fetch wrapper (adds Bearer header, handles errors/401)
db/init/01-seed.sql schema + seed data (≥10 rows in every table)
postman/            Postman collection
docs/API.md         endpoint reference
docker-compose.yaml db + backend + frontend
```

## Data model and relationships
```
users 1──1 user_profiles                      one-to-one  (user_profiles.user_id UNIQUE)
users 1──< accounts      users 1──< budgets   one-to-many
accounts 1──< transactions                    one-to-many (source account)
categories 1──< transactions, categories 1──< budgets
transactions >──< tags  (join table transaction_tags)   many-to-many
```
Seed data: 10 users, 10 profiles, 16 accounts, 15 categories, 10 tags, 30 transactions, 21 transaction–tag links, 12 budgets.

Accounts, transactions and budgets are **private** to their owner. Categories and tags are shared reference data.

## API overview (full details in [`docs/API.md`](docs/API.md))

| Resource | Public | Requires JWT |
|---|---|---|
| Auth | `POST /api/auth/signup`, `POST /api/auth/login` | — |
| Current user | — | `GET /api/users/me`, `PATCH /api/users/me` (username), `PUT /api/users/me/password`, `PUT /api/users/me/profile` |
| Categories | `GET /api/categories`, `GET /api/categories/{id}` | `POST`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}`, `GET /{id}/transactions`, `GET /{id}/budgets` |
| Tags | `GET /api/tags`, `GET /api/tags/{id}` | `POST`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}`, `GET /{id}/transactions` |
| Accounts | — | `GET`, `POST`, `GET/PUT/PATCH/DELETE /{id}`, `GET /{id}/transactions` |
| Transactions | — | `GET`, `POST`, `GET/PUT/PATCH/DELETE /{id}` |
| Budgets | — | `GET`, `POST`, `GET/PUT/PATCH/DELETE /{id}`, `GET /{id}/status` |
| Summary | — | `GET /api/summary?startDate=&endDate=` |

Status codes: `400` validation/business rule · `401` no/invalid/expired/revoked token · `403` someone else's data · `404` not found · `409` conflict.

## How authentication works (defense notes)

### JWT
A JSON Web Token is `header.payload.signature`, each part base64url-encoded:
- **header** `{"alg":"HS256"}` — signing algorithm
- **payload** (claims) `{"sub":"1","username":"demo","ver":0,"iat":…,"exp":…}` — user ID, username, token version, issued-at, expiry
- **signature** = HMAC-SHA256(header + "." + payload, `JWT_SECRET`)

The payload is only *encoded*, not encrypted (the Profile page decodes it in the browser), so it never contains the password. Nobody can change it without the secret, because the signature would no longer match.

### Backend flow
1. **Signup** (`AuthService.signup`) stores the password as a **BCrypt hash** (`BCryptPasswordEncoder`), creates the one-to-one profile and returns a token.
2. **Login** (`AuthService.login`) compares the password with the hash and returns `{accessToken, tokenType:"Bearer", expiresIn, user}`. Wrong username and wrong password give the same 401 message.
3. **Every request** passes `JwtAuthenticationFilter`: it reads `Authorization: Bearer <token>`, verifies signature and expiry (`JwtService.parse`), loads the user, checks the token version, and puts an `AuthUser(id, username)` into Spring's `SecurityContext`.
4. **`SecurityConfig`** decides what needs a token: signup/login and GET of categories/tags are `permitAll()`, everything else is `authenticated()`. Without a valid token the entry point returns JSON `401` with the reason (missing, invalid, expired, revoked).
5. **Ownership (authorization)**: controllers receive `@AuthenticationPrincipal AuthUser me`, and services check that the account/transaction/budget belongs to `me.id()`, otherwise `403`. The owner of new data always comes from the token, never from the request body.
6. **Password change** requires the current password, stores a new hash and increments `users.token_version`. Old tokens carry the old `ver` claim and are rejected (401) — this logs out other devices. A new token is returned so the current session continues.
7. The API is **stateless** (no HTTP session, no cookies), so CSRF protection is disabled; CORS is configured for the Vite dev server origin. In Docker, nginx serves the UI and proxies `/api` to the backend, so the browser sees one origin.

### Frontend flow
- `AuthContext` (React Context) holds `token` and `user` and exposes `login`, `signup`, `logout`, `updateUsername`, `changePassword`, `updateProfile`.
- The token is saved in `localStorage` so a reload keeps the session; on startup it is checked (`exp`) and `GET /api/users/me` loads the user.
- `api/client.js` adds `Authorization: Bearer <token>` to every request, converts error bodies to `ApiError` (message + field errors), and on any `401` for a request that carried a token, logs the user out with "session expired" and the guard sends them to `/login`. A timer also logs out exactly at `exp`.
- **Route protection:** `ProtectedRoute` wraps `/dashboard`, `/transactions`, `/accounts`, `/budgets`, `/profile`. While the session is checked it shows a spinner; without a user it renders `<Navigate to="/login" state={{from}} />`, and after login the user returns to the page they wanted. `GuestRoute` keeps logged-in users away from `/login` and `/signup`. Front-end guards only hide UI; the backend's 401/403 is the real protection.
- Every page shows loading spinners, disables buttons while saving, and shows API errors (including field-level validation errors).

### Protected pages
| Route | What it does | API used |
|---|---|---|
| `/profile` | Shows current user, profile and decoded JWT; change username; change password; edit details | `GET/PATCH /users/me`, `PUT /users/me/password`, `PUT /users/me/profile` |
| `/transactions` | Main feature: list, filter, add, edit, delete transactions with tags | `GET/POST/PUT/DELETE /transactions` |
| `/dashboard` | Month summary, balances, budget progress, recent activity | `/summary`, `/accounts`, `/budgets/{id}/status` |
| `/accounts`, `/budgets` | CRUD with PATCH for renames and limit changes | `/accounts`, `/budgets` |

Public pages: `/` (landing, uses public GET), `/catalog` (anyone can read categories and tags; edit buttons appear only when logged in), `/login`, `/signup`.

### Security choices and limits
- Short-lived access token (60 min, `JWT_EXPIRATION_MINUTES`), no refresh token: after expiry the user logs in again.
- `localStorage` is readable by JavaScript, so an XSS bug could steal the token. React escapes all output and the app never injects HTML. An HttpOnly refresh-token cookie would be the next step.
- Use HTTPS in any real deployment and set a long random `JWT_SECRET` in `.env`.

## Configuration (`.env`)
| Variable | Default | Purpose |
|---|---|---|
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | `tengetracker` / `postgres` / `postgrespassword` | database |
| `JWT_SECRET` | dev value in compose | HS256 signing key, ≥ 32 characters |
| `JWT_EXPIRATION_MINUTES` | `60` | access token lifetime |
| `APP_PORT` / `FRONTEND_PORT` / `DB_EXPOSE_PORT` | `3000` / `8080` / `5432` | host ports |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:8080` | origins allowed to call the API directly |

## Troubleshooting
- **Login fails for `demo` / tables missing:** the database volume is from the midterm. Run `docker compose down -v && docker compose up --build`.
- **Port already in use:** change `APP_PORT`, `FRONTEND_PORT` or `DB_EXPOSE_PORT` in `.env`.
- **Frontend shows "Cannot reach the server":** the backend is still starting; wait for `Started TengeTrackerApplication`.
