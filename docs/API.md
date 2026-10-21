# API Reference

**Primary API documentation: Swagger UI**

When the application is running, open:

- Swagger UI: <http://localhost:3000/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:3000/v3/api-docs>

Swagger UI is generated from the application's controllers and OpenAPI configuration. It documents the available routes and lets you execute requests against the running backend.

## Base URL

`http://localhost:3000`

## Accounts — `/api/accounts`

| Method | Endpoint | Description | Success response |
|---|---|---|---|
| GET | `/api/accounts` | List accounts | `200 OK` |
| GET | `/api/accounts/{id}` | Get one account | `200 OK` |
| POST | `/api/accounts` | Create an account | `201 Created` |
| PUT | `/api/accounts/{id}` | Update an account | `200 OK` |
| DELETE | `/api/accounts/{id}` | Delete an account | `204 No Content` |

## Transactions — `/api/transactions`

| Method | Endpoint | Description | Success response |
|---|---|---|---|
| GET | `/api/transactions` | List transactions | `200 OK` |
| POST | `/api/transactions` | Create a transaction | `201 Created` |

Transactions currently support listing and creation only. Account CRUD is the complete list/get/create/update/delete resource required by the assignment. See Swagger UI for the live request/response schemas and try-it-out controls.
