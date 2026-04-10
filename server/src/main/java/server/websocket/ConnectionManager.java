package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConnectionManager {
    public final HashMap<Integer, ArrayList<Session>> room = new HashMap<>();

    public void add(Session session, Integer id) {
        ArrayList<Session> connection = room.get(id);
        if (connection == null) {
            connection = new ArrayList<>();
        }
        connection.add(session);
        room.put(id, connection);
    }

    public void remove(Session session, Integer id) {
        ArrayList<Session> connection = room.get(id);
        connection.remove(session);
        room.put(id, connection);
    }

    public void broadcast(Session excludeSession, ServerMessage notification, Integer id) throws IOException {
        ArrayList<Session> connection = room.get(id);
        String msg = new Gson().toJson(notification);
        for (Session c : connection) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}