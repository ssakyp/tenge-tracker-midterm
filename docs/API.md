# API Reference

Base URL: `http://localhost:3000` — all bodies are JSON (`Content-Type: application/json`).
Interactive docs: Swagger UI `http://localhost:3000/swagger-ui/index.html`, OpenAPI JSON `http://localhost:3000/v3/api-docs`.

## Data model

```
Account  1 ──< N  Transaction  N >── 1  Category  1 ──< N  Budget
```

- **Account** (`id`, `name`, `balance`, `currency`)
- **Category** (`id`, `name`, `type` = `INCOME` | `EXPENSE`)
- **Transaction** (`id`, `accountId`, `categoryId?`, `amount`, `type` = `INCOME` | `EXPENSE` | `TRANSFER`, `targetAccountId?`, `description?`, `date`)
- **Budget** (`id`, `categoryId`, `limitAmount`, `month`, `year`)

Relationships (JPA `@OneToMany` / `@ManyToOne`, enforced by foreign keys): Account→Transactions, Category→Transactions, Category→Budgets.

## Endpoint summary (25)

| # | Method | URL | Success |
|---|---|---|---|
| 1 | GET | `/api/accounts` | 200 |
| 2 | GET | `/api/accounts/{id}` | 200 |
| 3 | POST | `/api/accounts` | 201 |
| 4 | PUT | `/api/accounts/{id}` | 200 |
| 5 | DELETE | `/api/accounts/{id}` | 204 |
| 6 | GET | `/api/accounts/{id}/transactions` | 200 |
| 7 | GET | `/api/categories` | 200 |
| 8 | GET | `/api/categories/{id}` | 200 |
| 9 | POST | `/api/categories` | 201 |
| 10 | PUT | `/api/categories/{id}` | 200 |
| 11 | DELETE | `/api/categories/{id}` | 204 |
| 12 | GET | `/api/categories/{id}/transactions` | 200 |
| 13 | GET | `/api/categories/{id}/budgets` | 200 |
| 14 | GET | `/api/budgets` | 200 |
| 15 | GET | `/api/budgets/{id}` | 200 |
| 16 | POST | `/api/budgets` | 201 |
| 17 | PUT | `/api/budgets/{id}` | 200 |
| 18 | DELETE | `/api/budgets/{id}` | 204 |
| 19 | GET | `/api/budgets/{id}/status` | 200 |
| 20 | GET | `/api/transactions` | 200 |
| 21 | GET | `/api/transactions/{id}` | 200 |
| 22 | POST | `/api/transactions` | 201 |
| 23 | PUT | `/api/transactions/{id}` | 200 |
| 24 | DELETE | `/api/transactions/{id}` | 204 |
| 25 | GET | `/api/summary` | 200 |

## Error format

```json
{ "timestamp": "2026-11-10T12:00:00", "status": 404, "error": "Account not found with id: 42" }
```
Bean-validation failures return `{"timestamp": ..., "status": 400, "errors": {"field": "message"}}`.

| Status | Meaning |
|---|---|
| 400 | Validation failure, malformed JSON/date, invalid parameter, business-rule violation (e.g. insufficient balance) |
| 404 | Resource (or referenced account/category) not found |
| 405 | HTTP method not supported on the URL |
| 409 | Conflict: duplicate budget, deleting/retyping something still in use |

---

## Accounts

### 1. GET `/api/accounts`
List all accounts. **200** → `[{"id":1,"name":"Kaspi Gold","balance":345000.00,"currency":"KZT"}, ...]`

### 2. GET `/api/accounts/{id}`
Path: `id` (number). **200** → one account. **404** if missing, **400** if `id` is not a number.

### 3. POST `/api/accounts`
Body: `name` (required, not blank), `balance` (optional, ≥ 0, default 0), `currency` (optional, 3 letters, default `KZT`).
```json
{ "name": "Main account", "balance": 25000.00, "currency": "KZT" }
```
**201** → created account with generated `id`. **400** on validation error.

### 4. PUT `/api/accounts/{id}`
Path: `id`. Body: `name` (required), `currency` (optional). Balance is **not** changed by this call (use transactions).
```json
{ "name": "Travel account", "currency": "USD" }
```
**200** → updated account. **400**, **404**.

### 5. DELETE `/api/accounts/{id}`
Path: `id`. **204** (no body). **404** if missing. **409** if the account still has transactions (as source or transfer target).

