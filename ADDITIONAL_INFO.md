
---

## 🏗️ Core Architectural Paradigms

### 1. Transparent B2B Multi-Tenancy
*   **Paradigam**: Shared database, shared schema model.
*   **Tenant Isolation**: Resolved dynamically via `TenantContext` using a ThreadLocal token storage injected on incoming HTTP request headers (`X-Tenant-ID` or extracted from authenticated JWT claims).
*   **AOP Database Injection**: An AspectJ aspect (`TenantFilterAspect`) intercepts Spring Data JPA repository operations, transparently activating Hibernate's `@Filter` (`tenantFilter`) with the dynamic `tenant_id` context parameter. This guarantees no leakage of records between separate industrial accounts.

### 2. Double-Auditing & Transaction Logs
*   **Audit Engine**: Critical state changes (stock punches, workforce adjustments, payroll approval locks, expense revisions) do not rely on implicit database hooks. Instead, they are explicitly logged within transactional boundaries using a custom `AuditLogService`. 
*   **Data Structure**: Changes are serialized to a dedicated `AuditLog` table using JSON formats, logging the operator, timestamp, action type, tenant context, and old/new state representations.

### 3. Stateless Security & Role-Based Access (RBAC)
*   **Authentication**: Stateless JWT authentication filter (`JwtFilter`) validating tokens on every request.
*   **Authorization**: Method-level security guards using Spring Security `@PreAuthorize` tags to block or permit routing based on custom roles:
    *   `OWNER`: Complete administrative privileges, system configuration, payroll release, and final expense reviews.
    *   `MANAGER`: Mid-tier management capabilities, shifts administration, attendance adjustments, and raw material catalog setups.
    *   `FLOOR_STAFF`: Restricted operational access, allowing material transaction punches (check-in/outs), attendance check-ins, and expense submissions.

---

## 🛠️ Technology Stack & Dependencies

*   **Runtime Environment**: Java 21 / 24 compatible (tested on JDK 24).
*   **Framework**: Spring Boot 3.2.x, Spring Data JPA, Spring Security.
*   **Database Engine**: PostgreSQL 16.
*   **Document Generation**: OpenPDF (in-memory PDF payslip binary generator).
*   **JSON Serialization**: Jackson DTOs.
*   **Orchestration**: Docker Compose.

---

## 🚀 Getting Started & Run Operations

### Option A: Isolated Docker Compose Orchestration (Recommended)
This approach automatically compiles, builds, migrates, and boots the entire platform using a multi-stage compilation flow.

1.  Ensure **Docker Desktop** or Docker Daemon is running on your host.
2.  Run the boot command in your terminal from the project's root folder:
    ```bash
    docker compose up --build -d
    ```
