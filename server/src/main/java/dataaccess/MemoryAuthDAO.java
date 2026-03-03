package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public ArrayList<String> data = new ArrayList<>();

    @Override
    public AuthData addAuth(UserData userData) {
        String token = generateToken();
        AuthData authKey = new AuthData(token);
        data.add(token);
        return authKey;
    }

    @Override
    public void deleteAuths() {
        if (data != null) {
            data.clear();
        }
    }

    @Override
    public void removeKey(AuthData authKey) {
        if (data != null) {
            data.remove(authKey.token());
        }
    }

    @Override
    public boolean findKey(AuthData authKey) {
        if (data == null) {
            return false;
        }
        return data.contains(authKey.token());
    }
}
