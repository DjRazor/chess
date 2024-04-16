package server.websocket;

import chess.*;
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
import spark.Response;
import webSocketMessages.Notification;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.ServerNotification;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.Scanner;

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
    public void onMessage(Session session, String message) throws IOException, DataAccessException, InvalidMoveException {
        System.out.println("WSH received message: " + message);
        UserGameCommand cmd = new Gson().fromJson(message, UserGameCommand.class);
        switch (cmd.getCommandType()) {
            case JOIN_PLAYER -> {
                JoinPlayer joinCmd = new Gson().fromJson(message, JoinPlayer.class);
                joinPlayer(joinCmd, session);
            }
            case JOIN_OBSERVER -> {
                JoinObserver joinObserverCmd = new Gson().fromJson(message, JoinObserver.class);
                joinObserver(joinObserverCmd, session);
            }
            case MAKE_MOVE -> {
                MakeMove makeMoveCmd = new Gson().fromJson(message, MakeMove.class);
                makeMove(makeMoveCmd, session);
            }
            case LEAVE -> {
                Leave leaveCmd = new Gson().fromJson(message, Leave.class);
                leave(leaveCmd, session);
            }
            case RESIGN -> {
                Resign resignCmd = new Gson().fromJson(message, Resign.class);
                resign(resignCmd, session);
            }
        }
    }

    // private void CMD for each cmd in UserGameCommand
    private void joinPlayer(JoinPlayer joinCmd, Session session) throws DataAccessException, IOException {
        // Checks if spot is taken
        var username = authDAO.usernameForAuth(joinCmd.getAuthString());
        boolean avail = connections.checkAvail(joinCmd.getGameID(), joinCmd.getPlayerColor());
        boolean userFound = connections.userFound(joinCmd.getAuthString());
        boolean validAuth = authDAO.validateAuth(joinCmd.getAuthString());
        boolean gameIDInUse = gameDAO.gameIDInUse(joinCmd.getGameID());
        boolean misMatch = connections.misMatch(username);
        boolean properColor;
        GameData currentGame = gameDAO.getGame(joinCmd.getGameID());
        if (userFound) {
            properColor = connections.properColor(username, joinCmd.getPlayerColor());
        } else {
            properColor = true;
        }
        // Checks gameID validity
        if (!gameIDInUse) {
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "WSH: Invalid gameID");
            connections.broadcastToOne(session, error);
        }

        else if (!validAuth) {
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "WSH: Invalid authToken");
            connections.broadcastToOne(session, error);
        }

        // Checks white availability
        else if (joinCmd.getPlayerColor() == ChessGame.TeamColor.WHITE && !username.equals(currentGame.whiteUsername())) {
            System.out.println(currentGame.whiteUsername());
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "White taken");
            System.out.println("sent white taken error");
            connections.broadcastToOne(session, error);
        }

        // Checks white availability
        else if (joinCmd.getPlayerColor() == ChessGame.TeamColor.BLACK && !username.equals(currentGame.blackUsername())) {
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Black taken");
            connections.broadcastToOne(session, error);
        }

        else if (misMatch && userFound) {
            System.out.println("Mismatch");
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Taken spot/Wrong Team");
            connections.broadcastToOne(session, error);
        }
        else if (joinCmd.getPlayerColor() == ChessGame.TeamColor.NONE) {
            System.out.println("Empty Team");
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Empty Team");
            connections.broadcastToOne(session, error);
        }
        else if (avail && properColor && validAuth && gameIDInUse) {
            // Updates game
            if (joinCmd.getPlayerColor().equals(ChessGame.TeamColor.BLACK)) {
                GameData updatedGameData = new GameData(currentGame.gameID(), currentGame.whiteUsername(), username,
                        currentGame.gameName(), currentGame.game());
                gameDAO.updateGame(updatedGameData);
            } else if (joinCmd.getPlayerColor().equals(ChessGame.TeamColor.WHITE)) {
                GameData updatedGameData = new GameData(currentGame.gameID(), username, currentGame.blackUsername(),
                        currentGame.gameName(), currentGame.game());
                gameDAO.updateGame(updatedGameData);
            }

            connections.add(username, joinCmd.getAuthString(), joinCmd.getGameID(), joinCmd.getPlayerColor(), session);
            System.out.println("added " + username + " to connections\n");
            var message = String.format("%s has entered the game as %s.", username, joinCmd.getPlayerColor());
            var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(joinCmd.getAuthString(), joinCmd.getGameID(), notification);
            var loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, "loaded game", "game hold");
            connections.broadcastGame(joinCmd.getAuthString(), joinCmd.getGameID(), loadGame);
        } else {
            System.out.println("EXISTING USER");
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Taken spot/Wrong Team");
            connections.broadcastToOne(session, error);
        }
    }

    private void joinObserver(JoinObserver joinObserverCmd, Session session) throws DataAccessException, IOException {
        boolean gameIDInUse = gameDAO.gameIDInUse(joinObserverCmd.getGameID());
        boolean validAuth = authDAO.validateAuth(joinObserverCmd.getAuthString());
        if (gameIDInUse && validAuth) {
            var username = authDAO.usernameForAuth(joinObserverCmd.getAuthString());
            connections.add(username, joinObserverCmd.getAuthString(), joinObserverCmd.getGameID(), ChessGame.TeamColor.NONE, session);
            var message = String.format("%s is observing the game", username);
            var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(joinObserverCmd.getAuthString(), joinObserverCmd.getGameID(), notification);
            var loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME,
                    "You are now observing game: " + joinObserverCmd.getGameID(), "game hold");
            connections.broadcastToOne(session, loadGame);
        } else {
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "WSH: Invalid auth/gameID");
            connections.broadcastToOne(session, error);
        }
    }

    private void makeMove(MakeMove makeMoveCmd, Session session) throws DataAccessException, IOException, InvalidMoveException {
        GameData currentGameData = gameDAO.getGame(makeMoveCmd.getGameID());
        ChessBoard currentBoard = currentGameData.game().getBoard();
        ChessMove move = makeMoveCmd.getMove();
        var validMoves = currentGameData.game().validMoves(move.getStartPosition());
        ChessGame.TeamColor currentColor = currentGameData.game().getTeamTurn();
        ChessGame.TeamColor userColor = connections.getTeamColor(makeMoveCmd.getAuthString());
        validMoves.removeIf(mv -> !mv.getStartPosition().equals(move.getStartPosition())
                || mv.getEndPosition().equals(move.getEndPosition()));

        if (!currentColor.equals(userColor)) {
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Not your turn.");
            connections.broadcastToOne(session, error);
        }
        else if (validMoves.isEmpty()) {
            //throw new DataAccessException("Not a valid move.");
            //Send Error
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Not a valid move");
            connections.broadcastToOne(session, error);
        }
        // if the size is > 0, it could only be because the piece
        // is a pawn and can be promoted
        else {
            if (validMoves.size() > 1) {
                //ChessPiece promoPiece = getPromoPiece(currentGameData.game().getTeamTurn());
                currentGameData.game().makeMove(new ChessMove(move.getStartPosition(), move.getEndPosition(), null));
            }
            else {
                currentGameData.game().makeMove(move);

            }
            gameDAO.updateGame(currentGameData);
            String message = "Moved " + move.getStartPosition() + " to " + move.getEndPosition();
            var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(makeMoveCmd.getAuthString(), currentGameData.gameID(), notification);
            var gameUpdate = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, "", "game game");
            connections.broadcastGameAll(makeMoveCmd.getGameID(), gameUpdate);
        }
    }

    private void leave(Leave leaveCmd, Session session) throws DataAccessException, IOException {
        connections.remove(leaveCmd.getAuthString());
        var username = authDAO.usernameForAuth(leaveCmd.getAuthString());
        var message = String.format("%s has left the game.", username);
        var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(leaveCmd.getAuthString(), leaveCmd.getGameID(), notification);
    }

    private void resign(Resign resignCmd, Session session) throws DataAccessException, IOException {
        GameData currentGameData = gameDAO.getGame(resignCmd.getGameID());
        // Checks if game has been resigned already
        if (currentGameData.game().getTeamTurn() == ChessGame.TeamColor.NONE) {
            var error  = new Error(ServerMessage.ServerMessageType.ERROR, "Game has already been resigned");
            connections.broadcastToOne(session, error);
        }

        // Checks if user is an observer and blocks resign request if so
        else if (connections.getTeamColor(resignCmd.getAuthString()) == ChessGame.TeamColor.NONE) {
            var error  = new Error(ServerMessage.ServerMessageType.ERROR, "You cannot resign" +
                    " a game as an observer.");
            connections.broadcastToOne(session, error);
        }
        else {
            currentGameData.game().setTeamTurn(ChessGame.TeamColor.NONE);
            gameDAO.updateGame(currentGameData);
            var message = String.format("%s has resigned. Game over.", authDAO.usernameForAuth(resignCmd.getAuthString()));
            var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcastAll(resignCmd.getGameID(), notification);
        }
    }
}
