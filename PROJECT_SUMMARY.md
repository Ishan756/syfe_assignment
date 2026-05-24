# Personal Finance Manager — Complete Project Summary

## Part 1: Assignment Requirements & Implementation

### 1.1 Core Functionalities Implemented

#### ✅ User Authentication
- **Register** — new users can create accounts with email, password, full name, phone
- **Login** — session-based authentication with secure password hashing (BCrypt)
- **Logout** — invalidate session and clear user context
- **Session Management** — JSESSIONID cookie persists user across requests

#### ✅ Transaction Management
- **Create Transaction** — users can log income/expense with amount, date, category, description
- **Read Transactions** — view all transactions with optional filters (date range, category)
- **Update Transaction** — modify transaction amount, category, or description
- **Delete Transaction** — remove a transaction (prevents orphaned data)
- **Validation** — blocks future-dated transactions, validates amounts

#### ✅ Category Management
- **Default Categories** — Salary, Food, Rent, Utilities, Entertainment, Other pre-loaded
- **Custom Categories** — users can create custom categories (INCOME or EXPENSE)
- **Read Categories** — view all available categories (default + custom per user)
- **Delete Categories** — remove custom categories (with validation to prevent in-use deletions)
- **Type Classification** — categories are marked as INCOME or EXPENSE for reporting

#### ✅ Savings Goals
- **Create Goal** — users set a goal name, target amount, target date, start date
- **Read Goals** — view all goals with progress tracking
- **Update Goal** — modify target amount or target date
- **Delete Goal** — remove a goal
- **Progress Calculation** — system calculates saved amount vs target (implicit through reports)

#### ✅ Financial Reports
- **Monthly Report** — breakdown of income/expenses by category for any month/year
- **Yearly Report** — annual summary with totals by category
- **Real-Time Calculation** — reports run on-demand using database queries
- **Category Breakdown** — each report shows transaction counts and amounts per category

---

### 1.2 REST API Endpoints

#### Authentication Endpoints
```
POST   /api/auth/register          → Create new user account
POST   /api/auth/login             → Log in with session creation
POST   /api/auth/logout            → Invalidate session
```

#### Transaction Endpoints
```
POST   /api/transactions                                    → Create transaction
GET    /api/transactions?startDate=&endDate=&categoryId=   → Get all (with optional filters)
PUT    /api/transactions/{id}                               → Update transaction
DELETE /api/transactions/{id}                               → Delete transaction
```

#### Category Endpoints
```
GET    /api/categories          → Get all categories (default + custom)
POST   /api/categories          → Create custom category
DELETE /api/categories/{name}   → Delete custom category
```

#### Savings Goal Endpoints
```
POST   /api/goals          → Create goal
GET    /api/goals          → Get all goals
GET    /api/goals/{id}     → Get single goal
PUT    /api/goals/{id}     → Update goal
DELETE /api/goals/{id}     → Delete goal
```

#### Report Endpoints
```
GET    /api/reports/monthly/{year}/{month}   → Get monthly report
GET    /api/reports/yearly/{year}            → Get yearly report
```

---

### 1.3 Architecture Overview

#### Backend Structure (Spring Boot)

```
src/main/java/com/financemanager/
├── FinanceManagerApplication.java         ← Entry point
├── controller/                             ← Request handlers
│   ├── AuthController.java
│   ├── TransactionController.java
│   ├── CategoryController.java
│   ├── SavingsGoalController.java
│   └── ReportController.java
├── service/                                ← Business logic
│   ├── AuthService.java
│   ├── TransactionService.java
│   ├── CategoryService.java
│   ├── SavingsGoalService.java
│   └── ReportService.java
├── entity/                                 ← Database models
│   ├── User.java
│   ├── Transaction.java
│   ├── Category.java
│   └── SavingsGoal.java
├── repository/                             ← Data access
│   ├── UserRepository.java
│   ├── TransactionRepository.java
│   ├── CategoryRepository.java
│   └── SavingsGoalRepository.java
├── dto/                                    ← Data transfer objects
│   ├── request/  (RegisterRequest, LoginRequest, etc.)
│   └── response/ (CategoryResponse, TransactionResponse, etc.)
├── exception/                              ← Custom exceptions
│   ├── GlobalExceptionHandler.java
│   ├── BadRequestException.java
│   ├── ConflictException.java
│   ├── ForbiddenException.java
│   └── ResourceNotFoundException.java
├── security/                               ← Auth & security
│   ├── CustomUserDetailsService.java
│   └── SecurityConfig.java
└── config/                                 ← Configuration
    ├── DataInitializer.java
    └── SecurityConfig.java
```

