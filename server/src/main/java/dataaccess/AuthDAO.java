package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData addAuth(UserData userData) throws DataAccessException;
    void deleteAuths() throws DataAccessException;
    void removeKey(String token);
    boolean findKey(String token);
    AuthData getKey(String token);
}