3.  The database schema will automatically migrate using the baseline [schema.sql](file:///c:/Users/KIIT0001/IdeaProjects/Fabricore/schema.sql).
4.  The REST API and frontend will be accessible directly on: **`http://localhost:8080`**
5.  To open the frontend directly in your default browser via PowerShell, run:
    ```powershell
    Start-Process "http://localhost:8080"
    ```
6.  To stop and shut down the active services gracefully:
    ```bash
    docker compose down
    ```
7.  **To shut down AND wipe the database clean** (erases the persistent PostgreSQL data volume to start fresh):
    ```bash
    docker compose down -v
    ```


### Option B: Local Native Maven Development
To run the Spring Boot application natively on your local machine:

1.  Ensure you have a PostgreSQL server running locally.
2.  Create a database named `fabricore` and configure your credentials inside [application.properties](file:///c:/Users/KIIT0001/IdeaProjects/Fabricore/src/main/resources/application.properties):
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/fabricore
    spring.datasource.username=postgres
    spring.datasource.password=postgres
    ```
3.  Compile and start the backend application via Maven:
    ```bash
    mvn clean spring-boot:run
    ```
4.  The application will start on port `8080` (endpoints base path `/api`).

---

## 🌐 Unified API Response Wrapper

All REST endpoints return responses wrapped inside a unified JSON object format:
```json
{
  "success": true,
  "message": "Operations message description",
  "data": { ... },
  "errors": null
}
```

---

## 📑 Complete REST API Directory

### 1. Authentication & Tenant Lifecycle (`/api/auth`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/register-tenant` | **Public** | `{ "username": "owner1", "email": "owner@mfg.com", "password": "securePass", "companyName": "Omega Factory" }` | Registers a new isolated Tenant entity, configures a unique `tenant_id` UUID, sets up the tenant's first `OWNER` account, and returns a dynamic JWT. |
| **POST** | `/api/auth/login` | **Public** | `{ "username": "owner1", "password": "securePass" }` | Authenticates users against the dynamic tenant storage, asserts the account `status` is `ACTIVE`, and returns a JWT claim holding the tenant boundary and RBAC roles. |

---

### 2. Supplier Management (`/api/suppliers`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/suppliers` | `OWNER`, `MANAGER` | `{ "name": "Steel Corp", "contactPerson": "John Doe", "phone": "+919876543210", "email": "john@steel.com" }` | Creates a new raw material supplier record inside the isolated tenant partition. |
| **GET** | `/api/suppliers` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10), `sortBy` ("name"), `direction` ("asc") | Returns a paginated catalog of all suppliers registered inside the tenant partition. |
| **GET** | `/api/suppliers/{id}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | None | Retrieves detailed contact information for a specific supplier by UUID. |
| **PUT** | `/api/suppliers/{id}` | `OWNER`, `MANAGER` | `{ "name": "Steel Corp LLC", "contactPerson": "John Doe Jr", "phone": "+919876543211", "email": "jr@steel.com" }` | Updates the detail columns of an active supplier. |
| **DELETE** | `/api/suppliers/{id}` | `OWNER`, `MANAGER` | None | Hard deletes a supplier from the database. Will fail if active materials map to this supplier. |

---

### 3. Raw Material Inventory (`/api/materials`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/materials` | `OWNER`, `MANAGER` | `{ "sku": "STL-REBAR-12", "name": "12mm Steel Rebar", "unit": "TONS", "reorderLevel": 5.00, "supplierId": "uuid-here" }` | Registers a new Raw Material SKU ledger item. Initial quantity starts at `0.00` and is subsequently incremented via auditable Stock Transactions. |
| **GET** | `/api/materials` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10), `sortBy` ("sku") | Returns a paginated list of active raw material SKUs. Transparently excludes soft-deleted items. |
| **GET** | `/api/materials/{id}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | None | Retrieves a raw material's specific details, quantities, and reorder alerts. |
| **GET** | `/api/materials/low-stock` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10) | Filters and returns a page of active materials where `quantity` is less than or equal to `reorderLevel`. |
| **PUT** | `/api/materials/{id}` | `OWNER`, `MANAGER` | `{ "sku": "STL-REBAR-12", "name": "12mm Steel Rebar", "unitOfMeasure": "TONS", "quantity": 25.00, "reorderLevel": 8.00, "supplierId": "uuid-here" }` | Updates material parameters and reorder warning configurations. |
| **DELETE** | `/api/materials/{id}` | `OWNER`, `MANAGER` | None | Performs a secure **Soft Delete** on the raw material SKU, updating its delete state flag so it remains preserved in stock transaction histories but excluded from listing requests. |

---

### 4. Stock Ledger Transactions (`/api/transactions`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/transactions` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | `{ "materialId": "uuid-here", "quantity": 5.50, "type": "STOCK_IN", "notes": "Received load" }` | Registers a stock movement punch. **Important Transaction Rules**: <br>• `STOCK_IN` increments the raw material's quantity.<br>• `STOCK_OUT` decrements the quantity, enforcing locks to prevent negative balances.<br>• The operation executes within a serialization lock boundary, writing detailed ledger entries. |
| **GET** | `/api/transactions/material/{materialId}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10), `sortBy` ("timestamp") | Returns a detailed history ledger of all stock-in/out punches for a targeted raw material, documenting the staff member who authorized the movement. |
| **GET** | `/api/transactions/{id}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | None | Retrieves details for an individual transaction punch. |

---

