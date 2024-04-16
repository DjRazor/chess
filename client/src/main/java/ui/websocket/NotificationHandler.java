package ui.websocket;

public interface NotificationHandler {
    void notify(String message);
    void loadGame(String message);
}
