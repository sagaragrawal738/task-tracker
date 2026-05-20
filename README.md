# Task Tracker API

A RESTful Spring Boot API for lightweight project and task management. Users can be assigned tasks within projects, and the service exposes full CRUD operations with filtering, pagination, and validation.

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA / Hibernate
- H2 in-memory database
- Bean Validation (Jakarta)
- JUnit 5 + Mockito
- springdoc-openapi (Swagger UI)
- Maven

## Prerequisites

- JDK 17+ installed
- Maven 3.8+ (or use the IDE's built-in Maven)
- (Optional) Docker, if you want to run via container

## How to Run

### Using Maven

```bash
# Clone the repo
git clone https://github.com/sagaragrawal738/task-tracker.git
cd task-tracker-api

# Build and run
mvn clean install
mvn spring-boot:run
```

The app starts at **http://localhost:8080**.

### Using Docker

```bash
docker build -t task-tracker-api .
docker run -p 8080:8080 task-tracker-api
```

Or with docker-compose:

```bash
docker-compose up --build
```

## Running Tests

```bash
mvn test
```

## Accessing the API

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/api-docs | OpenAPI JSON spec |
| http://localhost:8080/h2-console | H2 database console (JDBC URL: `jdbc:h2:mem:taskdb`) |

## Configuration

Key properties are externalised in `src/main/resources/application.properties`:

| Property | Description | Default |
|----------|-------------|---------|
| `app.name` | Application name (exposed via `/api/info`) | Task Tracker API |
| `app.version` | Application version | 1.0.0 |
| `task.default-priority` | Default priority when not specified | MEDIUM |
| `pagination.default-page-size` | Default page size for list endpoints | 20 |

## Sample curl Requests

### Create a project

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name": "Backend API", "description": "Core backend service"}'
```

### Create a user

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Johnson", "email": "alice@example.com", "role": "DEVELOPER"}'
```

### Create a task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Implement login", "status": "TODO", "priority": "HIGH", "projectId": 1, "assigneeId": 1, "dueDate": "2026-06-15"}'
```

### List all projects

```bash
curl http://localhost:8080/api/projects
```

### Get a specific task

```bash
curl http://localhost:8080/api/tasks/1
```

### Update a task status

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Implement login", "status": "IN_PROGRESS", "priority": "HIGH", "projectId": 1, "assigneeId": 1}'
```

### Filter tasks by status

```bash
curl "http://localhost:8080/api/tasks?status=TODO"
```

### Filter tasks by project

```bash
curl "http://localhost:8080/api/tasks?projectId=1"
```

### Get tasks under a project

```bash
curl http://localhost:8080/api/projects/1/tasks
```

### Delete a task

```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

### Application info

```bash
curl http://localhost:8080/api/info
```

## Project Structure

```
src/main/java/com/tasktracker/
├── config/          # AppConfig (externalised properties)
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs with validation
├── entity/          # JPA entities and enums
├── exception/       # Custom exceptions + global handler
├── repository/      # Spring Data JPA repositories
└── service/         # Business logic layer

src/test/java/com/tasktracker/
└── service/         # Unit tests for service layer
```