### 5. Workforce Employee Roster (`/api/employees`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/employees` | `OWNER`, `MANAGER` | `{ "firstName": "Alice", "lastName": "Smith", "email": "alice@mfg.com", "phone": "9876543210", "salaryModel": "DAILY", "baseSalary": 1200.00, "shiftId": "uuid-here" }` | Registers a new employee profile. <br>• `salaryModel` options: `FIXED` (monthly rate) or `DAILY` (rate computed by active daily shift punches). |
| **GET** | `/api/employees` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10) | Returns a paginated list of all active employee profiles. Automatically screens soft-deleted records. |
| **GET** | `/api/employees/{id}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | None | Retrieves the full demographic profile, current shift association, and base salary rules of an employee. |
| **PUT** | `/api/employees/{id}` | `OWNER`, `MANAGER` | `{ "firstName": "Alice", "lastName": "Smith", "email": "alice@mfg.com", "phone": "9876543210", "salaryModel": "FIXED", "baseSalary": 45000.00, "shiftId": "uuid-here" }` | Updates employee parameters, such as changing salary structures or shift rosters. |
| **DELETE** | `/api/employees/{id}` | `OWNER`, `MANAGER` | None | Soft-deletes an employee profile, disabling active shift association and check-in eligibility while preserving historical attendance, advance loans, and payroll sheets. |

---

### 6. Shift Administration (`/api/shifts`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/shifts` | `OWNER`, `MANAGER` | `{ "name": "Morning Shift", "startTime": "08:00:00", "endTime": "16:00:00", "gracePeriodMinutes": 15 }` | Registers a new operational timetable shift. `gracePeriodMinutes` determines late penalty boundaries. |
| **GET** | `/api/shifts` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10) | Retrieves a paginated list of active shift rules config. |
| **GET** | `/api/shifts/{id}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | None | Retrieves shift parameters. |
| **PUT** | `/api/shifts/{id}` | `OWNER`, `MANAGER` | `{ "name": "Morning Shift", "startTime": "07:30:00", "endTime": "15:30:00", "gracePeriodMinutes": 15 }` | Updates the start/end bounds of a shift. |
| **DELETE** | `/api/shifts/{id}` | `OWNER`, `MANAGER` | None | Deletes a shift profile. Fails if employees are currently mapped to this shift. |

---

### 7. Attendance Clock-in & Corrections (`/api/attendance`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/attendance/check-in` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | `{ "employeeId": "uuid-here", "shiftId": "uuid-here", "checkInTime": "2026-06-01T08:05:00" }` | Records a check-in punch. **Safety Rule**: Double check-in locks prevent an employee from logging a check-in if there is an active check-in without a check-out. Evaluates `gracePeriodMinutes` to set `isLate` flags automatically. |
| **POST** | `/api/attendance/check-out/employee/{employeeId}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | None | Records a check-out punch. Searches for the employee's active open check-in, calculates time differences, and computes total hours. |
| **PUT** | `/api/attendance/correct/{id}` | `OWNER`, `MANAGER` | `{ "correctedCheckInTime": "2026-06-01T08:00:00", "correctedCheckOutTime": "2026-06-01T16:00:00", "notes": "Forgot to card punch" }` | **Supervisor Override**: Allows manual corrections to check-in/out timestamps. Changes are logged to the transactional audit system. |
| **GET** | `/api/attendance/summary/{employeeId}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `month` ("2026-06") | Retrieves headcount stats for an employee, including Total Days Worked, Late Days, and Total Hours compiled. |
| **GET** | `/api/attendance` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10) | Lists paginated attendance logs. |

---

