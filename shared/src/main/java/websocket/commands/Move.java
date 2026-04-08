package websocket.commands;

import chess.ChessMove;

public class Move extends UserGameCommand {
    public Move(String authToken, Integer gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID, move);
    }
}