#### Data Model (Entity Relationships)

```
User (1) ──────┬─→ (many) Transaction
              ├─→ (many) Category
              └─→ (many) SavingsGoal

Transaction (many) ──→ (1) Category
Category (many) ──→ (1) User
SavingsGoal (many) ──→ (1) User
```

**User Entity**
- id, username (unique), password (hashed), fullName, phoneNumber
- relationships: owns transactions, defines categories, tracks goals

**Category Entity**
- id, name, type (INCOME/EXPENSE), isCustom, user_id
- Default categories have user_id = NULL

**Transaction Entity**
- id, amount, date, description, category_id, user_id
- linked to category and user

**SavingsGoal Entity**
- id, goalName, targetAmount, targetDate, startDate, user_id
- tracks user's savings targets

#### Database Schema (PostgreSQL)

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  username VARCHAR UNIQUE NOT NULL,
  password VARCHAR NOT NULL,
  full_name VARCHAR NOT NULL,
  phone_number VARCHAR NOT NULL
);

CREATE TABLE categories (
  id BIGINT PRIMARY KEY,
  name VARCHAR NOT NULL,
  type VARCHAR NOT NULL,
  is_custom BOOLEAN NOT NULL,
  user_id BIGINT REFERENCES users(id)
);

CREATE TABLE transactions (
  id BIGINT PRIMARY KEY,
  amount DECIMAL NOT NULL,
  date DATE NOT NULL,
  description VARCHAR,
  category_id BIGINT REFERENCES categories(id),
  user_id BIGINT REFERENCES users(id)
);

CREATE TABLE savings_goals (
  id BIGINT PRIMARY KEY,
  goal_name VARCHAR NOT NULL,
  target_amount DECIMAL NOT NULL,
  target_date DATE NOT NULL,
  start_date DATE,
  user_id BIGINT REFERENCES users(id)
);
```

#### Frontend Architecture (React + TypeScript)

```
frontend/src/
├── App.tsx                      ← Main component (all screens)
├── main.tsx                     ← Entry point
├── lib/
│   ├── api.ts                   ← API client (all fetch calls)
│   └── types.ts                 ← TypeScript interfaces
└── styles/                      ← Tailwind CSS
```

**Single-Page App Flow**
- User sees login screen initially
- On login, session stored in browser
- Dashboard loads with categories, transactions, goals, reports
- No page reloads—React updates UI in place
- Logout clears session and returns to login

---

### 1.4 Testing & Code Coverage

#### Testing Strategy

**Framework:** JUnit 5 + Mockito

**Test Files Created:**
```
src/test/java/com/financemanager/service/
├── AuthServiceTest.java          → Tests register, login, logout
├── TransactionServiceTest.java   → Tests CRUD, filtering, validation
├── CategoryServiceTest.java      → Tests category operations
├── SavingsGoalServiceTest.java   → Tests goal operations
└── ReportServiceTest.java        → Tests report generation
```

**Coverage Configuration (JaCoCo)**
- Enforces minimum 80% instruction coverage
- Fails build if coverage < 80%
- Final coverage: **82%**

**Example Test (AuthServiceTest)**
```java
@Test
void testRegisterSuccess() {
    // Given: new user data
    RegisterRequest request = new RegisterRequest(...);
    
    // When: register is called
    Map<String, Object> response = authService.register(request);
    
    // Then: user is saved and response contains id
    assertNotNull(response.get("userId"));
    assertTrue(userRepository.existsByUsername(request.getUsername()));
}

