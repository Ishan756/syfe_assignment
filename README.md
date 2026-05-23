# Personal Finance Manager

A Spring Boot REST API for managing personal finances — track income, expenses, savings goals, and generate reports.

---

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Security (session-based auth)
- Spring Data JPA + Hibernate
- PostgreSQL
- Maven
- JUnit 5 + Mockito

---

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

---

## PostgreSQL Setup

```sql
CREATE DATABASE finance_manager;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE finance_manager TO postgres;
```

Or update `src/main/resources/application.properties` with your own credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_manager
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

## How to Run

```bash
# Clone the repo
git clone https://github.com/your-username/finance-manager.git
cd finance-manager

# Build and run
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

Default categories (Salary, Food, Rent, etc.) are auto-seeded on first startup.

---

## Frontend

A simple React + Tailwind frontend lives in `frontend/`.

### Frontend Prerequisites

- Node.js 18+
- npm 9+

### Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server runs on `http://localhost:5173` and proxies `/api` requests to the Spring Boot backend on `http://localhost:8080`.

### Optional Production Build

```bash
cd frontend
npm run build
```

The build output is written to `frontend/dist`.

---

## Running Tests

```bash
mvn test
```

Tests use H2 in-memory database — no PostgreSQL needed for testing.

---

## Authentication

This API uses **session-based authentication with cookies**.

1. Register at `POST /api/auth/register`
2. Login at `POST /api/auth/login` — you'll receive a `JSESSIONID` cookie
3. Include that cookie in all subsequent requests
4. Logout at `POST /api/auth/logout` to invalidate the session

All endpoints except `/api/auth/register` and `/api/auth/login` require a valid session.

---

## API Overview

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Create a transaction |
| GET | `/api/transactions` | Get all transactions (supports filters) |
| PUT | `/api/transactions/{id}` | Update a transaction |
| DELETE | `/api/transactions/{id}` | Delete a transaction |

Filter params: `?startDate=2024-01-01&endDate=2024-01-31&categoryId=1`

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories (default + custom) |
| POST | `/api/categories` | Create a custom category |
| DELETE | `/api/categories/{name}` | Delete a custom category |

### Savings Goals
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/goals` | Create a goal |
| GET | `/api/goals` | Get all goals |
| GET | `/api/goals/{id}` | Get a specific goal |
| PUT | `/api/goals/{id}` | Update a goal |
| DELETE | `/api/goals/{id}` | Delete a goal |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/monthly/{year}/{month}` | Monthly report |
| GET | `/api/reports/yearly/{year}` | Yearly report |

---

## Sample Requests

### Register
```bash
curl -c cookies.txt -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123","fullName":"John Doe","phoneNumber":"+1234567890"}'
```

### Login
```bash
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123"}'
```

### Create Transaction
```bash
curl -b cookies.txt -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount":50000.00,"date":"2024-01-15","category":"Salary","description":"January Salary"}'
```

### Get Monthly Report
```bash
curl -b cookies.txt http://localhost:8080/api/reports/monthly/2024/1
```

---

## Deploying to Render

1. Push your code to a public GitHub repo
2. Go to [render.com](https://render.com) and create a new **Web Service**
3. Connect your GitHub repo
4. Set build command: `mvn clean package -DskipTests`
5. Set start command: `java -jar target/finance-manager-0.0.1-SNAPSHOT.jar`
6. Add a **PostgreSQL** database on Render and set environment variables:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
7. Add to `application.properties` to read env vars:
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

---

## Error Response Format

All errors return a simple JSON body:
```json
{ "message": "Category already exists" }
```

Status codes used: `400`, `401`, `403`, `404`, `409`
