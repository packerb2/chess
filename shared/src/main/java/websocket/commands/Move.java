package websocket.commands;

public class Move extends UserGameCommand {
    public Move(String authToken, Integer gameID) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
    }
}