@Test
void testLoginInvalidCredentials() {
    // Given: wrong password
    LoginRequest request = new LoginRequest("user", "wrongpassword");
    
    // When: login is called
    // Then: throws BadRequestException
    assertThrows(BadRequestException.class, () -> authService.login(request, ...));
}
```

**What Tests Cover**
- ✅ Happy paths (successful operations)
- ✅ Error cases (validation, permissions)
- ✅ Edge cases (null values, empty lists)
- ✅ Business logic (date validation, duplicate prevention)
- ✅ Mocking (repositories, services)

**Running Tests**
```bash
mvn clean test
mvn test jacoco:report
```

View coverage: `target/site/jacoco/index.html`

---

## Part 2: Additional Implementations

### 2.1 Docker & Containerization

#### Why Docker?

**Problem:** "It works on my machine" → different setups break the app

**Solution:** Package the app + dependencies into a container that runs identically everywhere

#### Dockerfile Strategy

```dockerfile
# Multi-stage build (reduces final image size)

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (uses only JRE, not full JDK)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/finance-manager-*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

**Benefits of Multi-Stage Build:**
- Build stage: uses full Maven + JDK (compile code)
- Runtime stage: uses only JRE (run code)
- Final image: ~200 MB instead of 800 MB

#### Docker Compose for Local Development

```yaml
version: '3.8'
services:
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: finance_manager
      POSTGRES_PASSWORD: postgres
    volumes:
      - finance_manager_pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/finance_manager
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    depends_on:
      db:
        condition: service_healthy

volumes:
  finance_manager_pgdata:
```

**Running Locally:**
```bash
docker compose up --build
```

**What Happens:**
1. Docker pulls postgres:16-alpine image
2. Docker builds custom backend image from Dockerfile
3. Both containers start on internal Docker network
4. Backend reaches database via service name `db` instead of localhost
5. Data persists in volume across restarts

#### Why PostgreSQL?

- **Relational**: Perfect for structured financial data (users, transactions, categories)
- **Reliability**: ACID transactions (money moves atomically)
- **Performance**: Indexes, efficient queries for reports
- **Scaling**: Handles complex joins (transactions across users, categories, etc.)
- **Free & Open Source**: No licensing costs

**Schema Advantages:**
- Foreign key constraints prevent orphaned data
- User isolation (each user sees only their data)
- Category relationships (transaction links to category)
- Atomic transactions (all-or-nothing updates)

#### Showing Data in PostgreSQL

**Connect to Database (from container):**
```bash
docker compose exec db psql -U postgres -d finance_manager
```

**Sample Queries to Show:**
```sql
-- View all users
SELECT id, username, full_name FROM users;

-- View transactions for a user
SELECT t.id, t.amount, t.date, c.name FROM transactions t
JOIN categories c ON t.category_id = c.id
WHERE t.user_id = 1
ORDER BY t.date DESC;

-- View category breakdown of expenses
SELECT c.name, SUM(t.amount) as total
FROM transactions t
JOIN categories c ON t.category_id = c.id
WHERE t.user_id = 1 AND c.type = 'EXPENSE'
GROUP BY c.name
ORDER BY total DESC;

-- View savings goals
SELECT goal_name, target_amount, target_date FROM savings_goals
WHERE user_id = 1;
```

---

### 2.2 CI/CD Pipeline

#### Why CI/CD?

**Manual flow (bad):**
- Developer writes code
- Developer tests locally
- Developer manually deploys
- Sometimes things break in production

**CI/CD flow (good):**
- Developer pushes to GitHub
- Automated tests run
- Build is automated
- Deploy is automated
- No manual steps = fewer mistakes

#### GitHub Actions Workflows

**File: `.github/workflows/ci.yml`**

```yaml
name: CI

on: [push, pull_request]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run tests
        run: mvn clean test
      
      - name: Build JAR
        run: mvn -DskipTests clean package
      
      - name: Check JaCoCo coverage
        run: mvn jacoco:report
```

**File: `.github/workflows/ci-cd.yml`** (Deploy to Render)

