package model;

import chess.ChessGame;
import com.google.gson.Gson;

public record JoinGameData(String gameID, ChessGame.TeamColor color, UserData user) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
