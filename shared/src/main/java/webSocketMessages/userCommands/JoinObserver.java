package webSocketMessages.userCommands;

public class JoinObserver extends UserGameCommand {
    private final Integer gameID;
    public JoinObserver(String authToken, Integer gameID) {
        super(authToken);
        this.gameID = gameID;
        commandType = CommandType.JOIN_OBSERVER;
    }

    public Integer getGameID() {
        return gameID;
    }
}
