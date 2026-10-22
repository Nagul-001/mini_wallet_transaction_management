# Mini Wallet Transaction System

A Spring Boot REST API implementing the requirements in the supplied Mini Wallet Transaction System PDF. No frontend is included.

## Requirements
- Java 17+
- Maven 3.8+

## Run

```bash
mvn spring-boot:run
```

The default database is an in-memory H2 database; no separate database installation is needed. Data is reset when the application stops. H2 console: `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:walletdb`, user `sa`, and blank password.

### Optional MySQL

Create a database named `mini_wallet`, then run:

```bash
DB_URL='jdbc:mysql://localhost:3306/mini_wallet' DB_USERNAME='root' DB_PASSWORD='your-password' mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Or set the corresponding environment variables for your environment. Never commit real credentials.

## API endpoints

| Method | Endpoint | Purpose | Success |
|---|---|---|---|
| POST | `/api/wallets` | Create wallet | `201 Created` |
| POST | `/api/wallets/{walletId}/deposit` | Deposit money | `200 OK` |
| POST | `/api/wallets/transfer` | Transfer between wallets | `200 OK` |
| GET | `/api/wallets/{walletId}` | Get wallet details | `200 OK` |
| GET | `/api/wallets/{walletId}/transactions` | Recent-first transaction history | `200 OK` |

Import `postman/mini-wallet-system.postman_collection.json` into Postman. Create two wallets first; then set `fromWalletId` and `toWalletId` in the collection variables. The collection saves IDs from the create-wallet response when run through the collection.

## Example requests

Create wallet:

```json
{"name":"Rahul","email":"rahul@example.com","initialBalance":1000.00}
```

Deposit:

```json
{"amount":500.00}
```

Transfer:

```json
{"fromWalletId":1,"toWalletId":2,"amount":300.00}
```

## Error responses

Errors use a consistent JSON structure with `timestamp`, `status`, `errorCode`, `message`, and `validationErrors`. Typical status codes: `400` for invalid input, insufficient balance, or same-wallet transfer; `404` for a missing wallet; `409` for duplicate email or concurrent/data-integrity conflicts; `500` for unexpected failures. Internal exception details are not returned.

## Data model

- `wallet`: owner name, unique email, current `DECIMAL(19,2)` balance, creation timestamp, and optimistic version.
- `wallet_transaction`: wallet foreign key, type, positive amount, status, optional transfer reference UUID, and timestamp. A transfer creates `TRANSFER_OUT` and `TRANSFER_IN` rows sharing the same reference ID.

## Transaction management

See [`docs/transfer-transaction-strategy.md`](docs/transfer-transaction-strategy.md). Deposits and transfers use `@Transactional`, and wallet rows are locked for updates during balance-changing operations. This ensures balance changes and history entries commit or roll back together.

## Test

```bash
mvn test
```

The integration tests exercise wallet creation/retrieval and invalid deposit validation. Additional scenarios to test in Postman include duplicate emails, negative initial balances, unknown wallet IDs, insufficient balance, same-wallet transfers, transfer history on both wallets, and a failed transfer's unchanged balances.

## Design notes

- Controllers only handle HTTP and validation; business logic belongs in the service.
- API DTOs are separate from JPA entities.
- `BigDecimal` is used for all monetary amounts.
- Database uniqueness is enforced for email; the service also checks before insert and handles concurrent duplicate inserts.
- Transaction history is ordered by `createdAt DESC`, then ID descending to break timestamp ties.
