package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private ArrayList<AuthData> data;

    @Override
    public String addAuth() {
        String token = generateToken();
        AuthData authKey = new AuthData(token);
        data.add(authKey);
        return token;
    }

    @Override
    public void deleteAuths() {
        data.clear();
    }

    @Override
    public void removeKey(AuthData authKey) {
        data.remove(authKey);
    }

    @Override
    public boolean findKey(AuthData authKey) {
        return data.contains(authKey);
    }
}
