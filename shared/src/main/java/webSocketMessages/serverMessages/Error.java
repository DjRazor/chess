package webSocketMessages.serverMessages;

public class Error extends ServerMessage {
    private String errorMessage;
    public Error(ServerMessageType type, String errorMsg) {
        super(type);
        this.errorMessage = errorMsg;
    }

    public String getErrorMsg() {
        return "Error: " + errorMessage;
    }
}
