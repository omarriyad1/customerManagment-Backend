# Customer Management System

A full-stack customer management solution consisting of a **Spring Boot REST API** (backend) and a **JavaFX desktop application** (client). The desktop app communicates with the backend exclusively over HTTP — it has zero direct database access.

---

## Table of Contents

- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Backend — Spring Boot API](#backend--spring-boot-api)
    - [Configuration](#configuration)
    - [Running the Backend](#running-the-backend)
    - [API Endpoints](#api-endpoints)
- [Desktop App — JavaFX Client](#desktop-app--javafx-client)
    - [Running the Desktop App](#running-the-desktop-app)
- [How They Communicate](#how-they-communicate)
- [First-Time Setup](#first-time-setup)

---

## Project Structure

```
customer-management/
│
├── customerManagment/              # Spring Boot backend (REST API)
│   ├── src/main/java/org/task/customermanagment/
│   │   ├── Controller/             # REST controllers (Auth, Customer)
│   │   ├── Service/                # Business logic
│   │   ├── Repository/             # JPA repositories
│   │   ├── Model/                  # JPA entities (Customers, AppUser)
│   │   ├── Dto/                    # Request / Response DTOs
│   │   ├── Exception/              # Custom exceptions + GlobalExceptionHandler
│   │   ├── Security/               # JWT filter, JwtUtil, SecurityConfig
│   │   └── mapper/                 # CustomerMapper
│   ├── src/main/resources/
│   │   └── application.properties  # DB, JWT, server config
│   └── pom.xml
│
└── customer-desktop/               # JavaFX desktop client
    ├── src/main/java/
    │   ├── Main.java               # JavaFX entry point
    │   ├── Api/
    │   │   ├── ApiClient.java      # All HTTP calls to the backend
    │   │   └── ApiException.java   # HTTP error wrapper
    │   ├── model/                  # DTOs mirrored from backend
    │   │   ├── CustomerRequestDto.java
    │   │   └── CustomerResponseDto.java
    │   └── view/
    │       ├── LoginView.java      # Login screen
    │       ├── MainView.java       # Customer table + toolbar
    │       └── CustomerFormDialog.java  # Add / Edit dialog
    └── pom.xml
```

---

## Architecture Overview

```
┌─────────────────────────────┐    HTTP + JWT     ┌──────────────────────────┐
│     JavaFX Desktop App      │ ───────────────►  │   Spring Boot REST API   │
│                             │                   │                          │
│  LoginView                  │  POST /auth/login │  AuthController          │
│  MainView  (TableView)      │  GET  /customers  │  CustomerController      │
│  CustomerFormDialog         │  POST /customers  │  CustomerService         │
│  ApiClient (HttpClient)     │  PUT  /customers  │  JwtAuthFilter           │
│                             │  DELETE /customer │  SecurityConfig          │
└─────────────────────────────┘                   └────────────┬─────────────┘
       No DB access ever                                       │ JPA
                                                    ┌──────────▼─────────────┐
                                                    │     MySQL DB       │
                                                    │  customers, users       │
                                                    └────────────────────────┘
```

**Every data operation goes through the API.** The desktop app holds no database driver, credentials, or direct connection of any kind.

---

## Prerequisites

| Tool  | Version | Notes |
|-------|---------|-------|
| Java  | 21+     | Required by both apps |
| Maven | 3.9+    | Build tool for both apps |
| MySQL  | 8+      | Backend database |

---

## Backend — Spring Boot API

### Configuration

Open `customerManagment/src/main/resources/application.properties` and fill in your values:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/customer_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=org.mysql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# JWT — must be at least 32 characters (256 bits)
jwt.secret=my-super-secret-key-that-is-long-enough-for-hs256
jwt.expiration=86400000
```

SQL Schema
```sql
CREATE DATABASE customer_managment;

USE customer_managment;

CREATE TABLE customers (
       id INT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(100),
       email VARCHAR(100),
       phone VARCHAR(50),
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(255) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL,
       role VARCHAR(255) NOT NULL
);
```

---

### Running the Backend

```bash
# 1. Navigate to the backend directory
cd customerManagment

# 2. Build the project
mvn clean install

# 3. Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

To verify it's running:

```bash
curl http://localhost:8080/api/v1/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

---

### API Endpoints

#### Auth (public — no token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/login` | Login and receive a JWT |

**Register example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123", "role": "ADMIN"}'
```

**Login example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123"}'
```

#### Customers (JWT required)

| Method | Endpoint                 | Role Required | Description                                        |
|--------|--------------------------|--------------|----------------------------------------------------|
| `GET` | `/api/v1/customers`      | USER, ADMIN | Get all customers                                  |
| `GET` | `/api/v1/customers/{id}` | USER, ADMIN | Get customer by ID                                 |
| `POST` | `/api/v1/customers`      | ADMIN | Create a customer                                  |
| `PUT` | `/api/v1/customers/{id}` | ADMIN | Update a customer                                  |
| `DELETE` | `/api/v1/customers/{id}` | ADMIN | Delete a customer                                  |
| `GET` | `/api/v1/customers/paginated`     | USER, ADMIN | Get all customers pagination and filtering applied |
Pass the token from login in every request:
```bash
curl http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer <your-token-here>"
```

---

## Desktop App — JavaFX Client

### Running the Desktop App

> The backend **must be running** before you launch the desktop app.

```bash
# 1. Navigate to the desktop project directory
cd customer-desktop

# 2. Build the project
mvn clean install

# 3. Run the application
mvn javafx:run
```

The login window will appear. Use the credentials you registered via the API.

---

## How They Communicate

The desktop app never touches the database. All operations flow through `ApiClient.java`, which uses Java's built-in `HttpClient` to make HTTP requests to the Spring Boot API.

**Login flow:**
```
User enters credentials
       │
       ▼
ApiClient.login()  ──►  POST /api/v1/auth/login
                                    │
                         Returns JWT token
                                    │
                  Token stored in ApiClient instance
                  for all subsequent requests
```

**Every subsequent request:**
```
User action (e.g. Add Customer)
       │
       ▼
ApiClient.createCustomer()
       │
       ├── Serializes DTO to JSON  (Jackson)
       ├── Attaches Authorization: Bearer <token> header
       └── POST /api/v1/customers
                    │
         Spring validates JWT in JwtAuthFilter
                    │
         Saves to DB via CustomerService
                    │
         Returns CustomerResponseDto as JSON
                    │
       ApiClient deserializes JSON → DTO
                    │
       MainView refreshes the TableView
```

**Error handling:** If the API is unreachable or returns a `4xx`/`5xx` response, `ApiClient` throws an `ApiException`. The desktop app catches it and shows an alert dialog to the user — it never crashes silently.

---

## First-Time Setup

Follow these steps in order on a fresh machine:

```
1. Start PostgreSQL and create the database
   └── CREATE DATABASE customer_db;

2. Configure application.properties with your DB credentials and JWT secret

3. Start the Spring Boot backend
   └── cd customerManagment && mvn spring-boot:run

4. Register your first admin user
   └── POST /api/v1/auth/register
       {"username":"admin","password":"yourpassword","role":"ADMIN"}

5. Launch the desktop application
   └── cd customer-desktop && mvn javafx:run

6. Log in with the credentials you just registered
```