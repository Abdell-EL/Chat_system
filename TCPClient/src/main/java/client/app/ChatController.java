package client.app;

import client.model.ChatClient;
import client.model.ClientListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import client.app.ClientApp;

import client.model.AppConfig;

public class ChatController implements ClientListener {

    // --- UI root ---
    private final StackPane root = new StackPane();

    // --- Screens ---
    private Parent joinScreen;
    private Parent chatScreen;

    // --- Join screen controls ---
    private TextField usernameField;
    private Button joinBtn;
    private Label joinStatus;

    // --- Chat screen controls ---
    private TextArea chatArea;
    private TextField messageField;
    private Button sendBtn;
    private Button allUsersBtn;

    private Label onlineLabel;
    private Circle onlineDot;

    // --- Model ---
    private final ChatClient client = new ChatClient();

    // State
    private boolean readOnly = false;

    private final String host =
            (ClientApp.getTargetIp() != null && !ClientApp.getTargetIp().isBlank())
                    ? ClientApp.getTargetIp()
                    : AppConfig.serverIp();

    private final int port =
            (ClientApp.getTargetPort() > 0)
                    ? ClientApp.getTargetPort()
                    : AppConfig.serverPort();

    public ChatController() {
        client.setListener(this);
        buildJoinScreen();
        buildChatScreen();

        root.getChildren().add(joinScreen);
    }

    public Parent getRoot() {
        return root;
    }

    // -------------------- UI BUILDING --------------------

    private void buildJoinScreen() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("join-grid");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(24));

        Label title = new Label("Join Chat");
        title.getStyleClass().add("title");

        Label userLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Leave empty for read-only mode");

        joinBtn = new Button("Join");
        joinStatus = new Label("");
        joinStatus.getStyleClass().add("status");

        grid.add(title, 0, 0, 2, 1);
        grid.add(userLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(joinBtn, 1, 2);
        grid.add(joinStatus, 0, 3, 2, 1);

        joinBtn.setOnAction(e -> attemptJoin());
        usernameField.setOnAction(e -> attemptJoin()); // Enter triggers join

        joinScreen = grid;
    }

    private void buildChatScreen() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("chat-grid");
        grid.setPadding(new Insets(16));
        grid.setHgap(12);
        grid.setVgap(12);

        // Columns/rows
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1);

        RowConstraints rTop = new RowConstraints();
        rTop.setVgrow(Priority.NEVER);
        RowConstraints rMid = new RowConstraints();
        rMid.setVgrow(Priority.ALWAYS);
        RowConstraints rBot = new RowConstraints();
        rBot.setVgrow(Priority.NEVER);
        grid.getRowConstraints().addAll(rTop, rMid, rBot);

        // Top bar
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);

        onlineDot = new Circle(6);
        onlineDot.getStyleClass().add("dot-offline");

        onlineLabel = new Label("Offline");
        onlineLabel.getStyleClass().add("online-label");

        allUsersBtn = new Button("All Users");
        allUsersBtn.setDisable(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(onlineDot, onlineLabel, spacer, allUsersBtn);

        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.getStyleClass().add("chat-area");
        chatArea.setWrapText(true);

        // Bottom input
        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_LEFT);

        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        HBox.setHgrow(messageField, Priority.ALWAYS);

        sendBtn = new Button("Send");

        bottom.getChildren().addAll(messageField, sendBtn);

        grid.add(topBar, 0, 0);
        grid.add(chatArea, 0, 1);
        grid.add(bottom, 0, 2);

        // Actions
        sendBtn.setOnAction(e -> sendCurrentMessage());
        messageField.setOnAction(e -> sendCurrentMessage()); // Enter sends
        allUsersBtn.setOnAction(e -> requestAllUsers());

        chatScreen = grid;
    }

    // -------------------- JOIN / ACTIONS --------------------

    private void attemptJoin() {
        joinBtn.setDisable(true);
        joinStatus.setText("Connecting...");

        String username = usernameField.getText().trim();
        readOnly = username.isBlank();

        // To allow read-only users to still RECEIVE broadcasts, we connect with a generated guest name
        // but disable sending controls in UI.
        if (readOnly) {
            username = "guest-" + UUID.randomUUID().toString().substring(0, 6);
        }

        final String finalUsername = username;

        new Thread(() -> {
            try {
                appendChat("DEBUG", "--", "Connecting to " + host + ":" + port);
                client.connect(host, port);
                client.sendHello(finalUsername);

                Platform.runLater(() -> {
                    showChatScreen();
                    setOnline(true);
                    appendSystem("Connected as " + finalUsername + (readOnly ? " (read-only)" : ""));
                    applyReadOnlyMode(readOnly);
                    allUsersBtn.setDisable(false);
                });

            } catch (IOException ex) {
                Platform.runLater(() -> {
                    joinStatus.setText("Connection failed: " + ex.getMessage());
                    joinBtn.setDisable(false);
                });
            }
        }, "ui-connect").start();
    }

    private void showChatScreen() {
        root.getChildren().clear();
        root.getChildren().add(chatScreen);
    }

    private void applyReadOnlyMode(boolean ro) {
        messageField.setDisable(ro);
        sendBtn.setDisable(ro);
    }

    private void sendCurrentMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        // support typing "allUsers" too
        if (text.equalsIgnoreCase("allUsers")) {
            requestAllUsers();
            messageField.clear();
            return;
        }

        // support typing "bye"/"end"
        if (text.equalsIgnoreCase("bye") || text.equalsIgnoreCase("end")) {
            client.disconnect();
            return;
        }

        try {
            client.sendMessage(text);
            messageField.clear();
        } catch (IOException e) {
            appendSystem("Send failed: " + e.getMessage());
        }
    }

    private void requestAllUsers() {
        try {
            client.requestAllUsers();
        } catch (IOException e) {
            appendSystem("Request failed: " + e.getMessage());
        }
    }

    private void setOnline(boolean online) {
        if (online) {
            onlineLabel.setText("Online");
            onlineDot.getStyleClass().removeAll("dot-offline");
            if (!onlineDot.getStyleClass().contains("dot-online")) onlineDot.getStyleClass().add("dot-online");
        } else {
            onlineLabel.setText("Offline");
            onlineDot.getStyleClass().removeAll("dot-online");
            if (!onlineDot.getStyleClass().contains("dot-offline")) onlineDot.getStyleClass().add("dot-offline");
        }
    }

    private void appendSystem(String text) {
        chatArea.appendText("[INFO] " + text + "\n");
        chatArea.positionCaret(chatArea.getText().length());
    }

    private void appendChat(String from, String time, String text) {
        chatArea.appendText("[" + time + "] " + from + ": " + text + "\n");
        chatArea.positionCaret(chatArea.getText().length());
    }

    // -------------------- ClientListener (events from model) --------------------

    @Override
    public void onInfo(String text) {
        Platform.runLater(() -> appendSystem(text));
    }

    @Override
    public void onChat(String from, String time, String text) {
        Platform.runLater(() -> appendChat(from, time, text));
    }

    @Override
    public void onUsers(List<String> users) {
        Platform.runLater(() -> appendSystem("Online users: " + String.join(", ", users)));
    }

    @Override
    public void onDisconnected() {
        Platform.runLater(() -> {
            setOnline(false);
            appendSystem("Disconnected.");
            applyReadOnlyMode(true);
            allUsersBtn.setDisable(true);
        });
    }
}