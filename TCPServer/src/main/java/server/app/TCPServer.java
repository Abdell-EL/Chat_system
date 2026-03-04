package server.app;

import server.model.AppConfig;
import javafx.application.Application;

public final class TCPServer {

    public static void main(String[] args) {
        int port = AppConfig.port();

        // optional arg override
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException ignored) {}
        }

        // pass to JavaFX app before launch
        ServerApp.setDefaultPort(port);

        Application.launch(ServerApp.class, args);
    }

    private TCPServer() {}
}