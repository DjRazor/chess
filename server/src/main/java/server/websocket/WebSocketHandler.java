package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    // use UserCommand/ServerMessage
    private final ConnectionManager connections = new ConnectionManager();
    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand cmd = new Gson().fromJson(message, UserGameCommand.class);
        switch (cmd.getCommandType()) {
            case JOIN_PLAYER -> joinPlayer(cmd.getAuthString(), session);
            case JOIN_OBSERVER -> joinObserver();
            case MAKE_MOVE -> makeMove();
            case LEAVE -> leave(cmd.getAuthString());
            case RESIGN -> resign();
        }
    }

    // private void CMD for each cmd in UserGameCommand
    private void joinPlayer(String authString, Session session) {
        connections.add(authString, session);
    }

    private void joinObserver() {

    }

    private void makeMove() {

    }

    private void leave(String authString) {
        connections.remove(authString);
    }

    private void resign() {

    }
}
