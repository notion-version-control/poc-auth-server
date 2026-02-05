# CLAUDE.ko.md

이 문서는 `CLAUDE.md`의 한국어 버전입니다. Claude Code는 `CLAUDE.md`만 참조하며, 이 파일은 개발자 참고용입니다.

## 프로젝트 개요

Notion Version Control - Notion 버전 관리를 위한 Spring Boot 백엔드 애플리케이션. OAuth2 + JWT 기반 인증 인프라 구현.

**기술 스택:**
- Java 25, Spring Boot 4.0.1, Gradle (Kotlin DSL)
- PostgreSQL + pg_cron (커스텀 Docker 이미지, Docker Compose)
- Spring Security + OAuth2 Client + JWT (JJWT 0.12.6)
- Flyway (마이그레이션), Testcontainers (통합 테스트)

## 빌드 및 실행 명령어

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 애플리케이션 실행 (Docker Compose로 PostgreSQL(pg_cron 포함) 자동 시작)
./gradlew bootRun

# 단일 테스트 클래스 실행
./gradlew test --tests "ai.usnack.notionversioncontrol.SomeTest"

# 단일 테스트 메서드 실행
./gradlew test --tests "ai.usnack.notionversioncontrol.SomeTest.methodName"

# 클린 빌드
./gradlew clean build

# 컴파일 에러만 확인 (테스트 제외)
./gradlew compileJava compileTestJava
```

## 필수 환경 변수

프로젝트 루트에 `.env` 파일 생성:
```
NOTION_CLIENT_ID=your_notion_client_id
NOTION_CLIENT_SECRET=your_notion_client_secret
JWT_SECRET=your_jwt_secret_key
CORS_ALLOWED_ORIGIN=http://localhost:3000
OAUTH2_REDIRECT_URI=http://localhost:3000/auth/callback
OAUTH2_FAILURE_REDIRECT_URI=http://localhost:3000/auth/error
```

## 아키텍처

### 패키지 구조

```
src/main/java/ai/usnack/notionversioncontrol/
├── global/                    # 횡단 관심사 (인프라)
│   ├── security/              # 인증 & 인가 (SSOT)
│   │   ├── config/            # SecurityConfig, CORS, Properties
│   │   ├── jwt/               # JwtProvider, JwtAuthenticationFilter
│   │   ├── oauth/             # OAuth2 핸들러, 사용자 서비스
│   │   ├── token/             # RefreshTokenService, TokenBlacklistService
│   │   ├── entity/            # AuthAccount, Role, Permission, NotionConnection, RefreshToken, BlacklistedToken
│   │   ├── repository/        # JPA 리포지토리
│   │   ├── controller/        # AuthController (갱신, 로그아웃)
│   │   ├── dto/               # TokenResponse
│   │   └── exception/         # 인증 에러 코드 & 핸들러
│   ├── jpa/                   # JPA 설정
│   │   ├── config/            # JpaConfig (Auditing 포함)
│   │   └── entity/            # BaseEntity (UUID, 타임스탬프, 소프트 삭제)
│   └── exception/             # 전역 예외 처리
├── notion/                    # 도메인: Notion 연동 (미구현)
└── user/                      # 도메인: 사용자 관리 (미구현)
```

### 설계 원칙

1. **SSOT (Single Source of Truth)**: 모든 인증 로직은 `global/security/` 모듈에만 존재
2. **Stateless 인증**: JWT 기반, 서버 세션 없음
3. **Spring Security Native**: 내장 필터와 핸들러 최대 활용
4. **Soft Delete**: 모든 엔티티에 Hibernate `@SoftDelete` 적용; `updated_at`이 삭제 시간 역할

### 핵심 컴포넌트

| 클래스 | 역할 |
|--------|------|
| `JwtProvider` | JWT 생성, 파싱, 검증 (모든 JWT 로직의 단일 진입점) |
| `RefreshTokenService` | PostgreSQL 기반 Refresh Token 관리 |
| `TokenBlacklistService` | 로그아웃용 토큰 무효화 |
| `JwtAuthenticationFilter` | 매 요청마다 JWT 검증, SecurityContext 설정 |
| `OAuth2AuthenticationSuccessHandler` | OAuth 로그인 성공 후 JWT 발급 |
| `BaseEntity` | UUID PK, 감사 필드 (created_at, updated_at, created_by, updated_by), 소프트 삭제 |

### 엔티티 관계

```
AuthAccount (1) ←→ (N) Role ←→ (N) Permission
AuthAccount (1) ←→ (N) NotionConnection (Provider별 OAuth 데이터)
```

### 토큰 저장소 (PostgreSQL)

| 테이블 | 타입 | 용도 | TTL | 정리 |
|--------|------|------|-----|------|
| `refresh_token` | Regular | Refresh Token 저장 (내구성 필요) | 30일 | pg_cron 매시간 |
| `blacklisted_token` | UNLOGGED | 로그아웃용 토큰 무효화 (3배 빠른 쓰기, 크래시 시 손실 허용) | 30분 | pg_cron 5분마다 |

만료된 토큰 정리는 PostgreSQL 내부 pg_cron이 처리 (Spring `@Scheduled` 불필요).

## 스펙 문서 참조

상세 인증/인가 스펙은 `spec/auth.md`에 문서화되어 있습니다:
- 아키텍처 및 설계 원칙 (섹션 2)
- JWT 토큰 구조 및 검증 규칙 (섹션 3)
- Role 기반 접근 제어와 Permission (섹션 4)
- 엔티티 설계 및 데이터베이스 스키마 (섹션 5)
- 보안 설정 상세 (섹션 6)

## 테스트

- 통합 테스트는 `@ServiceConnection`으로 Testcontainers 사용 (PostgreSQL(pg_cron 포함) 자동 설정)
- 테스트 설정: `src/test/resources/application.yaml`
- 테스트용 수동 DB 설정 불필요

## 현재 상태

**구현 완료:**
- OAuth2 + JWT 인증 인프라
- Role, Permission 엔티티 및 RBAC
- 토큰 갱신, 로그아웃 엔드포인트
- PostgreSQL 기반 토큰 관리 (UNLOGGED 블랙리스트, pg_cron 정리)
- Flyway 마이그레이션 스크립트 (토큰 테이블)
- Testcontainers 설정

**구현 예정:**
- Notion API 연동 (`notion/` 패키지)
- 사용자 관리 기능 (`user/` 패키지)
- API 문서화 (Swagger/OpenAPI)
