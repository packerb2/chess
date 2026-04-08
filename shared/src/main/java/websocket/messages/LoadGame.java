package websocket.messages;

import model.GameData;

public class LoadGame extends ServerMessage {

    private final GameData game;

    public LoadGame(GameData game) {
        super(ServerMessageType.LOAD_GAME, null, null);
        this.game = game;
    }
}
