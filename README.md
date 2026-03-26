# My Puppy

Pet grooming and bath appointment REST API built with Quarkus and Kotlin.

## Tech Stack
- **Quarkus** — Supersonic Subatomic Java framework
- **Kotlin** — Language
- **Hibernate ORM with Panache** — ORM
- **H2** — Dev/test database
- **PostgreSQL** — Production database

## Prerequisites
- Java 21+

## Running in Dev Mode
```bash
./mvnw quarkus:dev
```

API available at `http://localhost:8080`
Swagger UI at `http://localhost:8080/q/swagger-ui`

## Running Tests
```bash
./mvnw test
```

## Building for Production
```bash
./mvnw package
```
