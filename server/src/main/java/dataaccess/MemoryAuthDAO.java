package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public ArrayList<AuthData> data = new ArrayList<>();

    @Override
    public AuthData addAuth(UserData userData) {
        String token = generateToken();
        AuthData authKey = new AuthData(token, userData.username());
        while (findKey(authKey.token())) {
            token = generateToken();
            authKey = new AuthData(token, userData.username());
        }
        data.add(authKey);
        return authKey;
    }

    @Override
    public void deleteAuths() {
        if (data != null) {
            data.clear();
        }
    }

    @Override
    public void removeKey(String token) {
        if (data != null) {
            data.removeIf(key -> key.token().equals(token));
        }
    }

    @Override
    public boolean findKey(String token) {
        if (data.isEmpty() || token == null) {
            return false;
        }
        for (AuthData key : data) {
            if (key.token().equals(token)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AuthData getKey(String token) {
        if (data.isEmpty() || token == null) {
            return null;
        }
        for (AuthData key : data) {
            if (key.token().equals(token)) {
                return key;
            }
        }
        return null;
    }
}
