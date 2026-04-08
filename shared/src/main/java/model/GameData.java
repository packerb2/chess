package model;

import chess.ChessGame;
import com.google.gson.Gson;

public record GameData(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game,
                       boolean playing) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