### 8. Advance Salary Loans (`/api/advances`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/advances` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | `{ "employeeId": "uuid-here", "amount": 15000.00, "reason": "Medical emergency", "repaymentMonth": "2026-06" }` | Requests an advance loan. repays automatically by deducting from payroll calculation for the target repayment month. Limit controls assert that the advance amount cannot exceed 50% of the employee's base monthly wage. |
| **PUT** | `/api/advances/{id}/approve` | `OWNER`, `MANAGER` | *Query Params*: `status` ("APPROVED" or "REJECTED") | Approves or rejects a pending advance request. On approval, sets repayment hooks for the next payroll cycle. |
| **GET** | `/api/advances/employee/{employeeId}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10) | Lists all advance requests and balances for a targeted employee. |
| **GET** | `/api/advances` | `OWNER`, `MANAGER` | *Query Params*: `page` (0), `size` (10) | Lists all advance requests in the tenant partition. |

---

### 9. Payroll Engine & PDF Payslips (`/api/payroll`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/payroll/calculate` | `OWNER`, `MANAGER` | `{ "employeeId": "uuid-here", "payrollMonth": "2026-06" }` | **Payroll Arithmetic Engine**: Computes monthly draft payroll records. <br>• *For Fixed Employees*: Base salary is prorated, deducting absences.<br>• *For Daily Wage Employees*: Multiplies daily base rate by the count of active attendance check-outs.<br>• Automatically resolves and applies active `AdvanceSalary` loan repayments.<br>• Inserts a record in a `DRAFT` status. |
| **PUT** | `/api/payroll/{id}/approve` | `OWNER`, `MANAGER` | None | Transitions a payroll record from `DRAFT` to `APPROVED` status, locking parameters from further recalculations. |
| **PUT** | `/api/payroll/{id}/pay` | `OWNER`, `MANAGER` | None | Transitions a payroll record from `APPROVED` to `PAID` status, releasing disbursal processes. |
| **GET** | `/api/payroll/employee/{employeeId}` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0) | Retrieves an employee's historical payroll cards. |
| **GET** | `/api/payroll/{id}/payslip` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | None | **Payslip Generator**: Resolves the payroll calculations by UUID, compiles an in-memory PDF payslip complete with headers, breakdown calculations, and metadata utilizing **OpenPDF**, and streams the bytes directly back as a download. |

---

### 10. Operational Expenses (`/api/expenses`)

Submit-to-Compete workflow: **`SUBMITTED`** *(logged by staff)* $\rightarrow$ **`APPROVED`** *(authorized by manager/owner)* $\rightarrow$ **`COMPLETED`** *(reviewed by owner)*.

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/expenses` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | `{ "description": "High-voltage line utility bill", "amount": 8450.00, "category": "UTILITIES", "department": "Processing", "project": "Phase 2 Core" }` | Submits a factory operational expense. `category` options: `UTILITIES`, `MAINTENANCE`, `TRANSPORT`, `MISC`. |
| **PUT** | `/api/expenses/{id}/approve` | `OWNER`, `MANAGER` | None | Transitions a `SUBMITTED` expense record to `APPROVED` status. |
| **PUT** | `/api/expenses/{id}/review` | `OWNER` | None | Transitions an `APPROVED` expense record to `COMPLETED` status, finalizing the audit chain. |
| **PUT** | `/api/expenses/{id}/reject` | `OWNER`, `MANAGER` | None | Transitions a `SUBMITTED` or `APPROVED` expense to `REJECTED` status. |
| **GET** | `/api/expenses/summary` | `OWNER`, `MANAGER` | *Query Params*: `month` ("2026-06") | Returns consolidated expense reports summing up total approved spends, and providing breakdowns by Category and Department. |
| **GET** | `/api/expenses` | `OWNER`, `MANAGER`, `FLOOR_STAFF` | *Query Params*: `page` (0), `size` (10) | Lists paginated expense logs. |

---

### 11. Operations Dashboard Metrics & Data Exports (`/api/dashboard`)

| Method | Path | Role required | Request Body Schema | Description & Behavioral Rules |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/api/dashboard` | `OWNER`, `MANAGER` | *Query Params*: `month` ("2026-06") | Compiles real-time metrics across all domains (active workforce headcount, monthly payroll costs, total approved/reviewed expense burn, and critical low-stock item counts). |
| **GET** | `/api/dashboard/export` | `OWNER`, `MANAGER` | *Query Params*: `month` ("2026-06") | **Consolidated Reporter**: Generates a consolidated manufacturing operations report formatted as a CSV byte stream. Captures overview indicators, raw material inventories, and category expenses. |

---

## 🔒 Security Authentication Flow (cURL Walkthrough)

To consume secure REST APIs from external clients:

### 1. Register a Tenant Factory and Owner
```bash
curl -X POST http://localhost:8080/api/auth/register-tenant \
  -H "Content-Type: application/json" \
  -d '{"username":"chief1","email":"chief@omega.com","password":"mypassword","companyName":"Omega Manufacturing"}'
```
*   Save the `token` and `tenantId` from the returned JSON response.

### 2. Login to Retrieve a JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"chief1","password":"mypassword"}'
```

### 3. Fetch Roster Profiles using JWT and Tenant Context
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer <your-jwt-token-here>" \
  -H "X-Tenant-ID: <your-tenant-uuid-here>"
```
