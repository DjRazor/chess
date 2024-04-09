package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public String authString;
    public Session session;

    public Connection(String authString, Session session) {
        this.authString = authString;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}
