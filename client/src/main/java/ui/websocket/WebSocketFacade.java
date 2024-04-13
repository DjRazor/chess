package ui.websocket;

import com.google.gson.Gson;
import dataAccess.DataAccessException;
import webSocketMessages.Notification;
import webSocketMessages.userCommands.UserGameCommand;


import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;
    String authString;

    public WebSocketFacade(String url, NotificationHandler notificationHandler, String authString) throws DataAccessException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");
            this.notificationHandler = notificationHandler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.authString = authString;
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Notification notification = new Gson().fromJson(message, Notification.class);
                    notificationHandler.notify(notification);
                }
            });

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new DataAccessException("WS setup error: " + ex.getMessage());
        }
    }

    // Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinPlayer(Integer gameID) throws DataAccessException {
        try {
            UserGameCommand join = new UserGameCommand(authString);
            join.setCommandType(UserGameCommand.CommandType.JOIN_PLAYER);
            this.session.getBasicRemote().sendText(new Gson().toJson(join));
            System.out.println("sent join player: " + new Gson().toJson(join));
        } catch (IOException ex) {
            throw new DataAccessException("WSF joinPlayer error: " + ex.getMessage());
        }
    }

    public void leave() throws DataAccessException {
        try {
            UserGameCommand leave = new UserGameCommand(authString);
            leave.setCommandType(UserGameCommand.CommandType.LEAVE);
            this.session.getBasicRemote().sendText(new Gson().toJson(leave));
            this.session.close();
        } catch (IOException ex) {
            throw new DataAccessException("WSF leave error: " + ex.getMessage());
        }
    }
}
