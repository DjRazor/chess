package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String authString, Session session) {
        var connection = new Connection(authString, session);
        connections.put(authString, connection);
    }

    public void remove(String authString) {
        connections.remove(authString);
    }

    public void broadcast(String authString, Notification notification) throws IOException {
        System.out.println("made to broadcast");
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.authString.equals(authString)) {
                    c.send(notification.toString());
                }
            } else {
                removeList.add(c);
            }
        }
        for (var c : removeList) {
            connections.remove(c.authString);
        }
    }
}
