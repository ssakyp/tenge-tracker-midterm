# API Reference

Base URL: `http://localhost:3000` (or `http://localhost:8080/api/...` through the frontend's nginx). JSON bodies (`Content-Type: application/json`).
Interactive docs: Swagger UI `http://localhost:3000/swagger-ui/index.html` (click **Authorize** and paste the token).

## Authentication

1. `POST /api/auth/signup` or `POST /api/auth/login` → `accessToken`.
2. Send it on every protected request: `Authorization: Bearer <accessToken>`.

```json
// POST /api/auth/login   {"username":"demo","password":"password123"}
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJkZW1vIiwidmVyIjowLCJpYXQiOjE3NjM4ODQwMDAsImV4cCI6MTc2Mzg4NzYwMH0.…",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1, "username": "demo", "createdAt": "2026-09-24T12:00:00",
    "profile": {"fullName": "Demo User", "email": "demo@tengetracker.kz", "preferredCurrency": "KZT", "bio": "…"},
    "accountsCount": 5, "transactionsCount": 18
  }
}
```

Token payload: `sub` (user ID), `username`, `ver` (token version, bumped on password change), `iat`, `exp`. Signed with HS256.

## Data model

```
User 1──1 UserProfile
User 1──< Account 1──< Transaction >──< Tag      (transaction_tags)
User 1──< Budget >──1 Category 1──< Transaction
```

- **Account** (`id`, `userId`, `name`, `balance`, `currency`)
- **Category** (`id`, `name`, `type` = `INCOME` | `EXPENSE`) — shared
- **Tag** (`id`, `name`, `color`) — shared
- **Transaction** (`id`, `accountId`, `categoryId?`, `amount`, `type` = `INCOME` | `EXPENSE` | `TRANSFER`, `targetAccountId?`, `description?`, `date`, `tags[]`; write `tagIds[]`)
- **Budget** (`id`, `userId`, `categoryId`, `limitAmount`, `month`, `year`)

## Endpoints

🔓 = public, 🔒 = needs `Authorization: Bearer <token>`. 🔒 endpoints on accounts, transactions and budgets also return **403** when the resource belongs to another user.

| Method | URL | Auth | Body | Success |
|---|---|---|---|---|
| POST | `/api/auth/signup` | 🔓 | `{username, password, fullName?, email?}` | 201 AuthResponse |
| POST | `/api/auth/login` | 🔓 | `{username, password}` | 200 AuthResponse |
| GET | `/api/users/me` | 🔒 | — | 200 User |
| PATCH | `/api/users/me` | 🔒 | `{username}` | 200 AuthResponse (new token) |
| PUT | `/api/users/me/password` | 🔒 | `{currentPassword, newPassword}` | 200 AuthResponse (old tokens revoked) |
| PUT | `/api/users/me/profile` | 🔒 | `{fullName, email, preferredCurrency, bio}` | 200 User |
| GET | `/api/categories` | 🔓 | — | 200 |
| GET | `/api/categories/{id}` | 🔓 | — | 200 |
| POST | `/api/categories` | 🔒 | `{name, type}` | 201 |
| PUT | `/api/categories/{id}` | 🔒 | `{name, type}` | 200 |
| PATCH | `/api/categories/{id}` | 🔒 | `{name?, type?}` | 200 |
| DELETE | `/api/categories/{id}` | 🔒 | — | 204 (409 if used) |
| GET | `/api/categories/{id}/transactions` | 🔒 | — | 200 (my transactions) |
| GET | `/api/categories/{id}/budgets` | 🔒 | — | 200 (my budgets) |
| GET | `/api/tags` | 🔓 | — | 200 |
| GET | `/api/tags/{id}` | 🔓 | — | 200 |
| POST | `/api/tags` | 🔒 | `{name, color?}` | 201 (409 duplicate name) |
| PUT | `/api/tags/{id}` | 🔒 | `{name, color}` | 200 |
| PATCH | `/api/tags/{id}` | 🔒 | `{name?, color?}` | 200 |
| DELETE | `/api/tags/{id}` | 🔒 | — | 204 (removed from transactions) |
| GET | `/api/tags/{id}/transactions` | 🔒 | — | 200 (my tagged transactions) |
| GET | `/api/accounts` | 🔒 | — | 200 (my accounts) |
| GET | `/api/accounts/{id}` | 🔒 | — | 200 |
| POST | `/api/accounts` | 🔒 | `{name, balance?, currency?}` | 201 |
| PUT | `/api/accounts/{id}` | 🔒 | `{name, currency?}` | 200 (balance unchanged) |
| PATCH | `/api/accounts/{id}` | 🔒 | `{name?, currency?}` | 200 |
| DELETE | `/api/accounts/{id}` | 🔒 | — | 204 (409 if it has transactions) |
| GET | `/api/accounts/{id}/transactions` | 🔒 | — | 200 |
| GET | `/api/transactions?startDate=&endDate=&type=` | 🔒 | — | 200 (filters optional) |
| GET | `/api/transactions/{id}` | 🔒 | — | 200 |
| POST | `/api/transactions` | 🔒 | see below | 201 |
| PUT | `/api/transactions/{id}` | 🔒 | full body | 200 |
| PATCH | `/api/transactions/{id}` | 🔒 | any subset, e.g. `{description}` or `{tagIds}` | 200 |
| DELETE | `/api/transactions/{id}` | 🔒 | — | 204 (balance reversed) |
| GET | `/api/budgets?month=&year=` | 🔒 | — | 200 |
| GET | `/api/budgets/{id}` | 🔒 | — | 200 |
| POST | `/api/budgets` | 🔒 | `{categoryId, limitAmount, month, year}` | 201 (409 duplicate) |
| PUT | `/api/budgets/{id}` | 🔒 | full body | 200 |
| PATCH | `/api/budgets/{id}` | 🔒 | e.g. `{limitAmount}` | 200 |
| DELETE | `/api/budgets/{id}` | 🔒 | — | 204 |
| GET | `/api/budgets/{id}/status` | 🔒 | — | 200 `{spentAmount, remainingAmount, isExceeded, …}` |
| GET | `/api/summary?startDate=&endDate=` | 🔒 | — | 200 `{totalIncome, totalExpenses, netBalance, totalAssets}` |

### Transaction body
```json
{
  "accountId": 1,
  "categoryId": 5,
  "amount": 12500.00,
  "type": "EXPENSE",
  "targetAccountId": null,
  "description": "Groceries at Magnum",
  "date": "2026-11-23",
  "tagIds": [2, 4]
}
```
Rules: `INCOME` adds to the account, `EXPENSE` subtracts if funds suffice, `TRANSFER` needs `targetAccountId` (a different account of yours) and no category. A category's type must match the transaction type. On PUT, omitted `tagIds` means no tags; on PATCH, omitted fields stay unchanged.

## Errors

```json
{ "timestamp": "2026-11-23T12:00:00", "status": 401, "error": "Token has expired. Please log in again" }
{ "timestamp": "2026-11-23T12:00:00", "status": 400, "errors": { "password": "Password must be 8-72 characters" } }
```

| Status | When |
|---|---|
| 400 | Validation failure, malformed JSON/date, wrong current password, insufficient balance, category/type mismatch |
| 401 | Missing token, invalid signature, expired token, token revoked by password change, wrong login credentials |
| 403 | The account / transaction / budget belongs to another user |
| 404 | Resource or referenced account/category/tag not found |
| 405 | Method not supported on the URL |
| 409 | Duplicate username, tag or budget; deleting something still in use |
