package server.model;

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

    public static int port() {
        return Integer.parseInt(props.getProperty("server.port", "3000"));
    }

    private AppConfig() {}
}