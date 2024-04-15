package webSocketMessages.serverMessages;

public class ServerNotification extends ServerMessage {
    private String message;
    public ServerNotification(ServerMessageType type, String message) {
        super(type);
        this.message = message;
        this.serverMessageType = ServerMessageType.NOTIFICATION;
    }

    public String getMessage() {
        return message;
    }
}
