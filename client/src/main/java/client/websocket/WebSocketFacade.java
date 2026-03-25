//package client.websocket;
//
//import com.google.gson.Gson;
//import dataaccess.DataAccessException;
//import io.javalin.router.Endpoint;
//import org.eclipse.jetty.websocket.api.Session;
//import org.eclipse.jetty.websocket.api.WebSocketContainer;
//import org.eclipse.jetty.websocket.core.internal.MessageHandler;
//import webSocketMessages.Action;
//import webSocketMessages.Notification;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//
//public class WebSocketFacade extends Endpoint {
//
//    Session session;
//    NotificationHandler notificationHandler;
//
//    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws DataAccessException {
//        super();
//        try {
//            url = url.replace("http", "ws");
//            URI socketURI = new URI(url + "/ws");
//            this.notificationHandler = notificationHandler;
//
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            this.session = container.connectToServer(this, socketURI);
//
//            //set message handler
//            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
//                @Override
//                public void onMessage(String message) {
//                    Notification notification = new Gson().fromJson(message, Notification.class);
//                    notificationHandler.notify(notification);
//                }
//            });
//        } catch (IOException | URISyntaxException ex) {
//            throw new DataAccessException(ex.getMessage());
//        }
//    }
//
//
//    @Override
//    public void onOpen(Session session, EndpointConfig endpointConfig) {
//    }
//
//    public void enterPetShop(String visitorName) throws DataAccessException {
//        try {
//            var action = new Action(Action.Type.ENTER, visitorName);
//            this.session.getBasicRemote().sendText(new Gson().toJson(action));
//        } catch (IOException ex) {
//            throw new DataAccessException(ex.getMessage());
//        }
//    }
//
//    public void leavePetShop(String visitorName) throws DataAccessException {
//        try {
//            var action = new Action(Action.Type.EXIT, visitorName);
//            this.session.getBasicRemote().sendText(new Gson().toJson(action));
//        } catch (IOException ex) {
//            throw new DataAccessException(ex.getMessage());
//        }
//    }
//}
