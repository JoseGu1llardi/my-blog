# Blog REST API

A content-management REST API built with Spring Boot 3.2 and Java 17. It handles the full lifecycle of blog posts — creation, publishing, search, and soft deletion — with JWT authentication, IP-based rate limiting, and Flyway-managed schema migrations. Built as a portfolio project to demonstrate production-oriented backend practices in a junior engineer context.

**Live API:** `https://api.guillard.dev`

---

## Technical Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 3.2.0 | Application framework |
| Spring Security | (Boot-managed) | Authentication, authorization, filter chain |
| JJWT | 0.12.3 | JWT generation and validation |
| Spring Data JPA / Hibernate | (Boot-managed) | ORM, `@SQLRestriction`, `@Modifying` queries |
| PostgreSQL | 16 | Production and development database |
| H2 | (Boot-managed) | In-memory database — test profile only |
| Flyway | (Boot-managed) | Schema versioning and migrations |
| Caffeine | (Boot-managed) | In-process cache for rate limiters |
| Docker / Docker Compose | — | Container orchestration (dev and prod) |
| Nginx | 1.24 | Reverse proxy, SSL termination |
| GitHub Actions | — | CI/CD pipeline — build and deploy on push |
| AWS EC2 | t3.small | Production application server |
| AWS RDS | db.t3.micro | Managed PostgreSQL database |
| JaCoCo | 0.8.10 | Test coverage reporting |
| RestAssured | (Boot-managed) | Integration test HTTP client |
| Springdoc OpenAPI | 2.3.0 | Swagger UI (dev profile only) |

---

## Architecture Decisions

### JWT Authentication with Token Versioning
Every author entity stores a `token_version` integer (added via `V2` Flyway migration, indexed). On login, the current version is embedded as a custom claim in the JWT. On each authenticated request, `JwtAuthenticationFilter` extracts and compares the token's version against the database value. When `POST /auth/logout` is called, `incrementTokenVersion` invalidates all previously issued tokens without a token store or blocklist. Revocation is O(1) — one indexed column read per request.

### RBAC with `@PreAuthorize`
`Author` implements `UserDetails` and carries a `UserRole` enum (`ADMIN`, `AUTHOR`, `EDITOR`). Spring's `@EnableMethodSecurity` enables method-level guards. `POST /api/v1/categories` is restricted to `ROLE_ADMIN` via `@PreAuthorize("hasRole('ADMIN')")`. Write operations on posts additionally enforce ownership at the service layer (`validateOwnership`), so role is a necessary but not sufficient condition for mutation.

### Soft Delete with `@SQLRestriction`
`Post` carries a `deleted` boolean and `deletedAt` timestamp. Hibernate's `@SQLRestriction("deleted = false")` appends this predicate to every generated query automatically — no explicit filter required at the repository or service layer. The deleted record is preserved for audit purposes; the application never sees it.

### IP-Based Rate Limiting with Caffeine
Two independent limiters, both backed by Caffeine's TTL cache with a max size of 100,000 entries:

- **`LoginRateLimiter`**: Counts only *failed* login attempts per IP. A successful login resets the counter. Blocks after 5 failures within a 1-minute window. This avoids penalizing a user who finally provides the correct password.
- **`ViewRateLimiter`**: Tracks the `ip:slug` pair. A view is counted once per IP per post per 30-minute window — deduplication without a database write on every request.

Both classes expose package-private constructors accepting a `Duration`, allowing unit tests to use a short TTL without needing Spring or time manipulation.

### Slug as Value Object
`Slug` is an immutable value object with self-validating construction. `Slug.of()` normalizes input (NFD decomposition, accent removal, lowercase, whitespace-to-dash, character stripping) and throws `IllegalArgumentException` on empty results. It has proper `equals`/`hashCode` based on its normalized value, so it can be used directly as a JPA query parameter. A `SlugAttributeConverter` transparently maps it to `VARCHAR` in the database.

### Atomic View Count Increment
View counts are updated with a `@Modifying` JPQL query:
```sql
UPDATE Post p SET p.viewsCount = p.viewsCount + 1 WHERE p.id = :id
```
This is a single atomic `UPDATE` at the database level, avoiding the read-modify-write race condition that would occur with `post.incrementViewCount()` followed by a `save()`.

