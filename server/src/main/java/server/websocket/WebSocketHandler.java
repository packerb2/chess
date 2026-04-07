package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand action = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CONNECT -> connect(action.CommandType(), ctx.session);
                case LEAVE -> leave(action.CommandType(), ctx.session);
                case RESIGN -> resign(action.CommandType(), ctx.session);
                case MAKE_MOVE -> makeMove(action.CommandType(), ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String visitorName, Session session) throws IOException {
        connections.add(session);
        var message = String.format("%s is in the shop", visitorName);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(session, notification);
    }

    public void leave(String petName, String sound) throws Exception{
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(null, notification);
        } catch (Exception ex) {
            throw ex;
        }
    }

    private void resign(String visitorName, Session session) throws IOException {
        var message = String.format("%s left the shop", visitorName);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(session, notification);
        connections.remove(session);
    }

    public void makeMove(String petName, String sound) throws Exception{
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
            connections.broadcast(null, notification);
        } catch (Exception ex) {
            throw ex;
        }
    }
}