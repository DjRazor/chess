package webSocketMessages.userCommands;

public class Resign extends UserGameCommand {
    private Integer gameID;
    public Resign(String authToken, Integer gameID) {
        super(authToken);
        this.gameID = gameID;
        commandType = CommandType.RESIGN;
    }

    public Integer getGameID() {
        return gameID;
    }
}