### Admin Seed via Flyway
The initial admin user is created by `V3__seed_admin.sql` — a Flyway migration that runs once before the application starts. The BCrypt hash is pre-computed and stored directly in the SQL file. This eliminates runtime dependency on environment variables for critical data and makes the seed idempotent (`ON CONFLICT DO NOTHING`).

### Least-Privilege Database User
The application connects as `blog_user`, a PostgreSQL role with `SELECT`, `INSERT`, `UPDATE`, and `DELETE` on application tables only. The `postgres` superuser is not used by the application. This limits blast radius if the application layer is compromised.

### Nginx as Reverse Proxy
Nginx sits in front of the Spring Boot container, handling SSL termination and routing. The Spring Boot container is not exposed publicly — only Nginx is. This means port 8080 is closed to the internet, all traffic enters via ports 80 (redirected to HTTPS) and 443, and the `ForwardedHeaderFilter` bean reads `X-Forwarded-For` and `X-Forwarded-Proto` headers set by Nginx so IP-based rate limiting sees real client IPs.

### Managed Database (AWS RDS)
PostgreSQL runs on a dedicated AWS RDS `db.t3.micro` instance, separate from the EC2 application server. Database data persists independently of the EC2 instance lifecycle — terminating and replacing the EC2 instance does not affect the data. RDS provides automated daily backups with 7-day retention and automatic minor version upgrades.

### Dev/Prod Parity
- **`dev` profile**: PostgreSQL via Docker Compose (`docker-compose.dev.yml`), `ddl-auto: update`, Flyway disabled, seed data via `DataInitializer`.
- **`prod` profile**: AWS RDS PostgreSQL, `ddl-auto: validate`, Flyway enabled (`V1` → `V2` → `V3`), Swagger disabled.
- **`test` profile**: H2 in-memory only. H2 is scoped to `test` in `pom.xml` — it cannot be loaded in any other profile.

The base `application.yml` sets `ddl-auto: validate` as the default, so forgetting to set a profile fails loudly on startup rather than silently recreating the schema.

### `ForwardedHeaderFilter` for Real IP Extraction
Registered as a Spring `@Bean` in `WebConfig`. Without it, `request.getRemoteAddr()` returns the Nginx proxy IP, making the IP-based rate limiters ineffective. The filter rewrites `X-Forwarded-For` and `X-Forwarded-Proto` headers so all downstream components see real client values.

### Profile-Conditional Security Rules
`SecurityConfig` reads the active `Environment` profiles at bean construction time. H2 console access and Swagger UI routes are added to the permit list only when the `dev` profile is active. In production, these paths hit the catch-all `anyRequest().authenticated()` rule.

---

## Security Highlights

