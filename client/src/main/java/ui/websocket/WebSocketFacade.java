package ui.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.serverMessages.ServerNotification;
import webSocketMessages.userCommands.*;


import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;
    String authString;

    public WebSocketFacade(String url, NotificationHandler notificationHandler, String authString)  {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");
            this.notificationHandler = notificationHandler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxSessionIdleTimeout(600 * 60 * 1000);
            this.session = container.connectToServer(this, socketURI);
            this.session.setMaxIdleTimeout(6000000);
            this.authString = authString;
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage sm = new Gson().fromJson(message, ServerMessage.class);
                    if (sm.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                        ServerNotification sn = new Gson().fromJson(message, ServerNotification.class);
                        notificationHandler.notify(sn.getMessage());
                    }
                    else if (sm.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
                        LoadGame lg = new Gson().fromJson(message, LoadGame.class);
                        notificationHandler.loadGame(lg.getMessage());
                    }
                    else if (sm.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                        Error err = new Gson().fromJson(message, Error.class);
                        notificationHandler.notify(err.getErrorMsg());
                    }
                    else {
                        System.out.println("WSF onMessage problem: not recognizing ServerMessage when converting" +
                                "from JSON");
                    }
                    // consider gettingType from^^, then creating a new object using Gson again.
                }
            });

        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinPlayer(Integer gameID, ChessGame.TeamColor playerColor) throws IOException {
        JoinPlayer join = new JoinPlayer(authString, gameID, playerColor);
        join.setCommandType(UserGameCommand.CommandType.JOIN_PLAYER);
        this.session.getBasicRemote().sendText(new Gson().toJson(join));
        //System.out.println("sent join player: " + new Gson().toJson(join));
    }

    public void joinObserver(Integer gameID) throws IOException {
        JoinObserver joinObserver = new JoinObserver(authString, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(joinObserver));
    }

    public void leave(Integer gameID) throws IOException {
        Leave leave = new Leave(authString, gameID);
        leave.setCommandType(UserGameCommand.CommandType.LEAVE);
        this.session.getBasicRemote().sendText(new Gson().toJson(leave));
        this.session.close();
    }

    public void makeMove(Integer gameID, ChessMove move) throws IOException {
        MakeMove makeMove = new MakeMove(authString, gameID, move);
        this.session.getBasicRemote().sendText(new Gson().toJson(makeMove));
    }

    public void resign(Integer gameID) throws IOException {
        Resign resign = new Resign(authString, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(resign));
    }
}
