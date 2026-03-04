package client.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {}
    }

    public static String serverIp() {
        return props.getProperty("server.ip", "127.0.0.1");
    }

    public static int serverPort() {
        return Integer.parseInt(props.getProperty("server.port", "3000"));
    }

    private AppConfig() {}
}