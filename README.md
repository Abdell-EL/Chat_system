# 💬 TCP Chat System

A real-time multi-client chat application built with Java TCP Sockets and JavaFX.

---

## 🌐 Overview

**TCP Chat System** is a multi-client chat application built on Java sockets and JavaFX. It allows multiple users to connect to a central server, exchange messages in real time, and view the list of currently connected users.

The project is organized as a **multi-module Maven project** with two separate applications:

| Module | Description |
|--------|-------------|
| `TCPServer` | Chat server with a graphical interface for monitoring activity |
| `TCPClient` | Chat client with a graphical interface for sending and receiving messages |

---

## ✨ Features

### 🖥️ Server
- Accepts multiple client connections simultaneously
- Each client is handled in a dedicated thread
- Displays connected users and a live server activity log
- Broadcasts messages to all connected clients
- Supports graceful shutdown

### 💻 Client
- Connects to the server via TCP
- Send and receive messages in real time
- Persistent chat history display
- Live connection status indicator
- System notifications for user join/leave events

---

## 📁 Project Structure

```
Chat_system/
│
├── TCPServer/
│   ├── src/main/java/server/app/
│   │   ├── ServerApp.java
│   │   ├── ServerController.java
│   │   └── TCPServer.java
│   │
│   ├── src/main/java/server/model/
│   │   ├── ChatServer.java
│   │   ├── ClientHandler.java
│   │   ├── ServerListener.java
│   │   └── AppConfig.java
│   │
│   └── src/main/resources/
│       ├── config.properties
│       └── style.css
│
├── TCPClient/
│   ├── src/main/java/client/app/
│   │   ├── ClientApp.java
│   │   ├── ChatController.java
│   │   └── TCPClient.java
│   │
│   ├── src/main/java/client/model/
│   │   ├── ChatClient.java
│   │   ├── ClientListener.java
│   │   └── AppConfig.java
│   │
│   └── src/main/resources/
│       ├── config.properties
│       └── style.css
├──README.md
└── pom.xml
```

---

## ⚙️ How It Works

```
  Client A ──┐
             │    TCP Sockets
  Client B ──┼──────────────────► Server ──► Broadcast to all clients
             │
  Client C ──┘
```

1. The **server** starts and listens for incoming TCP connections.
2. When a **client connects**, a `ClientHandler` thread is created and the user registers with a username.
3. Messages sent by a client are received by the server and **broadcast to all connected clients**.
4. The server tracks connected users and updates the UI in real time.

---

## 📦 Requirements

- **Java** 21+
- **Maven** (or IntelliJ IDEA with Maven integration)
- **JavaFX** (bundled via Maven dependencies)

---

## 🚀 Getting Started

### Method 1 — IntelliJ IDEA *(Recommended)*

#### 1️⃣ Start the Server

1. Open the project in **IntelliJ IDEA**
2. Open the **Maven** panel (right sidebar)
3. Navigate to: `TCPServer → Plugins → javafx → javafx:run`
4. Double-click **`javafx:run`**
5. The *TCP Chat Server* window will appear — click **Start Server**

> Default port: **3000**

#### 2️⃣ Start Clients

In the Maven panel, navigate to:

```
TCPClient → Plugins → javafx → javafx:run
```

Run it **multiple times** to simulate multiple users. Each client can connect and start chatting independently.

---

### Method 2 — Command Line

#### Start the Server

```bash
# Default configuration (from config.properties)
mvn -pl TCPServer javafx:run

# Custom port
mvn -pl TCPServer javafx:run -Djavafx.args="4000"
```

#### Start a Client

```bash
# Default configuration (from config.properties)
mvn -pl TCPClient javafx:run

# Custom server address and port
mvn -pl TCPClient javafx:run -Djavafx.args="127.0.0.1 4000"
```

---

## 🔧 Configuration

Connection settings are loaded from `config.properties` in each module's resources folder. Command-line arguments override these values at runtime.

**`TCPServer/src/main/resources/config.properties`**
```properties
server.port=3000
```

**`TCPClient/src/main/resources/config.properties`**
```properties
server.ip=127.0.0.1
server.port=3000
```

### Argument Override Reference

| Application | Arguments | Example |
|-------------|-----------|---------|
| `TCPServer` | `<port>` | `4000` |
| `TCPClient` | `<ip> <port>` | `127.0.0.1 4000` |

---

## 🧩 Main Classes

### Server

| Class | Responsibility |
|-------|---------------|
| `ChatServer` | Manages the server socket and accepts client connections |
| `ClientHandler` | Handles communication with a single connected client (runs in its own thread) |
| `ServerController` | JavaFX UI controller for the server window |
| `ServerListener` | Interface for pushing updates from the model to the UI |
| `AppConfig` | Loads and provides server configuration |

### Client

| Class | Responsibility |
|-------|---------------|
| `ChatClient` | Manages TCP communication with the server |
| `ChatController` | JavaFX UI controller for the chat window |
| `ClientListener` | Interface for pushing updates from the model to the UI |
| `AppConfig` | Loads and provides client configuration |

---

## 🛠️ Technologies

| Technology | Usage |
|------------|-------|
| **Java 21** | Core language |
| **JavaFX** | GUI framework for both server and client |
| **TCP Sockets** | Network communication layer |
| **Maven** | Multi-module build and dependency management |
| **Multi-threading** | Concurrent client handling on the server |
