package model;

import com.google.gson.Gson;

public record GameData(Integer gameID, UserData player_white, UserData player_black, String gameName) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