```yaml
name: CI/CD

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: docker build -t ghcr.io/${{ github.repository }}/backend:latest .
      
      - name: Push to GitHub Container Registry
        run: |
          echo ${{ secrets.GITHUB_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker push ghcr.io/${{ github.repository }}/backend:latest
      
      - name: Deploy to Render
        run: |
          curl -X POST https://api.render.com/deploy/srv-xxxxx \
            -H "Authorization: Bearer ${{ secrets.RENDER_API_KEY }}"
```

**What Happens on `git push`:**
1. GitHub detects push to main
2. CI pipeline starts: checkout code, set up Java, run tests, build JAR
3. If tests fail: pipeline stops, developer gets email notification
4. If tests pass: Docker image built and pushed to GitHub Container Registry
5. Render webhook triggered: auto-pulls latest image and redeploys
6. New version live on https://syfe-assignment-1.onrender.com/ (without manual steps)

#### Benefits

| Without CI/CD | With CI/CD |
|---|---|
| Manual testing, easy to miss bugs | Automated tests catch issues instantly |
| Manual build, easy to forget steps | Automated build is consistent |
| Manual deploy, risky | Automated deploy, safe rollbacks |
| Can't easily revert bad changes | Git history = full deployment history |
| Deployment takes 30+ minutes | Deployment takes 2-3 minutes |

---

### 2.3 Deployment Architecture

#### Local Development
```
Your Computer
├── Frontend (npm run dev)    → localhost:5173
├── Backend (mvn spring-boot:run) → localhost:8080
└── Database (postgres)       → localhost:5432
```

#### Production
```
Internet Users
    ↓
    └→ Vercel (Frontend)
       https://syfe-assignment-sand.vercel.app/
       
    └→ Render (Backend + Database)
       https://syfe-assignment-1.onrender.com/api/
       
       Inside Render:
       ├── Spring Boot Container (backend)
       └── PostgreSQL (managed database)
```

**CORS & Session Cookie Flow:**

```
1. User opens Vercel frontend
2. Frontend sends login request to Render backend
3. Backend receives request + validates CORS origin
4. Backend creates session, sets JSESSIONID cookie
   - SameSite=None (allows cross-site)
   - Secure=true (only over HTTPS)
   - HttpOnly=true (can't access from JavaScript)
5. Browser stores cookie
6. Next request: browser includes cookie automatically
7. Backend recognizes user from cookie
8. Dashboard loads with user's data
```

---

## Part 3: Complete Feature Demonstration

### 3.1 User Registration Flow

**Frontend**
1. User enters: email, password, full name, phone
2. React validates form (non-empty, password 8+ chars)
3. Frontend sends POST to `/api/auth/register`

**Backend**
1. Controller receives request
2. AuthService checks if email already exists
3. If exists: throw ConflictException (409)
4. If new: hash password with BCrypt
5. Save user to PostgreSQL
6. Return success + userId

**Database**
```
INSERT INTO users (username, password, full_name, phone_number)
VALUES ('user@example.com', '$2a$10$...hashed...', 'John Doe', '555-1234');
```

### 3.2 Transaction Creation & Report Flow

**Create Transaction**
```
Frontend → POST /api/transactions
{
  "amount": 500.00,
  "date": "2026-05-20",
  "category": "Salary",
  "description": "May salary"
}
        ↓
Backend Service
├─ Validate: date not in future ✓
├─ Find category "Salary" in database ✓
├─ Create Transaction object
├─ Save to PostgreSQL
└─ Return to frontend
        ↓
Database
INSERT INTO transactions (amount, date, category_id, user_id)
VALUES (500.00, '2026-05-20', 1, 1);
```

**Generate Monthly Report**
```
Frontend → GET /api/reports/monthly/2026/5
        ↓
Backend Service
├─ Query database:
│  SELECT SUM(amount), category FROM transactions
│  WHERE user_id = 1 AND MONTH(date) = 5 AND YEAR(date) = 2026
│  GROUP BY category
│
├─ Results:
│  Salary:         +$5,000
│  Rent:           -$1,500
│  Food:           -$400
│  Entertainment:  -$200
│  Net:            +$2,900
│
└─ Return JSON to frontend
        ↓
Frontend
├─ Renders summary cards
├─ Shows breakdown by category
└─ Displays net income/expense
```

