package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import javax.management.Notification;
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

    public void broadcast(String excludeUsername, Notification notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.authString.equals(excludeUsername)) {
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
