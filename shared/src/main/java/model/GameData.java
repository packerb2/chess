package model;

import com.google.gson.Gson;

public record GameData(Integer gameID, String player_white, String player_black, String gameName) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
