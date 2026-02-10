# Contributing to NOX Platform

## Folder Structure

- **backend/**: Java/Spring Boot application.
  - `src/main/resources/db/migration`: Flyway SQL scripts.
- **frontend/**: TurboRepo Monorepo.
  - `apps/portal`: Next.js Dashboard.
  - `apps/studio`: Vite Graph Editor.
  - `packages/ui`: Shared React components.
- **docker/**: Infrastructure configurations (Docker Compose).

## Development Workflow

### Branching Policy
We follow a simplified Git flow:
- `main`: Stable production-ready code.
- `feature/*`: New features (e.g., `feature/login-screen`).
- `fix/*`: Bug fixes (e.g., `fix/api-cors`).
- `docs/*`: Documentation updates.

### Commit Guidelines
We enforce **Conventional Commits** using Husky and Commitlint.
Format: `<type>(<scope>): <subject>`

**Types:**
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding missing tests or correcting existing tests
- `chore`: Changes to the build process or auxiliary tools and libraries such as documentation generation

**Example:**
```bash
feat(auth): implement jwt token validation
fix(ui): resolve button alignment issue on mobile
chore(deps): update dependency pnpm to v9
```

### Setup Instructions
1. Install **PNPM**: `npm install -g pnpm`
2. Install dependencies: `pnpm install` (in root directory)
3. Backend requires **Java 21**.
