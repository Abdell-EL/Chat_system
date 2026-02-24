package server.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private final int port;
    private ServerSocket serverSocket;

    // username -> handler
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        log("Server started on port " + port);
        log("Waiting for clients...");

        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            log("New connection from " + socket.getRemoteSocketAddress());

            ClientHandler handler = new ClientHandler(socket, this);
            new Thread(handler).start();
        }
    }

    // Register username only after HELLO|username
    public boolean registerUser(String username, ClientHandler handler) {
        if (username == null || username.isBlank()) return false;
        // putIfAbsent ensures uniqueness
        ClientHandler existing = clients.putIfAbsent(username, handler);
        if (existing == null) {
            log("Welcome " + username);
            broadcastInfo(username + " joined the chat");
            return true;
        }
        return false;
    }

    public void removeUser(String username) {
        if (username == null) return;
        ClientHandler removed = clients.remove(username);
        if (removed != null) {
            log(username + " disconnected");
            broadcastInfo(username + " left the chat");
        }
    }

    public void broadcastChat(String fromUser, String time, String text) {
        String line = "CHAT|" + fromUser + "|" + time + "|" + text;
        for (ClientHandler h : clients.values()) {
            h.sendLine(line);
        }
        log("Broadcast from " + fromUser + ": " + text);
    }

    public void broadcastInfo(String infoText) {
        String line = "INFO|" + infoText;
        for (ClientHandler h : clients.values()) {
            h.sendLine(line);
        }
        log("INFO: " + infoText);
    }

    public void sendUsersListTo(ClientHandler requester) {
        Set<String> usernames = clients.keySet();
        String joined = String.join(",", usernames);
        requester.sendLine("USERS|" + joined);
    }

    private void log(String msg) {
        System.out.println("[SERVER] " + msg);
    }

    // Quick run (console)
    public static void main(String[] args) {
        int port = 3000; // later you’ll load from config.properties
        try {
            new ChatServer(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}