package model;

import com.google.gson.Gson;

public record AuthData(String token, String username) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
