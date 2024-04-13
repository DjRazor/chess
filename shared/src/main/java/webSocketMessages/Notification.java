package webSocketMessages;

import com.google.gson.Gson;
import webSocketMessages.userCommands.UserGameCommand;

public record Notification(UserGameCommand.CommandType type, String message) {
    public String toString() {
        return new Gson().toJson(this);
    }
}
