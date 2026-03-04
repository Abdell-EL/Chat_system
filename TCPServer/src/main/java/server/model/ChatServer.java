package server.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import server.model.AppConfig;

public class ChatServer {

    private final int port;
    private ServerSocket serverSocket;
    private ServerListener listener;
    private volatile boolean running = false;


    // username -> handler
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        running = true;

        serverSocket = new ServerSocket(port);
        log("Server started on port " + port);
        log("Waiting for clients...");

        try {
            while (running) {
                Socket socket = serverSocket.accept(); // blocks until client OR socket closed
                log("New connection from " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(socket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            // If we stopped intentionally, accept() throws "Socket closed" -> ignore
            if (running) {
                throw e;
            }
        } finally {
            // ensure closed
            if (serverSocket != null && !serverSocket.isClosed()) {
                try { serverSocket.close(); } catch (IOException ignored) {}
            }
            running = false;
        }
    }

    public void stopServer() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // this unblocks accept()
            }
        } catch (IOException ignored) {
        }

        log("Server stopped.");
    }

    // Register username only after HELLO|username
    public boolean registerUser(String username, ClientHandler handler) {
        if (username == null || username.isBlank()) return false;

        ClientHandler existing = clients.putIfAbsent(username, handler);

        if (existing == null) {
            log("Welcome " + username);
            broadcastInfo(username + " joined the chat");
            notifyUsersChanged();
            return true;
        }
        return false;
    }

    public void removeUser(String username) {
        if (username == null) return;

        ClientHandler removed = clients.remove(username);
        if (removed != null) {
            broadcastInfo(username + " left the chat");
            log(username + " disconnected");
            notifyUsersChanged();
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
        if (listener != null) {
            listener.onLog("[SERVER] " + msg);
        }
    }


    public static void main(String[] args) {
        int port = AppConfig.port();
        try {
            new ChatServer(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setListener(ServerListener listener) {
        this.listener = listener;
    }
    private void notifyUsersChanged() {
        if (listener == null) return;

        List<String> list = new ArrayList<>(clients.keySet());
        Collections.sort(list);
        listener.onUsersChanged(list);

        listener.onUsersChanged(list);
    }

}