| Measure | Threat Mitigated |
|---|---|
| JWT secret length validation at startup (`< 32 bytes` → `IllegalArgumentException`) | Weak secrets reaching production silently |
| Token versioning via `tokenVersion` claim + DB comparison on each request | Stateless logout and immediate credential revocation without a blocklist |
| `LoginRateLimiter`: failure-only counting, resets on success | Brute-force credential stuffing without penalizing legitimate recovery |
| LIKE wildcard escaping in `searchPublishedPosts` (`%`, `_`, `\` escaped with `ESCAPE '\'`) | LIKE injection — wildcard amplification causing full-table scans |
| `@Size(min=1, max=200)` on search query parameter | Oversized query strings causing excessive memory allocation |
| `accessDeniedHandler` returning structured JSON 403 | Leaking Spring's default error page format to callers |
| `authenticationEntryPoint` returning structured JSON 401 | Same — consistent error contract for API consumers |
| CORS configured with exact-match `allowedOrigins` from environment variable | Reflected CORS attacks from untrusted origins |
| `ddl-auto: validate` as base default | Accidental schema mutation in production on misconfigured startup |
| `iss` and `aud` claims required on JWT parse (`requireIssuer` / `requireAudience`) | Token confusion attacks — accepting tokens issued for a different service |
| BCrypt password encoding | Offline dictionary attacks on a compromised credential store |
| Swagger UI and H2 console gated behind `dev` profile | Accidental exposure of admin tooling in non-development environments |
| `SessionCreationPolicy.STATELESS` | Session fixation and CSRF via session cookies |
| Least-privilege DB user (`blog_user`) | Superuser access from a compromised application layer |
| Environment secrets stored in `/etc/blog-api/env` with `chmod 640` | Credential exposure via filesystem |
| Port 8080 closed externally — only Nginx has access | Direct access to Spring Boot bypassing Nginx security layer |
| HTTPS enforced via Let's Encrypt certificate (auto-renewed every 90 days) | Credential and token interception in transit |
| RDS not publicly accessible — VPC-internal only | Direct database access from the internet |
| RDS Security Group allows inbound only from EC2 Security Group | Database access from unauthorized compute resources |

---

## Test Coverage

**41 tests** across four layers.

### Unit Tests
Pure Mockito, no Spring context loaded.

- **`PostServiceTest`** (12 tests): Covers the full service contract — paginated list, slug lookup with view increment, ownership enforcement, slug conflicts on create, publish/unpublish state machine, and soft delete. Uses `InOrder` verification to assert orchestration sequence on `createPost`.
- **`JwtServiceTest`** (5 tests): Validates token generation, round-trip extraction, validity check, username mismatch rejection, and expiry. Uses `ReflectionTestUtils` to set `expiration = -1000L` for instant expiry without sleeping.
- **`AuthServiceTest`** (2 tests): Verifies the login flow delegates to `AuthenticationManager` before calling `JwtService`, and that a failed authentication records a rate-limit entry and short-circuits before querying the database.
- **`ViewRateLimiterTest`** (3 tests): Tests first-view allowance, deduplication within the window, and re-admission after TTL expiry. Uses the package-private `Duration` constructor with a 100ms window.

### Slice Tests (`@WebMvcTest`)
Spring MVC layer loaded, service layer mocked. Security auto-configuration excluded to test HTTP contract in isolation.

- **`PostControllerTest`** (5 tests): List, slug lookup, 404 response shape, post creation with `SecurityMockMvcRequestPostProcessors`, and Bean Validation rejection with field-level error detail.
- **`AuthControllerTest`** (3 tests): Login success (200 + token), bad credentials (401 + structured error), rate limit exceeded (429 + `RATE_LIMIT_EXCEEDED` error type).

### Repository Tests (`@DataJpaTest`)
H2 in-memory, real Hibernate, real SQL — no mocks.

- **`PostRepositoryTest`** (7 tests): Save with auto-slug generation, `findBySlug`, published-only queries ordered by date, year-based filtering, category join query, and slug normalization from title.

### Integration Tests (`@SpringBootTest` + RestAssured)
Full application context on a random port, H2 in-memory, real HTTP.

- **`PostApiIntegrationTest`** (3 tests): The primary test covers the complete happy path — authenticate, create draft, publish, fetch (view = 0), fetch again (view = 1), update, delete, verify 404. Two additional tests cover 404 response shape and Bean Validation rejection at the HTTP boundary.

---

## API Endpoints

**Base URL:** `https://api.guillard.dev`

### Posts

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/api/v1/posts` | No | Paginated list of published posts |
| `GET` | `/api/v1/posts/{slug}` | No | Fetch post by slug; increments view count (rate-limited per IP) |
| `GET` | `/api/v1/posts/year/{year}` | No | Posts published in a given year (`@Min(2026)`, `@Max(2100)`) |
| `GET` | `/api/v1/posts/category/{slug}` | No | Paginated posts for a category |
| `GET` | `/api/v1/posts/author/{slug}` | No | Paginated posts by an author |
| `GET` | `/api/v1/posts/search?query=` | No | Full-text search (LIKE, LIKE-injection escaped, `@Size(max=200)`) |
| `GET` | `/api/v1/posts/years` | No | Distinct years that have published posts |
| `POST` | `/api/v1/posts` | Yes (AUTHOR) | Create a draft post |
| `PUT` | `/api/v1/posts/{id}` | Yes (owner) | Update post content and categories |
| `PATCH` | `/api/v1/posts/{id}/publish` | Yes (owner) | Transition post to PUBLISHED |
| `PATCH` | `/api/v1/posts/{id}/unpublish` | Yes (owner) | Revert post to DRAFT |
| `DELETE` | `/api/v1/posts/{id}` | Yes (owner) | Soft-delete post |

### Categories

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/api/v1/categories` | No | All categories |
| `GET` | `/api/v1/categories/with-posts` | No | Categories that have at least one post |
| `GET` | `/api/v1/categories/{slug}` | No | Category by slug |
| `POST` | `/api/v1/categories` | Yes (ADMIN) | Create category |

### Authors

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `GET` | `/api/v1/authors` | No | Active authors |
| `GET` | `/api/v1/authors/with-posts` | No | Authors with published posts |
| `GET` | `/api/v1/authors/{slug}` | No | Author profile by slug |

### Auth

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | No | Authenticate; returns JWT (rate-limited per IP) |
| `POST` | `/api/v1/auth/logout` | Yes | Increment `tokenVersion`; invalidates all issued tokens |

---

## Running Locally

### Prerequisites
- Java 17
- Docker and Docker Compose
- Maven 3.8+

### 1. Start the full local environment

```bash
docker compose -f docker-compose.dev.yml up
```

Starts `postgres:16-alpine` on port 5432 and the Spring Boot application on port 8080, both in containers. The `dev` profile seeds an initial author and sample posts via `DataInitializer`. Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

### 2. Run Tests

```bash
mvn test
```

Tests run against H2 in-memory using the `test` profile. No database or environment variables required.

---

## Deployment

### Infrastructure

```
Internet
    ↓
https://api.guillard.dev (port 443)
    ↓
Nginx — SSL termination, reverse proxy
EC2: t3.small, Ubuntu 24.04, Elastic IP 100.31.169.60
    ↓
Spring Boot container (localhost:8080 — not publicly accessible)
    ↓
AWS RDS PostgreSQL 16 (VPC-internal — not publicly accessible)
blog-api-db.ci3kkxkpswbz.us-east-1.rds.amazonaws.com
```

### Auto-start on Reboot

A systemd service (`blog-api-docker.service`) manages Docker Compose. On EC2 reboot, systemd automatically runs `docker compose up -d`. The RDS instance is unaffected by EC2 reboots — data persists independently.

### CI/CD Pipeline

Every push to `main` triggers a GitHub Actions workflow defined in `.github/workflows/deploy.yml`:

```
git push origin main
    ↓
GitHub Actions (ubuntu-latest)
    ↓ copies source files to EC2 via SCP
EC2
    ↓ docker compose -f docker-compose.prod.yml up -d --build
Application running with new code
```

No manual SCP, no manual SSH, no manual restart. End-to-end deploy time is approximately 2–3 minutes.

### Environment Variables (Production)

Stored in `/etc/blog-api/env` on the EC2 instance with `chmod 640, chown root:ubuntu`. Never committed to the repository.

| Variable | Description |
|---|---|
| `DB_HOST` | RDS endpoint hostname |
| `DB_PORT` | PostgreSQL port (`5432`) |
| `DB_NAME` | Database name (`blogdb`) |
| `DB_USER` | Application database user |
| `DB_PASSWORD` | RDS master password |
| `APP_JWT_SECRET` | Base64-encoded HMAC-SHA512 signing key (min 32 bytes), generated with `openssl rand -base64 32` |
| `APP_JWT_EXPIRATION` | Token expiration in milliseconds (`3600000` = 1h) |
| `AUTHOR_PASSWORD` | Admin author password (used by Flyway V3 seed) |
| `AUTHOR_EMAIL` | Admin author email |
| `CORS_ALLOWED_ORIGINS` | Exact-match allowed origin |

### GitHub Actions Secrets

| Secret | Description |
|---|---|
| `EC2_SSH_KEY` | Contents of `blog-api-key.pem` |
| `EC2_HOST` | `100.31.169.60` |
| `EC2_USER` | `ubuntu` |

### Flyway Migrations

| Version | Description |
|---|---|
| V1 | Initial schema — authors, posts, categories, post_categories |
| V2 | Add `token_version` column to authors |
| V3 | Seed admin user with pre-computed BCrypt hash |

---

## Environment Variables Reference (Local Dev)

| Variable | Required | Description | Example |
|---|---|---|---|
| `APP_JWT_SECRET` | Required | Base64-encoded signing key (min 32 bytes) | `rl1fKrM7L9+BMyejalNy...` |
| `APP_JWT_EXPIRATION` | Optional (default: `3600000`) | Token expiration in milliseconds | `86400000` |
| `app.init.password` | Required (dev) | Seed author password | `changeme-local-only-123!` |
| `app.init.email` | Required (dev) | Seed author email | `dev@example.com` |