package webSocketMessages.serverMessages;

public class LoadGame extends ServerMessage {
    private String message;
    private String game;
    public LoadGame(ServerMessageType type, String message, String game) {
        super(type);
        this.message = message;
        this.game = game;
    }

    public String getMessage() {
        return message;
    }
}
