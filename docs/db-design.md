# Database Design

## Users Table

| Column | Type |
|----------|----------|
| id | BIGINT |
| name | VARCHAR(100) |
| email | VARCHAR(255) |
| password | VARCHAR(255) |
| created_at | TIMESTAMP |

---

## Transactions Table

| Column | Type |
|----------|----------|
| id | BIGINT |
| user_id | BIGINT |
| amount | DECIMAL |
| type | VARCHAR(20) |
| category | VARCHAR(50) |
| description | TEXT |
| transaction_date | DATE |
| created_at | TIMESTAMP |

---

## Budgets Table

| Column | Type |
|----------|----------|
| id | BIGINT |
| user_id | BIGINT |
| category | VARCHAR(50) |
| monthly_limit | DECIMAL |
| month | INT |
| year | INT |

---

## Relationships

One User -> Many Transactions

One User -> Many Budgets