package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData addAuth(UserData userData) throws DataAccessException;
    void deleteAuths();
    void removeKey(String token);
    boolean findKey(String token);
    AuthData getKey(String token);
}
