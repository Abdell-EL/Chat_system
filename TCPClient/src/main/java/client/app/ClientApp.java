package client.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) {
        ChatController controller = new ChatController();

        Scene scene = new Scene(controller.getRoot(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("TCP Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    private static String targetIp;
    private static int targetPort;

    public static void setConnectionTarget(String ip, int port) {
        targetIp = ip;
        targetPort = port;
    }

    public static String getTargetIp() {
        return targetIp;
    }

    public static int getTargetPort() {
        return targetPort;
    }

    public static void main(String[] args) {
        launch(args);
    }
}