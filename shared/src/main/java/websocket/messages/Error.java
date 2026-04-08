package websocket.messages;

public class Error extends ServerMessage {
    public Error(String message) {
        super(ServerMessageType.ERROR, message);
    }
}
