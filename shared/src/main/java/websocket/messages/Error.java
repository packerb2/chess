package websocket.messages;

public class Error extends ServerMessage {
    public Error(String errorMessage) {
        super(ServerMessageType.ERROR, null, errorMessage);
    }
}
