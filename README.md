# 🛠️ ServiceDesk — Enterprise IT Support & Incident Management System

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Render-46E3B7?style=for-the-badge&logo=render&logoColor=white)](https://servicedesk-to0k.onrender.com/)
[![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Build & Tests](https://img.shields.io/badge/CI%2FCD-Passing-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/EpicS9/servicedesk/actions)

> 🌐 **Live Production Application:** **[https://servicedesk-to0k.onrender.com/](https://servicedesk-to0k.onrender.com/)**

A production-ready, full-stack enterprise incident and ticket management application built with **Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL / H2, JUnit 5 / Mockito, and GitHub Actions CI/CD**.

This project is purposefully architected to demonstrate **Java OOP, REST API Development, Relational SQL Analytics, Root Cause Troubleshooting, QA Bug Tracking, Automated Testing, and CI/CD Automation**.

---

## 🏛️ System Architecture

```text
                               ┌───────────────────────────────────┐
                               │   Interactive Web UI / Dashboard  │
                               │   (HTML5, CSS3, JavaScript ES6)   │
                               └─────────────────┬─────────────────┘
                                                 │
                                                 ▼
                               ┌───────────────────────────────────┐
                               │       Spring Boot REST API        │
                               │       (Controller Layer)          │
                               └─────────────────┬─────────────────┘
                                                 │
             ┌───────────────────────────────────┼───────────────────────────────────┐
             ▼                                   ▼                                   ▼
┌─────────────────────────┐         ┌─────────────────────────┐         ┌─────────────────────────┐
│     Ticket Service      │         │ Troubleshooting Service │         │    Analytics Service    │
│  (Lifecycle & Workflow) │         │   (Root Cause Analysis) │         │   (SQL KPIs & Reports)  │
└────────────┬────────────┘         └────────────┬────────────┘         └────────────┬────────────┘
             │                                   │                                   │
             └───────────────────────────────────┼───────────────────────────────────┘
                                                 │
                                                 ▼
                               ┌───────────────────────────────────┐
                               │      Spring Data JPA Repos        │
                               │     (Hibernate / JPQL / SQL)      │
                               └─────────────────┬─────────────────┘
                                                 │
                                                 ▼
                               ┌───────────────────────────────────┐
                               │   PostgreSQL / In-Memory H2 DB    │
                               └───────────────────────────────────┘
```

---

## ✨ Key Features & Technical Modules

### 1. Object-Oriented Domain Hierarchy (Java OOP)
- **`User` (Abstract Base Class)**: Demonstrates **Abstraction** with polymorphic operations (`getRoleDescription()`, `canBeAssignedTickets()`, `canTroubleshoot()`).
- **Inheritance & Subclasses**:
  - `Admin`: System administrator with elevated privileges.
  - `SupportEngineer`: Tier level (`Tier 1`, `Tier 2`), domain specialization, active queue tracking.
  - `Developer`: Engineering team assignment, tech stack profile, bug resolution capability.
  - `Employee`: Requester domain model with department and office location.
- **Encapsulation**: Domain entities manage their internal state and automatically log audit history upon modification.

### 2. Full Incident & Ticket Lifecycle
- Statuses: `OPEN` ➔ `IN_PROGRESS` ➔ `WAITING_FOR_USER` ➔ `RESOLVED` ➔ `CLOSED`.
- Priorities: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- Role-based authorization rules (e.g., employees cannot close tickets filed by other users).

### 3. Incident Troubleshooting & Root Cause Analysis (RCA)
- Support engineers can capture real-world diagnostic workflows:
  - **Problem Summary**: Symptoms reported by user.
  - **Investigation Trail**: Chronological steps executed (inspecting `/var/log/...`, validating connection pools, checking network sockets).
  - **Root Cause Analysis**: Exact technical defect identified (e.g., unclosed Hibernate session exhausting HikariCP connection pool).
  - **Resolution Applied**: Remediation steps taken (hotfix PR, configuration tuning).

### 4. QA Bug Tracking & Ticket Escalation
- Support tickets can be escalated to engineering as formal **Bug Reports**:
  - Bug Key (`BUG-302`), Severity (`CRITICAL`, `MAJOR`, `MINOR`, `TRIVIAL`), Target Environment (`Production`, `Staging`).
  - Step-by-step reproduction steps, Expected Behavior, Actual Behavior, and Stack Trace.
  - Assigned Developer and bug fix lifecycle (`OPEN` ➔ `IN_TRIAGE` ➔ `IN_FIX` ➔ `VERIFIED` ➔ `CLOSED`).

### 5. SQL Analytics & Reporting Engine
- Aggregates operational support metrics via custom SQL/JPQL:
  - Support Engineer workload distribution.
  - Unclosed tickets by priority & category breakdown.
  - Average resolution time across resolved incidents.
  - Escalation rate (% of support tickets converted to developer bugs).

### 6. Automated Testing Suite (JUnit 5 + Mockito + MockMvc)
- **Unit Tests**: Domain logic, state machine validation, permission checks, and troubleshooting workflows.
- **Integration Tests**: MockMvc REST API validation tests checking HTTP status codes (200, 201, 400, 404) and JSON responses.

### 7. Automated CI/CD Pipeline
- `.github/workflows/ci.yml` runs automated Maven test suite, verifies build integrity, and archives executable JAR artifacts on every push/PR.

---

## 🚀 Quick Start & How to Run

### Prerequisites
- Java 21 or higher installed (`java -version`)
- Maven (or run using `./mvnw` / IDE)

### 1. Run with In-Memory H2 Database (Default)
```bash
# Clone or navigate to the project directory
cd /home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk

# Run Spring Boot application
mvn spring-boot:run
```
- Web Application UI: **http://localhost:8080**
- H2 Database Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:servicedeskdb`, Username: `sa`, Password: *(empty)*)

### 2. Run with PostgreSQL (Production Profile)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 3. Run Automated Tests
```bash
mvn test
```

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tickets` | List all tickets (supports `status`, `priority`, `search` filters) |
| `POST` | `/api/tickets` | Create a new support ticket |
| `GET` | `/api/tickets/{id}` | Get ticket details with comments, history, and resolution log |
| `PUT` | `/api/tickets/{id}/status` | Update ticket status (`OPEN`, `IN_PROGRESS`, `RESOLVED`, etc.) |
| `PUT` | `/api/tickets/{id}/assign` | Assign ticket to support engineer or developer |
| `POST` | `/api/tickets/{id}/comments` | Add comment / internal support note |
| `POST` | `/api/tickets/{id}/troubleshooting` | Log troubleshooting RCA and mark ticket `RESOLVED` |
| `GET` | `/api/tickets/{id}/troubleshooting` | Fetch troubleshooting resolution log |
| `POST` | `/api/bugs/tickets/{id}/convert` | Convert support incident into a QA bug report |
| `GET` | `/api/bugs` | List all bug reports |
| `PUT` | `/api/bugs/{id}/status` | Update bug lifecycle status |
| `GET` | `/api/reports/analytics` | Get real-time KPI metrics, SLA averages, and workload |
| `GET` | `/api/users` | List active users and system personas |
| `GET` | `/api/users/assignable` | List users eligible for ticket assignment |

---

## 📂 Project Structure

```text
servicedesk/
├── .github/workflows/ci.yml           # GitHub Actions CI/CD Pipeline
├── src/
│   ├── main/
│   │   ├── java/com/servicedesk/
│   │   │   ├── domain/               # OOP Domain Model (User, Ticket, ResolutionLog, BugReport)
│   │   │   ├── dto/                  # Request/Response DTOs & Validation
│   │   │   ├── repository/           # Spring Data JPA Repositories & SQL Queries
│   │   │   ├── service/              # Business Logic & Workflows
│   │   │   ├── controller/           # REST API Controllers
│   │   │   ├── exception/            # Centralized Exception Handling
│   │   │   └── ServiceDeskApplication.java
│   │   └── resources/
│   │       ├── application.yml       # Multi-profile configuration (H2 & Postgres)
│   │       ├── schema-postgres.sql   # Production PostgreSQL DDL Schema
│   │       └── static/               # Interactive Web UI (HTML, CSS, JS)
│   └── test/
│       └── java/com/servicedesk/     # JUnit 5 & Mockito Test Suite
├── pom.xml                           # Maven Dependencies & Build Configuration
└── README.md
```
