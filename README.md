# Blueprints REST API

A REST API for managing **blueprints**: designs identified by an author and a name, made up
of an ordered sequence of `(x, y)` points. It lets you create blueprints, query them and add
points to them, exposing a clean HTTP contract with real persistence in PostgreSQL.

> Project for Lab #4 of **Software Architecture (ARSW)** — Escuela Colombiana de Ingeniería
> Julio Garavito. Built with Java 21 and Spring Boot 3.3.

---

## What it does

A *blueprint* is a design: it has an **author**, a **name** and an ordered list of **points**
(integer `x`, `y` coordinates) that draw it. The API lets you store, query and modify them
through REST endpoints, always returning a uniform response format and the correct HTTP codes.

Storage is **swappable without touching the business logic**: by default it keeps data in
memory (handy to start with no infrastructure), and by activating a Spring profile it switches
to persisting in a real **PostgreSQL** database.

---

## Stack

| Technology | Purpose |
|------------|---------|
| Java 21 | Language |
| Spring Boot 3.3.9 | Framework (web, validation, data-jpa) |
| PostgreSQL 17 | Database (`postgres` profile) |
| Hibernate / Spring Data JPA | Persistence |
| springdoc-openapi 2.6 | Swagger / OpenAPI documentation |
| Maven | Build |
| Docker Compose | PostgreSQL for development |

---

## Architecture

The code follows a **layered separation**, where each package has a single responsibility and
the domain stays independent of any technology:

```
src/main/java/edu/eci/arsw/blueprints
  ├── model/         # Pure domain: Blueprint, Point (no framework annotations)
  ├── persistence/   # BlueprintPersistence contract + implementations
  │    ├── InMemoryBlueprintPersistence     (default profile)
  │    ├── PostgresBlueprintPersistence      ("postgres" profile)
  │    └── entity/   # JPA entities (BlueprintEntity, PointEmbeddable)
  ├── services/      # Business logic (BlueprintsServices)
  ├── filters/       # Optional point processing (Identity/Redundancy/Undersampling)
  ├── controllers/   # REST controller + global exception handling
  ├── dto/           # ApiResponse<T> (uniform wrapper)
  └── config/        # OpenAPI/Swagger configuration
```

**Key idea:** the domain classes `Blueprint` and `Point` know nothing about the database or
HTTP. PostgreSQL persistence is achieved with **separate JPA entities** that translate
entity ↔ domain, so switching storage (memory ↔ PostgreSQL) only takes changing the Spring
profile, without modifying services or controllers. Every implementation satisfies the same
`BlueprintPersistence` interface.

---

## Endpoints

Base URL: `http://localhost:8080/api/v1/blueprints`

| Method | Path | Description | Success HTTP |
|--------|------|-------------|--------------|
| `GET` | `/` | List all blueprints | `200` |
| `GET` | `/{author}` | Blueprints by author | `200` |
| `GET` | `/{author}/{name}` | A specific blueprint | `200` |
| `POST` | `/` | Create a new blueprint | `201` |
| `PUT` | `/{author}/{name}/points` | Add a point to a blueprint | `202` |

### Uniform response

Every response (success or error) shares the same contract through `ApiResponse<T>`:

```json
{
  "code": 200,
  "message": "execute ok",
  "data": { "author": "john", "name": "kitchen", "points": [ { "x": 1, "y": 1 } ] }
}
```

### HTTP codes and error handling

A global handler (`GlobalExceptionHandler`) translates exceptions into clear responses:

| Code | When |
|------|------|
| `200 OK` | Successful query |
| `201 Created` | Blueprint created |
| `202 Accepted` | Point added |
| `400 Bad Request` | Invalid data (validation) |
| `404 Not Found` | Author or blueprint not found |
| `409 Conflict` | Attempt to create a duplicate blueprint |

---

## How to run it

### Requirements
- **JDK 21**
- Maven 3.9+ (or use the bundled `./mvnw` wrapper)
- Docker (only if you want to use the PostgreSQL profile)

### Option A — In memory (no database)

```bash
mvn spring-boot:run
```

The app starts at `http://localhost:8080` keeping data in memory.

### Option B — With PostgreSQL (`postgres` profile)

1. Start the database (exposes PostgreSQL on host port **5434**):

   ```bash
   docker compose up -d
   ```

2. Run the app with the `postgres` profile:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```

   > **On PowerShell**, wrap the argument in quotes so it doesn't get split:
   > ```powershell
   > mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
   > ```

Hibernate creates the schema automatically (`ddl-auto=update`). The default connection
(`jdbc:postgresql://localhost:5434/blueprintsdb`, user/password `blueprints`) matches
`docker-compose.yml`; it can be overridden with the `DB_URL`, `DB_USER`, `DB_PASSWORD`
environment variables.

---

## Spring profiles

| Profile | Effect |
|---------|--------|
| *(default)* | In-memory persistence |
| `postgres` | PostgreSQL persistence via JPA |
| `redundancy` | Enables `RedundancyFilter` when querying a blueprint |
| `undersampling` | Enables `UndersamplingFilter` when querying a blueprint |

The **filters** transform points when querying a single blueprint without altering what is
stored: `RedundancyFilter` removes consecutive duplicate points and `UndersamplingFilter`
keeps one out of every two. With no filter profile active, `IdentityFilter` is used (it
changes nothing).

---

## Interactive documentation (Swagger)

With the app running:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

---

## Quick examples (curl)

```bash
# List all
curl -s http://localhost:8080/api/v1/blueprints

# Create a blueprint
curl -i -X POST http://localhost:8080/api/v1/blueprints \
  -H 'Content-Type: application/json' \
  -d '{"author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}]}'

# Query one
curl -s http://localhost:8080/api/v1/blueprints/john/kitchen

# Add a point
curl -i -X PUT http://localhost:8080/api/v1/blueprints/john/kitchen/points \
  -H 'Content-Type: application/json' \
  -d '{"x":9,"y":9}'
```

---

## Tests

```bash
mvn test
```

Includes a smoke test (`BlueprintsSmokeTest`) that validates the application context starts.

---

## Evidence

[`docs/evidencias-postgres.md`](docs/evidencias-postgres.md) documents, with screenshots, the
full behavior on PostgreSQL: startup with the profile, a walkthrough of the endpoints (happy
path and error cases with their HTTP codes) and verification that the data is actually
persisted in the database.
