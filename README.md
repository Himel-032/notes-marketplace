# 📘 Digital Notes Marketplace

> A role-based academic marketplace where students can upload, browse, purchase, and download study notes through a secure Spring Boot web platform.

![Java](https://img.shields.io/badge/Java-17-red)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Server--Side%20UI-darkgreen)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)
![Tests](https://img.shields.io/badge/Tests-Passing-success)

---

## 👨‍💻 Authors

- Md Himel
- Ariyan Aftab Spandan

---

## 📌 Project Description

Digital Notes Marketplace is a full-stack web application designed to help students share and monetize academic resources in a structured and secure way. The platform allows users to upload notes, explore available study materials, purchase premium content, and download notes after successful payment.

The system is built around a role-based access model:

- **Admin** manages users, notes, platform analytics, and administrative operations.
- **Seller** uploads notes, manages note listings, and monitors note sales.
- **Buyer** browses notes, places orders, completes payments, and downloads purchased notes.

---

## ✨ Features

### Core Features
- Role-based dashboards for Admin, Seller, and Buyer
- Secure login and registration (BCrypt password hashing)
- Note upload with PDF storage via Cloudinary
- Browse, search, and filter notes
- SSLCommerz payment gateway integration
- Order creation and download management

### New Features (Latest Release)
- **Sales Analytics** — Admin can view total users, notes, revenue, and sales charts over time
- **Profile Editing** — All users (Admin, Seller, Buyer) can update their name, phone, and password via `/profile`
- **Order Number System** — Orders are assigned human-readable numbers (e.g. `ORD-0001`) displayed in order history
- **Admin Self-Disable Protection** — Admin cannot disable their own account from User Management

---

## 🏗️ Architecture Overview

The application follows a **layered architecture** to keep responsibilities clean, maintainable, and testable.

### Core Layers

| Layer | Responsibility |
|---|---|
| **Controller** | Accepts HTTP requests, validates incoming data, returns views or API responses |
| **Service** | Contains business logic such as authentication, note management, payments, and order processing |
| **Repository** | Communicates with the database using Spring Data JPA |
| **Model** | Represents persistent domain entities such as `User`, `Note`, `Order`, and `Role` |
| **DTO** | Transfers structured data between backend layers and frontend/API consumers |

### Request Flow

```text
User → Controller → Service → Repository → Database
```

---

## 🧠 Design Patterns Used

| Pattern | Where Used |
|---|---|
| **Service Layer** | `AuthService`, `NoteService`, `OrderService`, `PaymentService` |
| **Repository** | `UserRepository`, `NoteRepository`, `OrderRepository` |
| **DTO** | `NoteDto`, `AuthResponse`, `RegisterRequest`, `AdminUserDto` |
| **MVC** | Controllers + Thymeleaf templates + JPA models |
| **Singleton** | All Spring beans (controllers, services, repositories) |
| **Strategy** | `PaymentServiceImpl` (payment strategy), `NoteServiceImpl` (browse modes) |

---

## 🗄️ Database Design

### Main Tables

| Table | Purpose |
|---|---|
| **users** | Stores user profile, email, phone, password, and account state |
| **roles** | Stores role definitions: `ADMIN`, `SELLER`, `BUYER` |
| **notes** | Stores note metadata, price, PDF URL, and seller reference |
| **orders** | Stores purchase details including `orderNumber`, total price, and transaction ID |
| **order_items** | Stores purchased note entries linked to an order |

### Relationships

- **User ↔ Role**: Many-to-Many
- **User → Note**: One-to-Many (seller uploads)
- **User → Order**: One-to-Many (buyer purchases)
- **Order → OrderItem**: One-to-Many
- **Note → OrderItem**: One-to-Many

---

## 🔐 Authentication & Security

| Feature | Detail |
|---|---|
| Spring Security | Route protection and role-based authorization |
| BCrypt | Secure password hashing |
| RBAC | Strict role-to-route mapping for Admin, Seller, Buyer |
| Admin Protection | Admin cannot disable own account |
| Custom Success Handler | Redirects each role to its respective dashboard after login |

---

## 🌐 API Endpoints

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`

### Notes
- `GET /api/buyer/notes`
- `GET /api/buyer/notes/search?keyword=...`
- `GET /api/buyer/notes/filter?category=...`
- `GET /api/buyer/notes/{id}`
- `POST /seller/notes/upload`
- `PUT /seller/notes/{id}`
- `DELETE /seller/notes/{id}`

### Orders
- `GET /api/buyer/my-orders`
- `GET /api/buyer/my-downloads`
- `POST /api/payment/success`

### Admin
- `GET /api/admin/users`
- `DELETE /api/admin/users/{id}`
- `PUT /api/admin/users/{id}/status`
- `GET /api/admin/notes`
- `DELETE /api/admin/notes/{id}`
- `GET /api/admin/analytics`

### Profile (all authenticated roles)
- `GET /profile`
- `POST /profile/update`

For the complete route reference, see `API_ENDPOINTS.md`.

---

## 💳 Payment Integration

The project integrates **SSLCommerz** for online payment processing.

1. A buyer selects a note.
2. The application creates a payment session via SSLCommerz API.
3. The buyer is redirected to the payment gateway.
4. On success, the application creates an order with a unique `orderNumber` and stores purchased note details.
5. The buyer can then access and download the purchased note.

---

## 🧪 Testing

### Tools
- JUnit 5, Mockito, MockMvc, Spring Boot Test, H2 (test DB)

### Coverage Areas
- Service layer (Auth, Admin, Note, Order, Payment)
- Controller integration tests (all major endpoints)
- Repository query validation
- End-to-end marketplace flow

```bash
./mvnw test
```

---

## 🐳 Docker Setup

```bash
docker compose up --build
```

Starts **PostgreSQL 16** and the **Spring Boot app** on port `8080`.

### Required `.env` Variables

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/notesmarketplace
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
SSLCOMMERZ_STORE_ID=...
SSLCOMMERZ_STORE_PASSWORD=...
SSLCOMMERZ_BASE_URL=https://sandbox.sslcommerz.com
SSLCOMMERZ_SUCCESS_URL=http://localhost:8080/api/payment/success
SSLCOMMERZ_FAIL_URL=http://localhost:8080/api/payment/fail
SSLCOMMERZ_CANCEL_URL=http://localhost:8080/api/payment/cancel
```

---

## 🚀 Tech Stack

| Category | Technology |
|---|---|
| Backend | Spring Boot 4, Spring MVC, Spring Data JPA, Spring Security |
| Frontend | Thymeleaf, Bootstrap 5, HTML, CSS, JavaScript |
| Database | PostgreSQL 16 |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Payment | SSLCommerz |
| File Storage | Cloudinary |
| DevOps | Docker, Docker Compose, Maven |

---

## 🔑 Demo Credentials

After starting the application, a default admin account is automatically created:

| Field | Value |
|---|---|
| **Role** | Admin |
| **Email** | `admin@gmail.com` |
| **Password** | `admin123` |

> You can register Buyer and Seller accounts through the `/register` page.

---

## 📂 Project Highlights

- Role-based dashboards for Admin, Seller, and Buyer
- Secure login and registration with BCrypt
- Upload, browse, buy, and download notes
- SSLCommerz payment gateway integration
- Sales analytics with revenue charts
- Human-readable order numbers (`ORD-0001`, `ORD-0002`, ...)
- Full profile editing for all roles
- Admin self-disable protection
- DTO-based API responses
- Unit and integration test coverage
- Dockerized deployment support

---

## 🎓 Academic Value

This project demonstrates practical application of:

- Software design patterns (Service Layer, Repository, Strategy, MVC)
- Layered system architecture
- Secure full-stack web development
- REST API design
- Payment gateway integration
- Automated testing and verification
- Containerized deployment

Suitable for **university project evaluation**, **software engineering coursework**, and **GitHub portfolio presentation**.


---

## 📌 Project Description

Digital Notes Marketplace is a full-stack web application designed to help students share and monetize academic resources in a structured and secure way. The platform allows users to upload notes, explore available study materials, purchase premium content, and download notes after successful payment.

The system is built around a role-based access model:

- **Admin** manages users, platform oversight, and administrative operations.
- **Seller** uploads notes, manages note listings, and monitors note sales.
- **Buyer** browses notes, places orders, completes payments, and downloads purchased notes.

The project is suitable for both academic evaluation and professional portfolio presentation because it combines layered backend design, security, payment integration, testing, and containerized deployment.

---

## 🏗️ Architecture Overview

The application follows a **layered architecture** to keep responsibilities clean, maintainable, and testable.

### Core Layers

| Layer | Responsibility |
|---|---|
| **Controller** | Accepts HTTP requests, validates incoming data, returns views or API responses |
| **Service** | Contains business logic such as authentication, note management, payments, and order processing |
| **Repository** | Communicates with the database using Spring Data JPA |
| **Model** | Represents persistent domain entities such as `User`, `Note`, `Order`, and `Role` |
| **DTO** | Transfers structured data between backend layers and frontend/API consumers |

### Request Flow

```text
User → Controller → Service → Repository → Database
```

### Example Flow

When a buyer requests available notes:

1. The **Buyer Controller** receives the request.
2. The **Note Service** applies business rules, search/filter logic, and DTO mapping.
3. The **Note Repository** fetches note data from the database.
4. The service maps `Note` entities into `NoteDto` responses.
5. The response is returned to the frontend for rendering.

This separation improves readability, testing, and future scalability.

---

## 🧠 Design Patterns Used

The project applies several important software engineering patterns to keep the code organized and extensible.

### 1. Service Layer Pattern

**What it is:**
The Service Layer Pattern isolates business logic from controllers and persistence logic.

**Where used:**
- `AuthService`
- `NoteService` / `NoteServiceImpl`
- `OrderService` / `OrderServiceImpl`
- `PaymentService` / `PaymentServiceImpl`

**Why used:**
It keeps controllers thin, makes unit testing easier, and centralizes application rules in one place.

### 2. Repository Pattern

**What it is:**
The Repository Pattern abstracts database access behind dedicated interfaces.

**Where used:**
- `UserRepository`
- `NoteRepository`
- `OrderRepository`
- `OrderItemRepository`
- `RoleRepository`

**Why used:**
It reduces boilerplate persistence code and keeps data access separate from business logic.

### 3. DTO Pattern

**What it is:**
The DTO Pattern uses dedicated data objects to transfer only the data required by the client.

**Where used:**
- `NoteDto`
- `AuthResponse`
- `RegisterRequest`
- `LoginRequest`
- `PaymentRequest`

**Why used:**
It prevents direct exposure of entity internals, keeps API responses clean, and supports frontend-friendly payloads such as note sales count.

### 4. MVC Pattern

**What it is:**
Model-View-Controller separates business data, UI rendering, and request handling.

**Where used:**
- **Model:** `User`, `Note`, `Order`, `OrderItem`, `Role`
- **View:** Thymeleaf templates under `src/main/resources/templates`
- **Controller:** `AuthController`, `BuyerController`, `SellerNoteController`, `PaymentController`, `WebController`

**Why used:**
It keeps the frontend and backend responsibilities well organized and easier to maintain.

### 5. Singleton Pattern (Spring Beans)

**What it is:**
In Spring, beans are singleton by default, meaning one shared instance is managed by the container.

**Where used:**
- Controllers
- Services
- Repositories
- Security configuration classes

**Why used:**
It improves memory efficiency, supports dependency injection, and provides consistent application-wide behavior.

### 6. Factory Pattern

**What it is:**
The Factory Pattern centralizes object or strategy selection instead of scattering conditional logic across the codebase.

**Where used:**
- `PaymentServiceImpl` uses a registry-backed factory to select payment strategy behavior.
- `NoteServiceImpl` uses a factory-like registry for browse modes such as all, search, and filter.

**Why used:**
It improves extensibility and keeps selection logic centralized and clean.

### 7. Strategy Pattern (SSLCommerz Payment)

**What it is:**
The Strategy Pattern encapsulates interchangeable behaviors behind a common contract.

**Where used:**
- `PaymentServiceImpl` applies strategy-based payment session initialization logic.
- `NoteServiceImpl` applies strategy objects for browsing notes in different modes.

**Why used:**
It allows the system to support different payment or browsing behaviors without changing controller code or duplicating logic.

---

## 🗄️ Database Design

The project uses a relational database with entity relationships mapped through JPA/Hibernate.

### Main Tables

| Table | Purpose |
|---|---|
| **User** | Stores user profile, email, password, and account state |
| **Role** | Stores role definitions such as `ADMIN`, `SELLER`, `BUYER` |
| **Note** | Stores note title, description, price, file URL, preview URL, and seller |
| **Order** | Stores purchase transaction details including total price and transaction ID |
| **OrderItem** | Stores purchased note entries linked to an order |

### Relationships

- **User ↔ Role**: Many-to-Many  
  A user can have one or more roles, and a role can belong to many users.

- **User → Note**: One-to-Many  
  One seller can upload many notes.

- **User → Order**: One-to-Many  
  One buyer can place many orders.

- **Order → OrderItem**: One-to-Many  
  One order can contain one or more items.

- **Note → OrderItem**: One-to-Many  
  One note can appear in many order items over time.

### Conceptual Flow

```text
User (Seller) ──< Note
User (Buyer)  ──< Order ──< OrderItem >── Note
User >──< Role
```

---

## 🔐 Authentication & Security

Security is implemented using **Spring Security** with form login and role-based authorization.

### Security Features

- **Spring Security** protects routes and enforces role-based access.
- **BCrypt password encryption** is used for secure password storage.
- **Role-based access control** restricts pages and APIs for Admin, Seller, and Buyer users.
- **Custom login success handling** redirects users to the correct dashboard based on role.

### Access Model

| Role | Access |
|---|---|
| **Admin** | Admin dashboard and administrative APIs |
| **Seller** | Note upload, note management, seller dashboard |
| **Buyer** | Note browsing, ordering, payments, downloads |

This design ensures that each user only interacts with the features relevant to their role.

---

## 🌐 API Endpoints

Below is a simplified overview of the main application APIs.

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`

### Notes

Core note-related endpoints in the current implementation:

- `GET /api/buyer/notes`
- `GET /api/buyer/notes/search?keyword=...`
- `GET /api/buyer/notes/filter?category=...`
- `GET /api/buyer/notes/{id}`
- `POST /seller/notes/upload`
- `PUT /seller/notes/{id}`
- `DELETE /seller/notes/{id}`

If documented in abstract CRUD form, these correspond conceptually to:

- `GET /api/notes`
- `POST /api/notes`
- `PUT /api/notes/{id}`
- `DELETE /api/notes/{id}`

### Orders

The project handles order information primarily through buyer and payment flows:

- `GET /api/buyer/my-orders`
- `GET /api/buyer/my-downloads`
- `POST /api/payment/success`

Conceptually, these align with:

- `POST /api/orders`
- `GET /api/orders/{id}`

### Admin

Current implementation includes admin dashboard and admin APIs such as user and note management under:

- `GET /api/admin/users`
- `DELETE /api/admin/users/{id}`

For a complete route list, see `API_ENDPOINTS.md`.

---

## 💳 Payment Integration

The project integrates **SSLCommerz** for online payment processing.

### How It Works

1. A buyer selects a note.
2. The application creates a payment session.
3. The buyer is redirected to the SSLCommerz gateway.
4. On successful payment, the application creates an order and stores purchased note details.
5. The buyer can then access and download the purchased note.

### Strategy Pattern Usage

Payment behavior is organized with the **Strategy Pattern**, which makes the payment flow easier to extend in the future if another gateway is introduced.

### Why This Design Matters

- Keeps payment code isolated from unrelated business logic
- Makes the integration easier to test
- Supports future gateway expansion with minimal controller changes

---

## 🧪 Testing

The project includes both **unit tests** and **integration tests**.

### Unit Tests

Focus areas:

- Service layer business logic
- Authentication logic
- Note sales-count logic
- Order creation and purchased-note retrieval
- Payment service behavior with external gateway calls mocked

### Integration Tests

Focus areas:

- Controller endpoints with `MockMvc`
- Authentication request/response flow
- Seller and buyer endpoints
- Payment success callback and persisted order creation
- Repository query validation for note sales count

### Tools Used

- **JUnit 5**
- **Mockito**
- **MockMvc**
- **Spring Boot Test**
- **H2 Database** for test environment

### Current Status

The project test suite passes successfully with:

```bash
./mvnw test
```

---

## 🐳 Docker Setup

The project includes both a `Dockerfile` and `docker-compose.yml` for containerized execution.

### Run with Docker Compose

```bash
docker compose up --build
```

### What This Starts

- **PostgreSQL 16** container for persistent database storage
- **Spring Boot application** container on port `8080`

### Required Environment Variables

Make sure your `.env` file provides:

- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_DB`

### Default Ports

| Service | Port |
|---|---|
| Spring Boot App | `8080` |
| PostgreSQL | `5432` |

---

## 🚀 Tech Stack

| Category | Technology |
|---|---|
| Backend | Spring Boot, Spring MVC, Spring Data JPA, Spring Security |
| Frontend | Thymeleaf, HTML, CSS, Bootstrap, JavaScript |
| Database | PostgreSQL |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Payment | SSLCommerz |
| DevOps | Docker, Docker Compose, Maven |

---

## 📂 Project Highlights

- Role-based dashboards for Admin, Seller, and Buyer
- Secure login and registration system
- Upload, browse, buy, and download notes
- Payment gateway integration using SSLCommerz
- Sales analytics with note purchase count
- DTO-based API responses
- Tested backend with unit and integration coverage
- Dockerized deployment support

---

## 🎓 Academic Value

This project demonstrates practical application of:

- Software design patterns
- Layered system architecture
- Secure full-stack web development
- REST API design
- Payment gateway integration
- Automated testing and verification
- Containerized deployment

It is well suited for **university project evaluation**, **software engineering coursework**, and **GitHub portfolio presentation**.

---

## 📬 Closing Note

Digital Notes Marketplace is a strong example of how modern Spring Boot applications can combine security, clean architecture, real-world business flow, and maintainable testing in a student-focused product.

If you want, I can also generate:

1. a shorter GitHub-style README version,
2. a version with screenshots/placeholders,
3. or a README with installation and local setup commands added in more detail.