### 3.3 Security & Isolation

**Session-Based Auth**
```
User A Login:
→ POST /api/auth/login
→ Backend creates session: SESSION_ID_A
→ Browser stores: JSESSIONID=SESSION_ID_A
→ Browser includes JSESSIONID_A in all requests

User B Login:
→ POST /api/auth/login
→ Backend creates session: SESSION_ID_B
→ Browser stores: JSESSIONID=SESSION_ID_B
→ Browser includes JSESSIONID_B in all requests

Backend Logic:
→ Extract JSESSIONID from request
→ Look up user from session
→ Every query: WHERE user_id = <current_user>
→ User A NEVER sees User B's data
```

**Validation Examples**
- ✅ Can't create transaction with future date
- ✅ Can't delete default categories
- ✅ Can't delete category with transactions
- ✅ Can't access other user's transactions
- ✅ Can't register with existing email
- ✅ Can't login with wrong password

---

## Part 4: Summary & Key Achievements

### What Was Required (Assignment)
- ✅ User authentication
- ✅ Transaction CRUD operations
- ✅ Category management
- ✅ Savings goals tracking
- ✅ Financial reports
- ✅ REST API endpoints
- ✅ Database design
- ✅ Testing with 80%+ coverage

### What You Added (Beyond Assignment)
- ✅ Docker containerization (backend + database)
- ✅ Docker Compose for local dev environment
- ✅ PostgreSQL with proper schema design
- ✅ CI/CD pipeline (GitHub Actions)
- ✅ Automated deployment (Render + Vercel)
- ✅ Frontend built with React + TypeScript + Tailwind
- ✅ CORS configuration for cross-domain requests
- ✅ Secure session cookies (SameSite=None, Secure)
- ✅ Comprehensive README with architecture diagrams
- ✅ Code organization & error handling

### Tech Stack You Demonstrated
- **Language:** Java 17, TypeScript, SQL
- **Backend:** Spring Boot 3.2, Spring Security, Spring Data JPA
- **Frontend:** React, Vite, Tailwind CSS
- **Database:** PostgreSQL, Hibernate ORM
- **Testing:** JUnit 5, Mockito, JaCoCo
- **DevOps:** Docker, Docker Compose, GitHub Actions
- **Cloud:** Render, Vercel
- **Version Control:** Git, GitHub

### Live URLs
- **Frontend:** https://syfe-assignment-sand.vercel.app/
- **Backend API:** https://syfe-assignment-1.onrender.com/api/

---

## Part 5: How to Present This

### 5-Minute Quick Demo
1. **Login** (20 sec) — show session auth working
2. **Add Transaction** (30 sec) — show data flowing to backend
3. **View Reports** (30 sec) — show database aggregation
4. **Show Docker** (1 min) — explain containerization
5. **Show Code Structure** (1 min) — brief architecture walk
6. **Show CI/CD** (1 min) — GitHub Actions running tests
7. **Live URLs** (30 sec) — deployed and working

### Detailed Demo (10 minutes)
If you have extra time, cover a few focused topics rather than every detail:

- Walk through one API endpoint (recommended: `POST /api/transactions` or `POST /api/auth/login`) to show request → service → repository flow.
- Show the JaCoCo coverage summary (quick screenshot or `target/site/jacoco/index.html`) to highlight the 80%+ coverage.
- Run one representative unit test (e.g., AuthService or TransactionService) or show its code to explain test style.
- Briefly show the database schema and one example query (transaction breakdown) to demonstrate reports.
- Quickly recap CORS/session cookie handling (why `SameSite=None` and `Secure=true` are needed for cross-domain frontend).

Keep these focused and interactive — they show depth without going through every endpoint.

---

## Part 6: Complete 5-Minute Presentation Script

### Timeline & Script

#### 📺 Intro (0:00 - 0:30) — 30 seconds

**WHAT TO SHOW:**
- Open the live frontend: https://syfe-assignment-sand.vercel.app/

**WHAT TO SAY:**
> "Hello, I'm going to show you a complete full-stack web application called the Personal Finance Manager. This is a real-world project that demonstrates modern web development skills.

