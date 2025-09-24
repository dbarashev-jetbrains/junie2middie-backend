

# j2m-backend

A simple Ktor-based backend for a messenger app. It exposes endpoints to manage contacts and messages and stores data in PostgreSQL.

Endpoints:
- GET /contacts — list contacts
- GET /contact/{id}/messages — list messages in a conversation with a contact
- POST /contact — create a contact
- POST /message — create a message

## Prerequisites
- Java 17+ (JDK)
- PostgreSQL 12+ (local or remote)
- Bash/PowerShell and cURL (for quick testing)

## 1) Database setup
You can point the app to any reachable PostgreSQL instance. The app will create the required tables automatically on startup. A matching schema is also provided at schema.sql (optional to run manually).

Example: create a local database and user (adjust credentials to your needs):

PostgreSQL shell (psql):
```
CREATE DATABASE j2m;
CREATE USER j2m_user WITH PASSWORD 'j2m_pass';
GRANT ALL PRIVILEGES ON DATABASE j2m TO j2m_user;
```

Note: If you prefer, you can inspect or run the provided DDL in schema.sql manually.

## 2) Configure database connection
Default configuration is in src/main/resources/application.yaml:
```
ktor:
  application:
    modules:
      - org.jetbrains.edu.junie2middie.ApplicationKt.module
  deployment:
    port: 8080
  database:
    jdbcUrl: "jdbc:postgresql://localhost:5432/j2m"
    user: "postgres"
    password: "postgres"
    maxPoolSize: 5
```

Options to configure:
- Edit application.yaml directly, or
- Override via JVM system properties when running:
  -Dktor.database.jdbcUrl=jdbc:postgresql://HOST:PORT/DB
  -Dktor.database.user=USER
  -Dktor.database.password=PASS
  -Dktor.deployment.port=8080

Examples:
```
./gradlew run -Dktor.database.jdbcUrl=jdbc:postgresql://localhost:5432/j2m \
              -Dktor.database.user=j2m_user \
              -Dktor.database.password=j2m_pass
```

## 3) Run the server (development)
Use the Gradle wrapper; no local Gradle install needed.

- Build (optional):
```
./gradlew build
```

- Run:
```
./gradlew run
```

If the server starts successfully, logs will include something similar to:
```
INFO  Application - Application started ...
INFO  Application - Responding at http://0.0.0.0:8080
```

## 4) Build a fat JAR and run it
Create a self-contained JAR:
```
./gradlew buildFatJar
```
The JAR is typically at build/libs/j2m-backend-all.jar. Run it with optional overrides:
```
java -Dktor.database.jdbcUrl=jdbc:postgresql://localhost:5432/j2m \
     -Dktor.database.user=j2m_user \
     -Dktor.database.password=j2m_pass \
     -Dktor.deployment.port=8080 \
     -jar build/libs/j2m-backend-all.jar
```

## 5) Build a Docker image with Jib
Jib is configured in build.gradle.kts to build a minimal Docker image without a Dockerfile.

- Build to the local Docker daemon (image name: j2m-backend:0.0.1):
```
./gradlew jibDockerBuild
```

- Build and push to a registry (example):
```
./gradlew jib -Djib.to.image=YOUR_REGISTRY/j2m-backend:0.0.1
```

Expose port 8080. Configure DB via environment or JVM props, e.g. when running with Docker:
```
docker run --rm -p 8080:8080 \
  -e KTOR_DATABASE_JDBCURL=jdbc:postgresql://host.docker.internal:5432/j2m \
  -e KTOR_DATABASE_USER=j2m_user \
  -e KTOR_DATABASE_PASSWORD=j2m_pass \
  j2m-backend:0.0.1
```

Note: Environment variables map to ktor.* config keys by replacing dots with underscores and uppercasing, or use -D system properties via JAVA_TOOL_OPTIONS.

## 5) Quick API test
With the server running on localhost:8080:

- Health check
```
curl http://localhost:8080/
# -> OK
```

- List contacts
```
curl http://localhost:8080/contacts
```

- Create a contact
```
curl -X POST http://localhost:8080/contact \
  -H 'Content-Type: application/json' \
  -d '{
        "id": 1,
        "name": "Alice Smith",
        "status": "Online",
        "avatarUrl": "https://example.com/avatar1.jpg"
      }'
```

- List messages with contact 1
```
curl http://localhost:8080/contact/1/messages
```

- Create a message
```
curl -X POST http://localhost:8080/message \
  -H 'Content-Type: application/json' \
  -d '{
        "id": 0,
        "content": "Hello, this is a test message",
        "senderId": 0,
        "receiverId": 1,
        "timestamp": "2023-06-15T14:30:00"
      }'
```

## 6) Curl examples (copy/paste ready)
These examples assume the server runs locally on port 8080.

Unix/macOS Bash:
```
BASE_URL=http://localhost:8080
```
PowerShell (Windows):
```
$env:BASE_URL = "http://localhost:8080"
```

- Health (show status and headers)
```
curl -i "$BASE_URL/"
```

- Get all contacts (pretty-print with jq if available)
```
curl -sS "$BASE_URL/contacts" | jq .
```

- Create a contact (id is optional; if omitted the server will assign one)
```
curl -sS -X POST "$BASE_URL/contact" \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Bob Johnson",
        "status": "Away",
        "avatarUrl": "https://example.com/avatar2.jpg"
      }'
```

- Fetch messages with contact id=1
```
curl -sS "$BASE_URL/contact/1/messages" | jq .
```

- Send a message (from me=0 to contact=1)
```
curl -sS -X POST "$BASE_URL/message" \
  -H "Content-Type: application/json" \
  -d '{
        "content": "Ping from curl",
        "senderId": 0,
        "receiverId": 1,
        "timestamp": "2023-06-15T14:30:00"
      }'
```

- Example sequence
```
# 1) Create Alice
curl -sS -X POST "$BASE_URL/contact" -H "Content-Type: application/json" -d '{
  "id": -1,
  "name": "Alice Smith",
  "status": "Online",
  "avatarUrl": "https://example.com/avatar1.jpg"
}' | jq .

# 2) Create Bob
curl -sS -X POST "$BASE_URL/contact" -H "Content-Type: application/json" -d '{
  "id": -1,
  "name": "Bob Baker",
  "status": "Online",
  "avatarUrl": "https://example.com/avatar2.jpg"
}' | jq .

# 2) Send a message to Bob
curl -sS -X POST "$BASE_URL/message" -H "Content-Type: application/json" -d '{
  "id": -1,
  "content": "Hello, Alice!",
  "senderId": 1,
  "receiverId": 2,
  "timestamp": "2023-06-15T10:30:00"
}' | jq .

# 3) Read conversation with Bob
curl -sS "$BASE_URL/contact/1/messages/2" | jq .
```

Notes for Windows cmd.exe (not PowerShell):
- Replace single quotes with double quotes and escape inner quotes accordingly.
- Join lines into a single line or use ^ for line continuation.

## Notes
- On first run, the server will ensure the required tables exist (contacts, messages).
- See schema.sql for the DDL if you want to inspect or run it yourself.
- Logs are configured with Logback (src/main/resources/logback.xml).
