package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.SqlAuthDAO;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.Notification;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.ServerNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private AuthDAO authDAO;
    {
        try {
            authDAO = new SqlAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(String username, String authString, Integer gameID, ChessGame.TeamColor teamColor, Session session) {
        var connection = new Connection(username, authString, gameID, teamColor, session);
        connections.put(authString, connection);
        System.out.println(connections);
    }

    public void remove(String authString) {
        connections.remove(authString);
    }

    public void broadcast(String authString, ServerMessage serverMessage) throws IOException {
        System.out.println("made to broadcast");
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.authToken.equals(authString)) {
                    c.send(new Gson().toJson(serverMessage));
                }
            } else {
                System.out.println("removed session: " + c);
                removeList.add(c);
            }
        }
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }

    public void broadcastAll(Integer gameID, ServerMessage serverMessage) throws IOException {
        System.out.println("made to broadcastAll");
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.gameID.equals(gameID)) {
                    c.send(new Gson().toJson(serverMessage));
                }
            } else {
                System.out.println("removed session: " + c);
                removeList.add(c);
            }
        }
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }

    // May need to make separate broadcastGameAll
    public void broadcastGame(String authToken, Integer gameID, LoadGame loadGame) throws IOException {
        System.out.println("made it to broadcastGame");
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (Objects.equals(c.gameID, gameID) && c.authToken.equals(authToken)) {
                    c.send(new Gson().toJson(loadGame));
                }
            }
        }
    }

    public void broadcastGameAll(Integer gameID, LoadGame loadGame) throws IOException {
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (Objects.equals(c.gameID, gameID)) {
                    c.send(new Gson().toJson(loadGame));
                }
            }
        }
    }

    public void broadcastToOne(Session session, ServerMessage serverMessage) throws IOException {
        System.out.println("made it to broadcastToOne");
        if (session.isOpen()) {
            session.getRemote().sendString(new Gson().toJson(serverMessage));
        }
    }

    public boolean checkAvail(Integer gameID, ChessGame.TeamColor teamColor) {
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (Objects.equals(c.gameID, gameID) && teamColor.equals(c.teamColor)) {
                    System.out.println("Failed checkAvail");
                    return false;
                }
            }
        }
        return true;
    }

    public boolean properColor(String username, ChessGame.TeamColor playerColor) throws DataAccessException {
        for (var c : connections.values()) {
            if (c.username.equals(username) && c.teamColor == playerColor) {
                return true;
            }
        }
        System.out.println("Failed properColor");
        return false;
    }

    public boolean userFound(String authToken) {
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.authToken.equals(authToken)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean misMatch(String username) {
        for (var c : connections.values()) {
            if (c.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public ChessGame.TeamColor getTeamColor(String authToken) {
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.authToken.equals(authToken)) {
                    return c.teamColor;
                }
            }
        }
        return null;
    }
}
