package server.websocket;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public Integer gameID;
    public Session session;
    public String authToken;
    public ChessGame.TeamColor teamColor;
    public String username;

    public Connection(String username, String authToken, Integer gameID, ChessGame.TeamColor teamColor, Session session) {
        this.username = username;
        this.authToken = authToken;
        this.gameID = gameID;
        this.session = session;
        this.teamColor = teamColor;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
        System.out.println("Connection.class sent msg: " + msg);
    }
}
