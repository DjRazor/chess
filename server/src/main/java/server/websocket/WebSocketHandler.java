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
                leave(cmd.getAuthString());
            }
            case RESIGN -> {
                Resign resignCmd = new Gson().fromJson(message, Resign.class);
                resign(resignCmd);
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
        if (userFound) {
            properColor = connections.properColor(username, joinCmd.getPlayerColor());
        } else {
            properColor = true;
        }
        if (misMatch && userFound) {
            System.out.println("Mismatch");
            var error = new Error(ServerMessage.ServerMessageType.ERROR, "Taken spot/Wrong Team");
            connections.broadcastToOne(session, error);
        }
        else if (avail && properColor && validAuth && gameIDInUse) {
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

    private void joinObserver(JoinObserver joinObserverCmd, Session session) throws DataAccessException, IOException {
        boolean gameIDInUse = gameDAO.gameIDInUse(joinObserverCmd.getGameID());
        boolean validAuth = authDAO.validateAuth(joinObserverCmd.getAuthString());
        if (gameIDInUse && validAuth) {
            var username = authDAO.usernameForAuth(joinObserverCmd.getAuthString());
            connections.add(username, joinObserverCmd.getAuthString(), joinObserverCmd.getGameID(), null, session);
            var message = String.format("%s is observing the game", username);
            var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(joinObserverCmd.getAuthString(), notification);
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
            connections.broadcast(makeMoveCmd.getAuthString(), notification);
            var gameUpdate = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, "", "game game");
            connections.broadcastGameAll(makeMoveCmd.getGameID(), gameUpdate);
        }
    }

    private void leave(String authString) throws DataAccessException, IOException {
        connections.remove(authString);
        var username = authDAO.usernameForAuth(authString);
        var message = String.format("%s has left the game.", username);
        var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(username, notification);
    }

    private void resign(Resign resignCmd) throws DataAccessException, IOException {
        GameData currentGameData = gameDAO.getGame(resignCmd.getGameID());
        currentGameData.game().setBoard(null);
        gameDAO.updateGame(currentGameData);
        var message = String.format("%s has resigned. Game over.", authDAO.usernameForAuth(resignCmd.getAuthString()));
        var notification = new ServerNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(resignCmd.getAuthString(), notification);
    }

    private String convertIntToLetter(int num) {
        String s;
        switch (num) {
            case 1 -> s = "a";
            case 2 -> s = "b";
            case 3 -> s = "c";
            case 4 -> s = "d";
            case 5 -> s = "e";
            case 6 -> s = "f";
            case 7 -> s = "g";
            case 8 -> s = "h";
            default -> s = "x";
        }
        return s;
    }

    private static ChessPiece getPromoPiece(ChessGame.TeamColor currentColor) {
        Scanner promoScan = new Scanner(System.in);
        while (true) {
            System.out.println("Enter desired promo piece letter(Q,N,R,B):");
            String line = promoScan.nextLine();
            line = line.toUpperCase();
            switch (line) {
                case "Q" -> { return new ChessPiece(currentColor, ChessPiece.PieceType.QUEEN); }
                case "B" -> { return new ChessPiece(currentColor, ChessPiece.PieceType.BISHOP); }
                case "N" -> { return new ChessPiece(currentColor, ChessPiece.PieceType.KNIGHT); }
                case "R" -> { return new ChessPiece(currentColor, ChessPiece.PieceType.ROOK); }
            }
        }
    }
}
