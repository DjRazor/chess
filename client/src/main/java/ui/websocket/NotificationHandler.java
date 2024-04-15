package ui.websocket;

import dataAccess.DataAccessException;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.ServerNotification;

public interface NotificationHandler {
    void notify(String message);
    void loadGame(String message);
}
