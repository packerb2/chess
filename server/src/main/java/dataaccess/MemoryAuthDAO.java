package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private ArrayList<String> data;

    @Override
    public String addAuth() {
        String token = generateToken();
        data.add(token);
        return token;
    }

    @Override
    public void deleteAuths() {
        data.clear();
    }
}
