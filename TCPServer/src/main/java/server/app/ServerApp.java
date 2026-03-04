package server.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {

    @Override
    public void start(Stage stage) {
        ServerController controller = new ServerController();

        Scene scene = new Scene(controller.getRoot(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("TCP Chat Server");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> controller.forceStopOnExit());
    }
    private static int defaultPort = 3000;

    public static void setDefaultPort(int port) {
        defaultPort = port;
    }

    public static int getDefaultPort() {
        return defaultPort;
    }

    public static void main(String[] args) {
        launch(args);
    }
}