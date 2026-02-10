# NOX Platform

**Simulation & Code Generation Platform**

NOX Platform is a system for building "Digital Blueprints" for software, supporting logic simulation and AI-driven code generation.

## Architecture

- **Backend**: Java 21, Spring Boot 3.2.2, Modular Monolith Architecture.
- **Frontend**: Hybrid Monorepo (TurboRepo + PNPM).
  - `portal`: Next.js 14 (SEO & Dashboard) - Port 3000.
  - `studio`: React Vite (Graph Editor/Canvas) - Port 5173.
  - `@nox/ui`: Shared UI Component Library.
- **Database**: PostgreSQL 16 managed by Flyway Migrations (V1-V5).

## Tech Stack

| Component | Technology | Port |
| :--- | :--- | :--- |
| **Backend** | Java 21, Spring Boot 3 | `8081` |
| **Portal** | Next.js 14 | `3000` |
| **Studio** | React, Vite | `5173` |
| **Database** | PostgreSQL 16 | `5432` |

## Quick Start

### 1. Prerequisites
- Docker & Docker Compose.
- Java 21 (JDK).
- Node.js 20+ & PNPM (`npm install -g pnpm`).

### 2. Infrastructure
Start the database (PostgreSQL):
```bash
docker-compose -f docker/docker-compose.yml up -d
```

### 3. Backend
Start the compiled backend:
```bash
cd backend
./gradlew.bat bootRun
```
*API Docs: http://localhost:8081/swagger-ui.html (if enabled) or http://localhost:8081/api/health*

### 4. Frontend
Install dependencies and start the monorepo from the root:
```bash
# Install dependencies (from root)
pnpm install

# Start development server
pnpm run dev
```
- **Portal**: http://localhost:3000
- **Studio**: http://localhost:5173
