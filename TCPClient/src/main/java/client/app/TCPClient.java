package client.app;

import client.model.AppConfig;
import javafx.application.Application;

public final class TCPClient {

    public static void main(String[] args) {
        String ip = AppConfig.serverIp();
        int port = AppConfig.serverPort();

        // args override config
        if (args.length >= 1 && args[0] != null && !args[0].isBlank()) {
            ip = args[0].trim();
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException ignored) {}
        }

        // pass to JavaFX app before launch
        ClientApp.setConnectionTarget(ip, port);

        Application.launch(ClientApp.class, args);
    }

    private TCPClient() {}
}