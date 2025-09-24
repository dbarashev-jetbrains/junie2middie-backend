# junie2middie-backend

## Prerequisites
- Java 17+ (JDK)
- Bash/PowerShell and cURL (for quick testing)

## HTTP Protocol Specification

### Create a contact

`POST /contact`

Creates a new contact using the provided payload in JSON format:
```
    {
      "id": -1, // if id is < 0 we will create a new contact, otherwise we will update the existing, if any 
      "name": "Bob Johnson",
      "status": "Away",
      "avatarUrl": "https://example.com/avatar2.jpg"
    } 
```


### Get a list of contacts

`GET /contacts` 

Returns a list of contacts in JSON format. 
```
{
  "contacts":[
    {
      "id": 1,
      "name": "Alice Smith",
      "status": "Online",
      "avatarUrl": "https://example.com/avatar1.jpg"
    },
    {
      "id": 2,
      "name": "Bob Johnson",
      "status": "Away",
      "avatarUrl": "https://example.com/avatar2.jpg"
    } 
}
```

### Send Message

`POST /message`

Creates a new message using the provided payload in JSON format:

```
{
  "id": -1, // id does not matter when creating a message 
  "content": "Hello, this is a test message",
  "senderId": 0, 
  "receiverId": 1,  
  "timestamp": "2023-06-15T14:30:00", 
}
```

### Get Chat Messages

`GET /contact/{contact1}/messages/{contact2}`

Returns a conversation between the given contact1 and contact2 in JSON format:

```
{
  "messages": [
    {
      "id": 1,
      "content": "Hello, how are you?",
      "senderId": 1,
      "receiverId": 0,
      "timestamp": "2023-06-15T10:30:00"
    },
    {
      "id": 2,
      "content": "I'm good, thanks for asking!",
      "senderId": 0,
      "receiverId": 1,
      "timestamp": "2023-06-15T11:30:00"
    }
 ]
}
```

## Run the server (development)
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

## Build a Docker image with Jib
Jib is configured in build.gradle.kts to build a minimal Docker image without a Dockerfile.

- Build to the local Docker daemon (image name: j2m-backend:0.0.1):
```
./gradlew jibDockerBuild
```

- Build and push to a registry (example):
```
./gradlew jib -Djib.to.image=YOUR_REGISTRY/j2m-backend:0.0.1
```

## Curl examples (copy/paste ready)
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
