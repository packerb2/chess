package model;

import com.google.gson.Gson;

public record AuthData(String token, UserData userData) {

    public String toString() {
        return new Gson().toJson(this);
    }
}
