package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.Action;
import webSocketMessages.Notification;

import java.io.IOException;

public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    public void handleMessage(WsMessageContext ctx) {
        try {
            Action action = new Gson().fromJson(ctx.message(), Action.class);
            switch (action.type()) {
                case ENTER -> enter(action.visitorName(), ctx.session);
                case EXIT -> exit(action.visitorName(), ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void enter(String visitorName, Session session) throws IOException {
        connections.add(session);
        var message = String.format("%s is in the shop", visitorName);
        var notification = new Notification(Notification.Type.ARRIVAL, message);
        connections.broadcast(session, notification);
    }

    private void exit(String visitorName, Session session) throws IOException {
        var message = String.format("%s left the shop", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(session, notification);
        connections.remove(session);
    }
}
