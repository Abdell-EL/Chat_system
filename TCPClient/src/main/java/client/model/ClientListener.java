package client.model;

import java.util.List;

public interface ClientListener {
    void onInfo(String text);
    void onChat(String from, String time, String text);
    void onUsers(List<String> users);
    void onDisconnected();
}