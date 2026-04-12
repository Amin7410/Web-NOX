# NOX Platform

**A backend-driven collaborative system design platform built with Java Spring Boot.**
Status: Active development

---

## Overview

NOX Platform is a multi-tenant web application that allows teams to design and organize software systems through a structured visual workspace.

From a backend perspective, this project focuses on:

* Designing and building RESTful APIs
* Implementing authentication and access control
* Managing multi-tenant data models
* Ensuring system scalability and maintainability

The system is divided into two main parts:

* **Portal**: handles user management, authentication, and organization-level operations
* **Studio**: provides the workspace where system components (blocks) are created and connected
    Frontend is still under development and not yet fully functional

---

## Key Backend Features

### Authentication & Security

* Implemented using **Spring Security**
* JWT-based authentication (access + refresh token)
* Two-Factor Authentication (TOTP)
* OAuth2 login (Google, etc.)
* Role-based access control (RBAC)

### Multi-Tenancy

* Organization-based data isolation
* Each request carries organization context
* Access control enforced at service layer

### REST API Design

* Built with **Spring Boot 3.3**
* JSON-based communication
* Clear separation between controller, service, and domain layers
* Input validation and structured error handling

### Database & Persistence

* PostgreSQL 16 as primary database
* Schema managed via **Flyway migration**
* Optimized queries and indexing strategies
* Soft delete handling with explicit control

### Caching & Performance

* Redis used for:

  * Caching frequently accessed data
  * Temporary storage for tokens and OTP
* Background jobs for syncing usage metrics

### Testing & Debugging

* Unit testing for service layer
* Debugging and validation of API flows
* Structured logging for traceability

---

## System Design (Simplified)

The system follows a **modular monolith architecture**, organized into independent modules:

| Module    | Responsibility                                    |
| --------- | ------------------------------------------------- |
| iam       | Authentication, authorization, user security      |
| tenant    | Organizations, roles, membership                  |
| engine    | Core business logic (projects, blocks, relations) |
| warehouse | Reusable templates and definitions                |
| shared    | Common utilities (time, audit, events)            |

Each module has:

* Domain layer (business logic)
* Service layer (use cases)
* Infrastructure layer (database, external services)
* API layer (controllers)

---

## Core Concepts

To support system design, NOX introduces a structured data model:

### Block

Represents a component in a system (e.g., service, database, API).

### Relation

Represents connections between components (e.g., dependency, data flow).

### Invader

Represents additional properties or behaviors of a component
(e.g., caching enabled, authentication required).

> These concepts are used to structure and store system design data.
> They are not executable code, but serve as a blueprint for understanding and extending systems.

---

## Tech Stack

| Layer      | Technology                   |
| ---------- | ---------------------------- |
| Backend    | Java 21, Spring Boot 3.3     |
| Security   | Spring Security, JWT, OAuth2 |
| Database   | PostgreSQL 16                |
| Cache      | Redis                        |
| Migration  | Flyway                       |
| Build Tool | Gradle (Kotlin DSL)          |
| Frontend   | Next.js, React, Vite         |
| DevOps     | Docker, Docker Compose       |

---

## Running the Project

### Prerequisites

* Docker & Docker Compose
* Java 21
* Node.js 20+ and PNPM

### Start Infrastructure

```bash
docker-compose -f docker/docker-compose.yml up -d
```

### Run Backend

```bash
cd backend
./gradlew bootRun
```

* Swagger UI: http://localhost:8081/swagger-ui.html
* Health Check: http://localhost:8081/api/health

### Run Frontend

```bash
pnpm install
pnpm dev
```

---

## What I Learned

* Designing scalable REST APIs using Spring Boot
* Implementing secure authentication systems (JWT, 2FA, OAuth2)
* Managing multi-tenant architectures
* Working with PostgreSQL, Redis, and Flyway in real-world scenarios
* Structuring a modular backend system for maintainability

---

## Notes

This project is developed as a personal and team learning project, with a focus on backend engineering.
All architectural decisions were made with maintainability, scalability, and clarity in mind.