### 6. GET `/api/accounts/{id}/transactions`
Related data. Path: `id`. Returns all transactions where the account is the source or target, newest first.
**200** → array of transactions (empty if none). **404** if account missing.

## Categories

### 7. GET `/api/categories`
**200** → `[{"id":5,"name":"Groceries & Supermarket","type":"EXPENSE"}, ...]`

### 8. GET `/api/categories/{id}`
**200** → category. **404** if missing.

### 9. POST `/api/categories`
Body: `name` (required), `type` (required: `INCOME` or `EXPENSE`).
```json
{ "name": "Groceries", "type": "EXPENSE" }
```
**201** → created category. **400** on validation error.

### 10. PUT `/api/categories/{id}`
Body as in POST. **200** → updated. **400**, **404**. **409** if `type` changes while transactions/budgets use the category.

### 11. DELETE `/api/categories/{id}`
**204**. **404**. **409** if transactions or budgets reference it.

### 12. GET `/api/categories/{id}/transactions`
Related data. **200** → transactions of the category, newest first. **404** if category missing.

### 13. GET `/api/categories/{id}/budgets`
Related data. **200** → budgets of the category. **404** if category missing.

## Budgets

### 14. GET `/api/budgets`
Query (optional): `month` (1–12), `year`. **200** → array of budgets.
```json
[{ "id": 1, "categoryId": 5, "limitAmount": 150000.00, "month": 11, "year": 2026 }]
```

### 15. GET `/api/budgets/{id}`
**200** → budget. **404**.

### 16. POST `/api/budgets`
Body: `categoryId` (required, existing **EXPENSE** category), `limitAmount` (required, > 0), `month` (1–12), `year` (2000–2100). One budget per category and month/year.
```json
{ "categoryId": 5, "limitAmount": 50000.00, "month": 11, "year": 2026 }
```
**201** → created. **400** validation / non-expense category. **404** category not found. **409** duplicate.

### 17. PUT `/api/budgets/{id}`
Body as in POST. **200** → updated. **400**, **404**, **409**.

### 18. DELETE `/api/budgets/{id}`
**204**. **404**.

### 19. GET `/api/budgets/{id}/status`
Related data: sums the category's EXPENSE transactions within the budget's month. **404** if missing.
```json
{ "budgetId": 1, "categoryId": 5, "limitAmount": 50000.00, "spentAmount": 1200.00, "remainingAmount": 48800.00, "isExceeded": false }
```

## Transactions

### 20. GET `/api/transactions`
Query (optional): `startDate`, `endDate` (`YYYY-MM-DD`, inclusive), `type` (`INCOME`/`EXPENSE`/`TRANSFER`). Newest first.
**200** → array. **400** for an invalid type/date or `startDate` after `endDate`.

### 21. GET `/api/transactions/{id}`
**200** → transaction. **404**.

### 22. POST `/api/transactions`
Body: `accountId` (required), `amount` (required, > 0), `type` (required, case-insensitive), `date` (required, `YYYY-MM-DD`), `categoryId` (optional), `targetAccountId` (required for `TRANSFER`), `description` (optional).
```json
{ "accountId": 1, "categoryId": 5, "amount": 1200.00, "type": "EXPENSE", "description": "Groceries", "date": "2026-11-02" }
```
Rules: INCOME adds to the account; EXPENSE subtracts (fails if balance is too low); TRANSFER moves funds to a **different** target account and cannot have a category. A category's type must match the transaction type.
**201** → created transaction. **400** validation / insufficient balance / missing target / type mismatch. **404** account, target or category not found.

### 23. PUT `/api/transactions/{id}`
Body as in POST (all fields replaced). The old transaction's balance effect is reversed and the new one applied atomically.
**200** → updated. **400**, **404**.

### 24. DELETE `/api/transactions/{id}`
Deletes the transaction and reverses its effect on account balance(s). **204**, **404**.

## Summary

### 25. GET `/api/summary`
Query (required): `startDate`, `endDate` (`YYYY-MM-DD`, inclusive).
```json
{ "totalIncome": 5000.00, "totalExpenses": 1200.00, "netBalance": 3800.00, "totalAssets": 2894200.50 }
```
`totalAssets` is the sum of all account balances. **400** on missing/invalid dates or `startDate` after `endDate`.