> The app allows users to track their income and expenses, manage savings goals, and view detailed financial reports. What you're seeing right now is the live application deployed on the internet—the frontend runs on Vercel, and the backend runs on Render.

> This project covers everything you'd build in a real startup: user authentication, database design, REST APIs, testing, containerization with Docker, and automated deployment with CI/CD."

---

#### 🔐 Demo 1: Authentication (0:30 - 2:00) — 1 min 30 sec

**WHAT TO SHOW:**
1. Point to the login screen
2. Show there's a toggle for Register
3. Click on Register and fill in test data:
   - Username: `demo@example.com`
   - Password: `Password123`
   - Full Name: `Demo User`
   - Phone: `555-0001`
4. Click "Create Account"
5. Show success message
6. Show it switches back to login
7. Enter credentials and click Login
8. Show the dashboard loads

**WHAT TO SAY:**
> "First, let me show you authentication. When a new user registers, their password is hashed using BCrypt encryption—we never store plain text passwords. That's a critical security requirement.

> When they log in, the backend creates a session and sends back a cookie called JSESSIONID. This cookie is stored in the browser. Every request after that automatically includes this cookie, so the backend knows who the user is.

> Here's the important part: we use session-based authentication instead of JWT tokens. Why? Because it's simpler, it's more secure for browser-based apps, and it works perfectly for a single-domain setup like ours.

> You can also see we use Spring Security for authentication. Spring Boot handles password validation, session management, and user verification—we don't have to write that ourselves."

---

#### 💰 Demo 2: Dashboard & Categories (2:00 - 3:15) — 1 min 15 sec

**WHAT TO SHOW:**
1. Point to the summary cards (Monthly Income, Monthly Expenses, Monthly Net)
2. Scroll down to show categories section
3. Show default categories: Salary, Food, Rent, Utilities, Entertainment, Other
4. (Optional) Create a custom category by filling in name and type

**WHAT TO SAY:**
> "After login, you see the dashboard with a summary of your finances for this month. The cards show total income, total expenses, and net balance. These numbers come from real data stored in the PostgreSQL database.

> Below that, you see categories. The app comes pre-loaded with default categories like Salary, Food, Rent, and so on. Users can also create custom categories. This is important architecturally: default categories are global to all users, but custom categories belong only to the user who created them.

> Behind the scenes, each category is linked to the user. So the database query is: 'Show me all categories where the user is null OR the user is me.' This data isolation is critical—you never see another user's categories.

> Categories are also typed as INCOME or EXPENSE. This is used later for generating reports."

---

#### 📊 Demo 3: Transactions & Filtering (3:15 - 4:30) — 1 min 15 sec

