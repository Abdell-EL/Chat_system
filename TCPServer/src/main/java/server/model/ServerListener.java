package server.model;

import java.util.List;

public interface ServerListener {
    void onLog(String line);
    void onUsersChanged(List<String> usernames);
}