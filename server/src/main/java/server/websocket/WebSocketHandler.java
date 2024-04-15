package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dataAccess.*;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.ServerFacade;
import webSocketMessages.Notification;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.ServerNotification;
import webSocketMessages.userCommands.JoinPlayer;
import webSocketMessages.userCommands.MakeMove;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    // use UserCommand/ServerMessage
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;
    {
        try {
            authDAO = new SqlAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private final GameDAO gameDAO;
    {
        try {
            gameDAO = new SqlGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        System.out.println("WSH received message: " + message);
        UserGameCommand cmd = new Gson().fromJson(message, UserGameCommand.class);
        switch (cmd.getCommandType()) {
            case JOIN_PLAYER -> {
                JoinPlayer joinCmd = new Gson().fromJson(message, JoinPlayer.class);
                joinPlayer(joinCmd, session);
            }
            case JOIN_OBSERVER -> joinObserver();
            //case MAKE_MOVE -> makeMove();
            case LEAVE -> leave(cmd.getAuthString());
            case RESIGN -> resign();
        }
    }

    @OnWebSocketError
    public void onError(Throwable throwable) throws Throwable {
        throw throwable;
    }

    // private void CMD for each cmd in UserGameCommand
    private void joinPlayer(JoinPlayer joinCmd, Session session) throws DataAccessException, IOException {
        // Checks if spot is taken
        var username = authDAO.usernameForAuth(joinCmd.getAuthString());
        boolean avail = connections.checkAvail(joinCmd.getGameID(), joinCmd.getPlayerColor());
        boolean userFound = connections.userFound(username);
        boolean validAuth = authDAO.validateAuth(joinCmd.getAuthString());
        boolean gameIDInUse = gameDAO.gameIDInUse(joinCmd.getGameID());
        boolean properColor;
        if (userFound) {
            properColor = connections.properColor(username, joinCmd.getPlayerColor());
        } else {
            properColor = true;
        }
        if (avail && properColor && validAuth && gameIDInUse) {
            connections.add(username, joinCmd.getAuthString(), joinCmd.getGameID(), joinCmd.getPlayerColor(), session);
            System.out.println("added " + username + " to connections\n");
            var message = String.format("%s has entered the game as %s.", username, joinCmd.getPlayerColor());
            var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(joinCmd.getAuthString(), notification);
            var loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, "loaded game", "game hold");
            connections.broadcastGame(joinCmd.getAuthString(), joinCmd.getGameID(), loadGame);
        } else {
            System.out.println("EXISTING USER");
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Taken spot/Wrong Team");
            connections.broadcastToOne(session, error);
        }
    }

    private void joinObserver() {

    }

    private void makeMove(MakeMove makeMoveCmd) {

    }

    private void leave(String authString) throws DataAccessException, IOException {
        connections.remove(authString);
        var username = authDAO.usernameForAuth(authString);
        var message = String.format("%s has left the game", username);
        var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(username, notification);
    }

    private void resign() {

    }
}