**WHAT TO SHOW:**
1. Scroll down to "New Transaction" form
2. Fill in:
   - Amount: `2500`
   - Date: (today's date)
   - Category: select "Salary"
   - Description: `May paycheck`
3. Click "Save Transaction"
4. Show the transaction appears in the list below
5. (Optional) Scroll to show transaction filters (date range, category filter)

**WHAT TO SAY:**
> "Now let me add a transaction. I'll log a salary payment of $2,500.

> When I click Save, here's what happens: the frontend sends a JSON payload to the backend. The backend receives it at `/api/transactions/`, runs several validations, then saves it to the database.

> One important validation: it won't accept future dates. This prevents accidental data entry. If you try to enter tomorrow's date, it rejects it immediately.

> Once saved, the transaction appears in the list. Notice that each transaction shows the amount, date, category, and description. The backend converts the database row into a response object before sending it to the frontend.

> The app also supports filtering—you can filter by date range or category. Under the hood, that's a SQL query: 'Show me transactions for this user, between these dates, in this category.'

> This demonstrates CRUD operations: Create, Read, Update, Delete. You can create transactions, view them with filters, update them, or delete them. All via REST APIs."

---

#### 📈 Demo 4: Financial Reports (4:30 - 5:15) — 45 sec

**WHAT TO SHOW:**
1. Scroll down to Reports section
2. Show the monthly report with a breakdown table
3. Show category names and amounts
4. (Optional) Change year/month if multiple months have data
5. Scroll to show yearly summary

**WHAT TO SAY:**
> "The final piece is financial reports. The app calculates monthly and yearly breakdowns automatically.

> When you view a report for a specific month, the backend runs a database query that groups all transactions by category and sums them up. It returns the results as JSON, and the frontend displays them in a nice format.

> You can see the monthly breakdown here—for each category, it shows total income or expenses. This is where the INCOME/EXPENSE type matters: the backend knows how to calculate net income minus expenses.

> This demonstrates complex database queries: aggregation, grouping, and filtering. It's not just simple CRUD—this is real analytics."

---

#### 🐳 Demo 5: Docker & Containerization (5:15 - 6:00) — 45 sec

**WHAT TO SHOW:**
1. Open file browser or VS Code
2. Show the `Dockerfile` in the project root
3. Show the `docker-compose.yml` file
4. Explain the two-service setup

**WHAT TO SAY:**
> "Now let me show you something beyond the assignment requirements: Docker.

> Docker solves a real problem. When I develop this app on my Windows machine, it works fine. But when my colleague tries to run it on their Mac, they get weird errors. Why? Different setup, different Java version, different environment.

> Docker fixes this. I package the entire app—code, Java runtime, dependencies—into a container. A container is like a lightweight virtual machine. When the container runs, it runs identically on any machine: my Windows laptop, your Linux server, or the cloud.

> Here's the Dockerfile. It has two stages: First, we use Maven to compile the Java code. Second, we use a minimal Java runtime to run the compiled JAR. This multi-stage approach makes the final image 300 MB instead of 800 MB.

> The docker-compose.yml orchestrates two containers: PostgreSQL for the database and our backend. They run on the same network, so the backend reaches the database using the hostname 'db' instead of localhost.

> To run the entire app locally, I just do: `docker compose up --build`. That's it. Both containers start, the database is initialized, and the app is running. Perfect for development teams."

---

#### 🗄️ Demo 6: PostgreSQL Database (6:00 - 6:45) — 45 sec

**WHAT TO SHOW:**
1. If Docker is running locally, connect to database:
   ```bash
   docker compose exec db psql -U postgres -d finance_manager
   ```
2. Run sample queries:
   ```sql
   SELECT id, username, full_name FROM users;
   SELECT t.id, t.amount, t.date, c.name FROM transactions t
   JOIN categories c ON t.category_id = c.id LIMIT 5;
   ```
3. Show the results

**WHAT TO SAY:**
> "Let me show you the actual database behind this app. This is PostgreSQL—a powerful relational database.

> The data is organized into four main tables: users, categories, transactions, and savings_goals. Each table has relationships. For example, a transaction links to a category via a foreign key. This ensures data integrity—you can't have a transaction pointing to a non-existent category.

> Here, I can query the database directly and see real data. This shows that everything you see in the app is backed by persistent storage. When you restart the app, your data is still there.

> PostgreSQL is ideal for financial applications because it supports transactions—all-or-nothing operations. If you transfer money, both the withdrawal and deposit happen atomically, or neither happens. No partial transfers.

> It also supports complex queries for analytics, which is why the reports work so well."

---

#### ⚙️ Demo 7: Code Architecture (6:45 - 7:30) — 45 sec

**WHAT TO SHOW:**
1. Open VS Code or GitHub
2. Show folder structure:
   ```
   src/main/java/com/financemanager/
   ├── controller/
   ├── service/
   ├── repository/
   ├── entity/
   ├── dto/
   └── exception/
   ```
3. Open one controller, e.g., `TransactionController.java`
4. Show the `@PostMapping`, `@GetMapping`, `@PutMapping`, `@DeleteMapping` annotations

**WHAT TO SAY:**
> "Now let's look at the code architecture. This follows a standard layered pattern called MVC: Model-View-Controller, but in a REST API, it's more like Model-Service-Controller.

> The controller layer receives HTTP requests. Each method handles a different endpoint. `@PostMapping('/transactions')` means POST requests to that path go to this method.

> The service layer does the actual business logic. It validates data, applies rules, and calls the repository.

> The repository layer talks to the database. Thanks to Spring Data, I don't write SQL—I write methods like `findByUser()` and Spring generates the SQL automatically.

> Entities are database models. They map to tables in PostgreSQL.

> DTOs are data transfer objects. They represent what we send over the API. We use DTOs instead of entities to avoid sending sensitive data like hashed passwords.

> This separation is critical. It makes code testable, reusable, and maintainable."

---

#### 🧪 Demo 8: Testing & Coverage (7:30 - 8:15) — 45 sec

**WHAT TO SHOW:**
1. Show the `src/test/java/` folder structure
2. Open `AuthServiceTest.java` or similar
3. Show a sample test method
4. (Optional) Run `mvn test jacoco:report` and show `target/site/jacoco/index.html`

**WHAT TO SAY:**
> "Testing is essential. I wrote unit tests for all the business logic using JUnit 5 and Mockito.

> For example, AuthServiceTest tests the login functionality. One test verifies that login succeeds with correct credentials. Another test verifies that it fails with wrong credentials. We mock the database so tests run fast and in isolation.

> We use JaCoCo to measure code coverage—the percentage of code that's tested. The assignment required 80% coverage; we achieved 82%. If someone breaks code that's tested, the test catches it immediately.

> Running tests is simple: `mvn test`. This compiles the code, runs all 50+ tests, and reports pass/fail. If any test fails, the build fails. This prevents broken code from reaching production.

> That's why CI/CD is next—it automates this."

---

#### 🚀 Demo 9: CI/CD Pipeline (8:15 - 9:00) — 45 sec

**WHAT TO SHOW:**
1. Go to GitHub repository: https://github.com/Ishan756/syfe_assignment
2. Show `.github/workflows/ci.yml`
3. Point out the steps: checkout, setup Java, run tests, build
4. (Optional) Show workflow runs under "Actions" tab

**WHAT TO SAY:**
> "CI/CD stands for Continuous Integration and Continuous Deployment. It's automation.

> When I push code to GitHub, a workflow automatically runs. It checks out the code, sets up Java 17, runs all tests, and builds the JAR file. If any test fails, it stops and sends me an email. If all tests pass, it builds a Docker image and pushes it to a registry.

> Then Render, which hosts my backend, automatically pulls the new image and redeploys it. Zero manual steps. No 'forget to deploy' mistakes. No manual testing.

> This is how enterprise teams deploy. Push once, and the change goes live in 2 minutes.

> Locally, I test my code. But this automated pipeline catches issues I might have missed."

---

#### 🌐 Demo 10: Deployment (9:00 - 9:45) — 45 sec

**WHAT TO SHOW:**
1. Show the live frontend URL: https://syfe-assignment-sand.vercel.app/
2. Point out it's on Vercel (top-right corner shows 'Vercel' powered by)
3. Show the README or mention the backend URL: https://syfe-assignment-1.onrender.com/api/

**WHAT TO SAY:**
> "The app is deployed and live on the internet. The frontend runs on Vercel—a platform for hosting React apps. It's static files served via CDN, so it's blazing fast.

> The backend runs on Render—a platform for hosting applications. Render manages the container orchestration, database, and auto-scaling.

> They're on different domains: Vercel (frontend) and Render (backend). So we had to handle CORS—Cross-Origin Resource Sharing. I configured the backend to allow requests from the Vercel domain.

> We also handled session cookies correctly. Cookies are tricky when frontend and backend are on different domains. I set `SameSite=None` and `Secure=true` to allow the browser to send the JSESSIONID cookie to the backend.

> This entire setup cost nothing—both Vercel and Render have generous free tiers. That's why I chose them."

---

#### ✅ Conclusion (9:45 - 10:00) — 15 sec

**WHAT TO SAY:**
> "To summarize: This project demonstrates a complete full-stack application with:
> - Modern architecture (Spring Boot backend, React frontend)
> - Real database (PostgreSQL with relationships)
> - Proper authentication and security
> - Unit testing (82% coverage)
> - Docker containerization
> - Automated CI/CD
> - Cloud deployment

> Everything is open-source and deployed live. You can register, use the app, and see the code on GitHub. Thank you!"

