package server.app;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import server.model.ChatServer;
import server.model.ServerListener;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import server.model.AppConfig;
import server.app.ServerApp;

public class ServerController implements ServerListener {

    @Override
    public void onLog(String line) {
        appendLog(line);
    }

    @Override
    public void onUsersChanged(List<String> usernames) {
        setUsers(usernames);
        setUsers(usernames);
    }
    // Root
    private final BorderPane root = new BorderPane();

    // UI controls
    private TextField portField;
    private Button startBtn;
    private Button stopBtn;

    private Label statusLabel;
    private Circle statusDot;

    private ListView<String> usersList;
    private TextArea logArea;

    // Server model
    private ChatServer server;
    private Thread serverThread;

    // For "random background colors per user"
    private final Map<String, Color> userColors = new ConcurrentHashMap<>();

    public ServerController() {
        buildUI();
        setStatus(false);
        stopBtn.setDisable(true);
    }

    public Parent getRoot() {
        return root;
    }

    // ---------------- UI BUILDING ----------------

    private void buildUI() {
        root.setPadding(new Insets(12));
        root.getStyleClass().add("chat-grid");

        // Top bar (port + start/stop + status)
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(8));

        Label portLabel = new Label("Port:");
        portLabel.getStyleClass().add("online-label");

        portField = new TextField(String.valueOf(ServerApp.getDefaultPort()));
        portField.setPrefWidth(100);

        startBtn = new Button("Start Server");
        stopBtn = new Button("Stop");

        statusDot = new Circle(6);
        statusDot.getStyleClass().add("dot-offline");

        statusLabel = new Label("Offline");
        statusLabel.getStyleClass().add("online-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        top.getChildren().addAll(portLabel, portField, startBtn, stopBtn, spacer, statusDot, statusLabel);

        // Center split: users list (left) + logs (right)
        usersList = new ListView<>();
        usersList.setPrefWidth(250);
        usersList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                if (empty || username == null) {
                    setText(null);
                    setBackground(null);
                    return;
                }
                setText(username);

                // Assign a stable random color per user
                Color c = userColors.computeIfAbsent(username, u -> randomSoftColor());

                // Use that color as background
                String css = String.format(
                        "-fx-background-color: rgba(%d,%d,%d,0.45); -fx-text-fill: white; -fx-font-weight: bold;",
                        (int) (c.getRed() * 255),
                        (int) (c.getGreen() * 255),
                        (int) (c.getBlue() * 255)
                );
                setStyle(css);
            }
        });

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("chat-area");

        VBox left = new VBox(8, new Label("Connected Users"), usersList);
        left.setPadding(new Insets(8));
        left.getStyleClass().add("label");

        VBox right = new VBox(8, new Label("Server Log"), logArea);
        right.setPadding(new Insets(8));
        right.getStyleClass().add("label");

        SplitPane splitPane = new SplitPane(left, right);
        splitPane.setDividerPositions(0.28);

        root.setTop(top);
        root.setCenter(splitPane);

        // Actions
        startBtn.setOnAction(e -> startServer());
        stopBtn.setOnAction(e -> stopServer());
    }

    // ---------------- SERVER CONTROL ----------------

    private void startServer() {
        int port;

        if (serverThread != null && serverThread.isAlive()) {
            appendLog("[ERROR] Server thread already running.");
            return;
        }
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            appendLog("[ERROR] Invalid port.");
            return;
        }

        startBtn.setDisable(true);
        portField.setDisable(true);
        stopBtn.setDisable(false);

        appendLog("[SERVER] Starting server on port " + port + "...");

        serverThread = new Thread(() -> {
            try {
                server = new ChatServer(port);
                server.setListener(this);
                server.start();
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    appendLog("[ERROR] " + ex.getMessage());
                    setStatus(false);
                    startBtn.setDisable(false);
                    portField.setDisable(false);
                    stopBtn.setDisable(true);
                });
            }
        }, "server-main");

        setStatus(true);
        serverThread.start();
    }

    private void stopServer() {
        appendLog("[SERVER] Stop requested.");

        if (server != null) {
            server.stopServer();
        }

        if (serverThread != null) {
            try {
                serverThread.join(500);
            } catch (InterruptedException ignored) {}
        }

        setStatus(false);
        startBtn.setDisable(false);
        portField.setDisable(false);
        stopBtn.setDisable(true);

        setUsers(Collections.emptyList());
    }

    // ---------------- UI UPDATE HELPERS ----------------

    private void setStatus(boolean online) {
        if (online) {
            statusLabel.setText("Online");
            statusDot.getStyleClass().removeAll("dot-offline");
            if (!statusDot.getStyleClass().contains("dot-online")) statusDot.getStyleClass().add("dot-online");
        } else {
            statusLabel.setText("Offline");
            statusDot.getStyleClass().removeAll("dot-online");
            if (!statusDot.getStyleClass().contains("dot-offline")) statusDot.getStyleClass().add("dot-offline");
        }
    }

    /** Call this from the server side later when users change */
    public void setUsers(List<String> usernames) {
        Platform.runLater(() -> {
            usersList.getItems().setAll(usernames);
        });
    }

    /** Call this from the server side later to log events */
    public void appendLog(String msg) {
        Platform.runLater(() -> {
            logArea.appendText(msg + "\n");
            logArea.positionCaret(logArea.getText().length());
        });
    }

    private Color randomSoftColor() {
        // Soft random colors (avoid too bright / too dark)
        double r = 0.25 + Math.random() * 0.5;
        double g = 0.25 + Math.random() * 0.5;
        double b = 0.25 + Math.random() * 0.5;
        return new Color(r, g, b, 1.0);
    }

    public void forceStopOnExit() {
        if (server != null) {
            server.stopServer();
        }
    }
}