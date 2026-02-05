# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Notion Version Control - A Spring Boot backend application implementing version control for Notion, with a robust OAuth2 + JWT authentication infrastructure.

**Technology Stack:**
- Java 25, Spring Boot 4.0.1, Gradle (Kotlin DSL)
- PostgreSQL with pg_cron (custom Docker image, via Docker Compose)
- Spring Security + OAuth2 Client + JWT (JJWT 0.12.6)
- Flyway for migrations, Testcontainers for integration tests

## Build & Run Commands

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run application (Docker Compose auto-starts PostgreSQL with pg_cron)
./gradlew bootRun

# Run single test class
./gradlew test --tests "ai.usnack.notionversioncontrol.SomeTest"

# Run single test method
./gradlew test --tests "ai.usnack.notionversioncontrol.SomeTest.methodName"

# Clean build
./gradlew clean build

# Check for compilation errors without running tests
./gradlew compileJava compileTestJava
```

## Required Environment Variables

Create a `.env` file in the project root:
```
NOTION_CLIENT_ID=your_notion_client_id
NOTION_CLIENT_SECRET=your_notion_client_secret
JWT_SECRET=your_jwt_secret_key
CORS_ALLOWED_ORIGIN=http://localhost:3000
OAUTH2_REDIRECT_URI=http://localhost:3000/auth/callback
OAUTH2_FAILURE_REDIRECT_URI=http://localhost:3000/auth/error
```

## Architecture

### Package Structure

```
src/main/java/ai/usnack/notionversioncontrol/
├── global/                    # Cross-cutting concerns (infrastructure)
│   ├── security/              # Authentication & Authorization (SSOT)
│   │   ├── config/            # SecurityConfig, CORS, Properties
│   │   ├── jwt/               # JwtProvider, JwtAuthenticationFilter
│   │   ├── oauth/             # OAuth2 handlers and user service
│   │   ├── token/             # RefreshTokenService, TokenBlacklistService
│   │   ├── entity/            # AuthAccount, Role, Permission, NotionConnection, RefreshToken, BlacklistedToken
│   │   ├── repository/        # JPA repositories
│   │   ├── controller/        # AuthController (refresh, logout)
│   │   ├── dto/               # TokenResponse
│   │   └── exception/         # Auth error codes & handlers
│   ├── jpa/                   # JPA configuration
│   │   ├── config/            # JpaConfig with auditing
│   │   └── entity/            # BaseEntity (UUID, timestamps, soft delete)
│   └── exception/             # Global exception handling
├── notion/                    # Domain: Notion integration (placeholder)
└── user/                      # Domain: User management (placeholder)
```

### Design Principles

1. **SSOT (Single Source of Truth)**: All auth logic is contained in `global/security/` module only
2. **Stateless Authentication**: JWT-based, no server-side sessions
3. **Spring Security Native**: Leverage built-in filters and handlers
4. **Soft Delete**: All entities use Hibernate `@SoftDelete` annotation; `updated_at` serves as deleted timestamp

### Key Components

| Class | Responsibility |
|-------|----------------|
| `JwtProvider` | JWT creation, parsing, validation (single entry point for all JWT logic) |
| `RefreshTokenService` | Refresh token management in PostgreSQL |
| `TokenBlacklistService` | Token invalidation for logout |
| `JwtAuthenticationFilter` | Validates JWT on each request, sets SecurityContext |
| `OAuth2AuthenticationSuccessHandler` | Issues JWT after successful OAuth login |
| `BaseEntity` | UUID PK, auditing fields (created_at, updated_at, created_by, updated_by), soft delete |

### Entity Relationships

```
AuthAccount (1) ←→ (N) Role ←→ (N) Permission
AuthAccount (1) ←→ (N) NotionConnection (Provider-specific OAuth data)
```

### Token Storage (PostgreSQL)

| Table | Type | Purpose | TTL | Cleanup |
|-------|------|---------|-----|---------|
| `refresh_token` | Regular | Refresh token storage (durability required) | 30 days | pg_cron hourly |
| `blacklisted_token` | UNLOGGED | Token invalidation for logout (3x faster writes, crash loss OK) | 30 min | pg_cron every 5 min |

Expired token cleanup is handled by pg_cron inside PostgreSQL (no Spring `@Scheduled` needed).

## Specification Reference

Detailed authentication/authorization specification is documented in `spec/auth.md` (Korean). Key sections:
- Architecture and design principles (Section 2)
- JWT token structure and validation rules (Section 3)
- Role-Based Access Control with Permissions (Section 4)
- Entity design and database schema (Section 5)
- Security configuration details (Section 6)

## Testing

- Integration tests use Testcontainers with `@ServiceConnection` for automatic PostgreSQL (with pg_cron) setup
- Test configuration in `src/test/resources/application.yaml`
- No manual database setup required for tests

## Current Status

**Implemented:**
- OAuth2 + JWT authentication infrastructure
- Role and Permission entities with RBAC
- Token refresh and logout endpoints
- PostgreSQL-based token management (UNLOGGED blacklist, pg_cron cleanup)
- Flyway migration scripts for token tables
- Testcontainers setup

**Pending:**
- Notion API integration (`notion/` package)
- User management features (`user/` package)
- API documentation (Swagger/OpenAPI)
