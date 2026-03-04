package server.model;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;

    private BufferedReader in;
    private BufferedWriter out;

    private String username; // set after HELLO

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            sendLine("INFO|Connected. Send HELLO|<username> to join.");

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Handle BYE directly
                if (line.equalsIgnoreCase("BYE")) {
                    disconnect();
                    break;
                }

                // Parse protocol TYPE|payload
                String[] parts = line.split("\\|", 2);
                String type = parts[0];
                String payload = (parts.length > 1) ? parts[1] : "";

                switch (type) {
                    case "HELLO" -> handleHello(payload);
                    case "MSG" -> handleMsg(payload);
                    case "CMD" -> handleCmd(payload);
                    default -> sendLine("INFO|Unknown command. Use HELLO|, MSG|, CMD|, BYE");
                }
            }
        } catch (IOException e) {
            // connection dropped
        } finally {
            disconnect();
        }
    }

    private void handleHello(String requestedUsername) {
        if (username != null) {
            sendLine("INFO|Already registered as " + username);
            return;
        }

        String u = requestedUsername.trim();
        if (u.isBlank()) {
            sendLine("INFO|Username cannot be empty.");
            return;
        }

        boolean ok = server.registerUser(u, this);
        if (!ok) {
            sendLine("INFO|Username already taken. Try another.");
            return;
        }

        this.username = u;
        sendLine("WELCOME|" + username);
    }

    private void handleMsg(String text) {
        // Allow read-only clients later; for now server just rejects if not HELLO yet
        if (username == null) {
            sendLine("INFO|You must register first: HELLO|<username>");
            return;
        }

        String t = text.trim();
        if (t.isEmpty()) return;

        // Support "bye/end" typed as chat message (matches requirements)
        if (t.equalsIgnoreCase("bye") || t.equalsIgnoreCase("end")) {
            disconnect();
            return;
        }

        String time = LocalTime.now().format(TIME_FMT);
        server.broadcastChat(username, time, t);
    }

    private void handleCmd(String cmd) {
        if (cmd.equalsIgnoreCase("allUsers")) {
            server.sendUsersListTo(this);
        } else {
            sendLine("INFO|Unknown CMD. Try CMD|allUsers");
        }
    }

    public synchronized void sendLine(String line) {
        try {
            out.write(line);
            out.newLine();
            out.flush();
        } catch (IOException ignored) {
        }
    }

    private void disconnect() {
        // Remove from server map if registered
        if (username != null) server.removeUser(username);

        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }
    public void close() {
        try { socket.close(); } catch (IOException ignored) {}
    }
}