
####################################################
                    TCP Chat System
####################################################
--------------------------------------
Overview
--------------------------------------

This project implements a multi-client TCP chat system using Java sockets and JavaFX.
It allows multiple clients to connect to a server, exchange messages in real time, and see the list of connected users.

The system is organized as a multi-module Maven project with two applications:

TCPServer - the chat server with a graphical interface to monitor activity.

TCPClient - the chat client with a graphical interface for users to send and receive messages.

Communication between server and clients is done through TCP sockets, and the UI is built with JavaFX.

--------------------------------------
Project Structure
--------------------------------------

Chat_system
│
├── TCPServer
│   ├── src/main/java/server/app
│   │       ServerApp.java
│   │       ServerController.java
│   │       TCPServer.java
│   │
│   ├── src/main/java/server/model
│   │       ChatServer.java
│   │       ClientHandler.java
│   │       ServerListener.java
│   │       AppConfig.java
│   │
│   └── src/main/resources
│           config.properties
│           style.css
│
├── TCPClient
│   ├── src/main/java/client/app
│   │       ClientApp.java
│   │       ChatController.java
│   │       TCPClient.java
│   │
│   ├── src/main/java/client/model
│   │       ChatClient.java
│   │       ClientListener.java
│   │       AppConfig.java
│   │
│   └── src/main/resources
│           config.properties
│           style.css
│
└── pom.xml

--------------------------------------
Features
--------------------------------------

Server

. Accepts multiple clients simultaneously

. Each client handled in a separate thread

. Displays:

    . connected users

    . server activity log

. Broadcasts messages to all connected clients

. Supports graceful shutdown

Client

. Connects to the server using TCP

. Allows sending and receiving messages

. Displays chat history

. Shows connection status

. Shows system messages (user join/leave)

Configuration

The server address and port are loaded from config files.

Example:

server.ip=127.0.0.1
server.port=3000

Command-line arguments can override these values.

--------------------------------------
Requirements
--------------------------------------

. Java 21

. Maven (or IntelliJ Maven integration)

. JavaFX

--------------------------------------
How to Run
--------------------------------------

Method 1 — Using IntelliJ (Recommended)

--------------------------------------

1️⃣ Start the Server

1.Open the project in IntelliJ IDEA

2.Open the Maven panel on the right

3.Navigate to:

    TCPServer → Plugins → javafx → javafx:run

4.Double-click javafx:run

5.The TCP Chat Server window will appear

6.Click Start Server

    Default port: 3000

--------------------------------------

2️⃣ Start Clients

In the Maven panel:

    TCPClient → Plugins → javafx → javafx:run

Run it multiple times to simulate multiple users.

Each client can connect and start chatting.

--------------------------------------
Running with Command Line Arguments
--------------------------------------

The system supports overriding the configuration file.

- Server

Example:

    TCPServer 4000

This starts the server on port 4000.

- Client

Example:

    TCPClient 127.0.0.1 4000

This connects the client to:

    IP: 127.0.0.1
    Port: 4000

If arguments are not provided, the values from config.properties are used.

--------------------------------------
How the System Works
--------------------------------------

1.The server starts and listens for incoming TCP connections.

2.When a client connects:

    . A ClientHandler thread is created.

    . The user registers with a username.

3.Messages sent by a client are:

    . received by the server

    . broadcast to all connected clients.

4.The server keeps track of connected users and updates the UI.

--------------------------------------
Main Classes
--------------------------------------

- Server

ChatServer - manages server socket and client connections
ClientHandler - handles communication with a single client
ServerController - JavaFX UI controller
ServerListener - interface for UI updates

- Client

ChatClient - handles TCP communication
ChatController - JavaFX UI controller
ClientListener - interface for UI updates

--------------------------------------
Technologies Used
--------------------------------------

. Java

. JavaFX

. TCP Sockets

. Maven

. Multi-threading
