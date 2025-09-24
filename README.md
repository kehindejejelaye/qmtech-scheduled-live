# Scheduled Live Polling Service

This project provides a **real-time polling service** built with **Spring Boot**, **RabbitMQ**, and **PostgreSQL**.  
It allows creating polls, casting votes, and broadcasting live tallies over WebSocket.

---

## 📋 Prerequisites

Before you start, ensure you have the following installed:

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Docker** and **Docker Compose**
- (Optional) A REST client such as **Postman** or **curl**

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/kehindejejelaye/qmtech-scheduled-live.git
cd scheduled-live
```

### 2️⃣ Start Infrastructure with Docker
RabbitMQ and PostgreSQL are already defined in the `docker-compose.yml` file.

```bash
docker compose up -d
```

This command will:
- Spin up **PostgreSQL** on port **5432**
- Spin up **RabbitMQ** on ports **5672** (AMQP) and **15672** (management UI)

You can access RabbitMQ management UI at:  
👉 [http://localhost:15672](http://localhost:15672)  
(Default credentials are usually `guest` / `guest` unless overridden in `docker-compose.yml`.)

### 3️⃣ Configure the Application
Check `src/main/resources/application.yml` or `application.properties` and verify:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/scheduledlive
    username: scheduledlive
    password: scheduledlive
  rabbitmq:
    host: localhost
    port: 5672
```
Adjust values if your container or local settings differ.

### 4️⃣ Build and Run the Spring Boot App
The API will start on **http://localhost:8080**.

---

## 🖥️ Interacting with the UI

A simple HTML/JS client is included to test WebSocket and REST functionality.

1. Open the UI file (usually `src/main/resources/static/index.html`) in a browser:
   ```
   http://localhost:8080/index.html
   ```
2. **Connect to WebSocket**
    - Enter the WebSocket URL (e.g. `ws://localhost:8080/ws`) and click **Connect**.
3. **Create a Poll**
    - Enter a question, comma-separated options, and a start time.
    - Click **Create Poll** to save it in the database.
4. **Subscribe to Poll Updates**
    - Paste the Poll ID and click **Subscribe** to receive live tallies.
5. **Cast Votes**
    - Enter the Poll ID, an option, and a username, then click **Vote**.
6. Watch votes update in real time in the **Subscribed Poll Details** section.

---

## 🔑 Key Endpoints
| Method | Endpoint                     | Description                  |
|-------|--------------------------------|------------------------------|
| `POST` | `/api/v1/polls`               | Create a new poll            |
| `GET`  | `/api/v1/polls`               | List all polls               |
| `GET`  | `/api/v1/polls/{pollId}`      | Get a poll with tallies      |
| `POST` | `/api/v1/polls/{pollId}/vote` | Cast a vote for an option    |

### WebSocket
Live updates are broadcast over a WebSocket endpoint:
```
ws://localhost:8080/ws/polls/{pollId}
```

---

## 🧩 Project Structure
```
src/main/java/com/qmtech/scheduledlive
├─ config/          # RabbitMQ & WebSocket configuration
├─ controller/      # REST endpoints
├─ dto/             # Data transfer objects
├─ entity/          # JPA entities (Poll, Vote)
├─ repo/            # Spring Data repositories
├─ service/         # Business logic
└─ websocket/       # WebSocket handlers
```

---

## 🐳 Managing Containers
- **Stop services**:
  ```bash
  docker compose down
  ```
- **View logs**:
  ```bash
  docker compose logs -f
  ```
- **Rebuild if configs change**:
  ```bash
  docker compose up -d --build
  ```

---

## ✅ Tips
- Make sure RabbitMQ is fully started before launching the Spring Boot application.
- You can verify PostgreSQL is healthy by connecting with a client:

---

## 📜 License
MIT (or your chosen license)


----


### Assumptions

The following assumptions were made while implementing the solution:

***

### 🏗️ Architectural & Infrastructure

* **RabbitMQ & PostgreSQL via Docker** – RabbitMQ and PostgreSQL are expected to run via the provided `docker-compose.yml` file for local development.
* **Single-node environment** – The setup assumes a single-node/local environment without clustering or scaling considerations.
* **Default Ports & Credentials** – RabbitMQ and PostgreSQL use default ports and credentials defined in `docker-compose.yml`; no external secrets manager is used.

***

### ⚡ Application Logic

* **Poll Start Time** – Votes are only valid after the `scheduledStartTime` of a poll. The system clock is assumed to be reliable and synchronized.
* **Vote Persistence** – Votes are processed asynchronously through RabbitMQ, assuming stable message delivery with no dead-letter queue configured.
* **Single Vote Counting** – Duplicate votes by the same user are not restricted.
* **Poll Options** – Poll options are stored as a JSON array in the database and assumed to remain in this format.

***

### 💻 Frontend/UI

* **Static UI Served from Backend** – The HTML/JS frontend is served directly from the Spring Boot application and connects to REST and WebSocket endpoints on the same origin.
* **Browser Support** – Requires a modern browser with WebSocket and ES6+ JavaScript support.

***

### 🔒 Security

* **Open Access** – No authentication or authorization is enforced. Any user can create polls and vote.