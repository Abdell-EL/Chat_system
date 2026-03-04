package client.model;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ChatClient {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Thread readerThread;

    private volatile boolean running = false;
    private ClientListener listener;

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        running = true;

        readerThread = new Thread(this::readLoop, "client-reader");
        readerThread.start();
    }

    public void sendHello(String username) throws IOException {
        sendLine("HELLO|" + username);
    }

    public void sendMessage(String text) throws IOException {
        sendLine("MSG|" + text);
    }

    public void requestAllUsers() throws IOException {
        sendLine("CMD|allUsers");
    }

    public void disconnect() {
        try { sendLine("BYE"); } catch (Exception ignored) {}
        close();
    }

    private void sendLine(String line) throws IOException {
        if (out == null) throw new IOException("Not connected");
        out.write(line);
        out.newLine();
        out.flush();
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                handleServerLine(line);
            }
        } catch (IOException ignored) {
        } finally {
            close();
            if (listener != null) listener.onDisconnected();
        }
    }

    private void handleServerLine(String line) {
        // Examples:
        // INFO|text
        // WELCOME|username
        // CHAT|user|time|text
        // USERS|u1,u2,u3

        String[] parts = line.split("\\|", 2);
        String type = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";

        if (listener == null) return;

        switch (type) {
            case "INFO" -> listener.onInfo(payload);
            case "WELCOME" -> listener.onInfo("Welcome " + payload);
            case "CHAT" -> {
                String[] p = payload.split("\\|", 3);
                if (p.length == 3) listener.onChat(p[0], p[1], p[2]);
            }
            case "USERS" -> {
                List<String> users = payload.isBlank()
                        ? List.of()
                        : Arrays.asList(payload.split(","));
                listener.onUsers(users);
            }
            default -> listener.onInfo("Unknown: " + line);
        }
    }

    private void close() {
        running = false;
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }
}