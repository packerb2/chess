package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public SQLAuthDAO() {
        try {new SQLDataAccess();} catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData addAuth(UserData userData) {
        return null;
    }

    @Override
    public void deleteAuths() {

    }

    @Override
    public void removeKey(String token) {

    }

    @Override
    public boolean findKey(String token) {
        return false;
    }

    @Override
    public AuthData getKey(String token) {
        return null;
    }
}
