# Chat Lab

A simple distributed chat application built as independent microservices with Spring Boot. External clients talk REST to BFF, the<br>
services talk to each other over REST and gRPC, and new messages are published as events to RabbitMQ. Authentication uses JWT.


## Architecture

```
                  ┌───────────────────────────┐
  Browser ──────► │   BFF :8080 (REST + JWT)  │
  client/         └──┬──────────┬──────────┬──┘
                     │ login    │ register │ messages
                     ▼          │          ▼
              ┌──────────────┐  │  ┌─────────────────┐         ┌──────────┐
              │ Auth  :8081  │  │  │ Message  :8083  │ ──────► │ RabbitMQ │
              └──────┬───────┘  │  └────────┬────────┘         └──────────┘
                     │ REST     ▼           │ gRPC
                     │  ┌──────────────┐    │
                     └─►│ User  :8082  │◄───┘
                        │ gRPC  :9090  │
                        └──────────────┘
```

The browser only ever talks to the BFF.

## Services

| Service | Port | Responsibility | Data |
|---|---|---|---|
| BFF | 8080 | REST API for the client, validates the JWT, forwards calls to the other services | – |
| Auth Service | 8081 | Login: fetches the user from User Service, checks the BCrypt hash, issues a JWT | – |
| User Service | 8082 (REST), 9090 (gRPC) | Creates and looks up users, serves `GetUser` over gRPC | MySQL `user_db` |
| Message Service | 8083 | Stores messages, publishes `message.published` events, looks up sender names over gRPC | MySQL `message_db` |
| MySQL | 3306 | One server hosting both databases | – |
| RabbitMQ | 5672 (AMQP), 15672 (web UI) | Event bus | – |

## REST API (through the BFF)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/bff/register` | No | Create a user. Body: `{ displayName, email, password }` |
| POST | `/bff/login` | No | Log in. Body: `{ email, password }`. Returns `{ token }`, or 401 for wrong credentials |
| POST | `/bff/messages` | Bearer token | Send a message. Body: `{ channelId, body }` |
| GET | `/bff/messages?channelId=<channel>&since=<timestamp>` | Bearer token | List messages in a channel. `since` is optional. Each message has `messageId`, `channelId`, `senderId`, `senderName`, `body` and `createdAt` |

Channels are just a `channelId` string on each message, so there is no endpoint for creating them.

### Authentication

- Auth Service issues a JWT whose subject is the user ID. It is valid for 24 hours and signed with the secret in `JWT_SECRET`.
- The BFF validates the token on every request except `/bff/register` and `/bff/login`, and answers 401 if it is missing, invalid or expired.
- The BFF takes the user ID from the token and passes it to Message Service in an `X-User-Id` header, so the sender of a message is never taken from the client.
- Auth Service and the BFF must use the same `JWT_SECRET`. Both fall back to the same development default when it is not set. Step 2 under *Running locally* shows how to set your own.

## What happens when a message is sent

1. The client sends `POST /bff/messages` with its JWT.
2. The BFF validates the token and forwards the message to Message Service together with the user ID.
3. Message Service saves the message and an outbox row in the same database transaction.
4. A relay job runs every 5 seconds and publishes pending outbox rows to RabbitMQ. When the broker confirms, the row is marked as processed.
5. When messages are fetched, Message Service asks User Service over gRPC for each sender's display name.
6. The client polls `GET /bff/messages` every 2 seconds and shows the new message.

### Events

Events use the transactional outbox pattern, so a message is never saved without its event, and an event is never lost if RabbitMQ is down.

| Item | Value |
|---|---|
| Exchange | `chat.exchange` (topic) |
| Routing key | `message.published` |
| Queue | `message.published.queue` (durable) |
| Payload | JSON with `eventId`, `messageId`, `channelId`, `senderId` and `body` |

No service consumes the queue yet. Published events stay in the queue, where you can inspect them in the RabbitMQ web UI.

## Client

The client is a single file, `client/index.html`, using Vue 3 from a CDN. There is no build step and no npm.

- Login and registration, then a chat view with channels.
- The channel list is stored in the browser (`localStorage`), as is the JWT.
- The BFF address is set at the top of the script: `const BFF = 'http://localhost:8080'`. The BFF allows requests from any origin, so you can open the file straight from disk.

## Running locally

### Prerequisites

- Java 25
- Docker (for MySQL and RabbitMQ)

### 1. Start MySQL and RabbitMQ

```bash
docker compose up -d mysql rabbitmq
```

On first start MySQL creates `user_db` and `message_db` from the scripts in `init-db/`.

### 2. Set a JWT secret (optional)

Copy the example file and replace the value with your own secret:

```bash
cp .env.example .env
```

The secret signs and verifies the JWTs, so the BFF and Auth Service must use the same one, and it should be at least 32 characters. One way to generate it is `openssl rand -base64 48`. `.env` is listed in `.gitignore`, so it is never committed.

Docker Compose reads `.env` automatically, but Spring Boot does not. When you start the BFF and Auth Service with Maven, set the same value as an environment variable in those two terminals:

```bash
export JWT_SECRET=<value from .env>
```

In PowerShell:

```powershell
$env:JWT_SECRET = "<value from .env>"
```

You can also set it in the IntelliJ run configuration of both services. If you skip this step, both services fall back to the same built-in development secret, which is fine on your own machine but must not be used anywhere else.

### 3. Start the services

Run each service in its own terminal, from the repository root:

```bash
cd user-service && ./mvnw spring-boot:run
cd auth-service && ./mvnw spring-boot:run
cd message-service && ./mvnw spring-boot:run
cd bff && ./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run` instead. Hibernate creates the tables automatically on first start.

### 4. Open the client

Open `client/index.html` in a browser, register a user, log in and start chatting. To follow the events, open the RabbitMQ web UI at http://localhost:15672 (user `guest`, password `guest`).

### Configuration

Every setting has a default that points at localhost, and can be overridden with environment variables:

| Variable | Used by | Default |
|---|---|---|
| `JWT_SECRET` | BFF, Auth Service | Development-only secret |
| `USER_SERVICE_URL` | BFF, Auth Service | `http://localhost:8082` |
| `AUTH_SERVICE_URL` | BFF | `http://localhost:8081` |
| `MESSAGE_SERVICE_URL` | BFF | `http://localhost:8083` |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | User Service, Message Service | Local MySQL, user `root` |
| `SPRING_RABBITMQ_HOST` | Message Service | `localhost` |
| `USER_SERVICE_HOST`, `USER_SERVICE_GRPC_PORT` | Message Service | `localhost`, `9090` |