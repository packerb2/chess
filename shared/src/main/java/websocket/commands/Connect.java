package websocket.commands;

import chess.ChessGame;

public class Connect extends UserGameCommand {
    public Connect(String authToken, Integer gameID) {
        super(CommandType.CONNECT, authToken, gameID);
    }
}
