package server.websocket;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.SqlAuthDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.Notification;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    // use UserCommand/ServerMessage
    private final ConnectionManager connections = new ConnectionManager();
    private AuthDAO authDAO;

    {
        try {
            authDAO = new SqlAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        System.out.println("WSH received message: " + message);
        var cmd = new Gson().fromJson(message, UserGameCommand.class);
        switch (cmd.getCommandType()) {
            case JOIN_PLAYER -> joinPlayer(cmd.getAuthString(), session);
            case JOIN_OBSERVER -> joinObserver();
            case MAKE_MOVE -> makeMove();
            case LEAVE -> leave(cmd.getAuthString());
            case RESIGN -> resign();
        }
    }

    @OnWebSocketError
    public void onError(Throwable throwable) throws Throwable {
        throw throwable;
    }

    // private void CMD for each cmd in UserGameCommand
    private void joinPlayer(String authString, Session session) throws DataAccessException, IOException {
        connections.add(authString, session);
        var username = authDAO.usernameForAuth(authString);
        System.out.println("added " + username + " to connections\n");
        var message = String.format("%s has entered the game", username);
        var notification = new Notification(UserGameCommand.CommandType.JOIN_PLAYER, message);
        connections.broadcast(authString, notification);
    }

    private void joinObserver() {

    }

    private void makeMove() {

    }

    private void leave(String authString) throws DataAccessException, IOException {
        connections.remove(authString);
        var username = authDAO.usernameForAuth(authString);
        var message = String.format("%s has left the game", username);
        var notification = new Notification(UserGameCommand.CommandType.LEAVE, message);
        connections.broadcast(username, notification);
    }

    private void resign() {

    }
}
