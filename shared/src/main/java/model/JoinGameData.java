package model;

import chess.ChessGame;
import com.google.gson.Gson;

public record JoinGameData(int gameID, ChessGame.TeamColor playerColor, UserData user) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
