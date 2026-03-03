package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData addAuth(UserData userData);
    void deleteAuths();
    void removeKey(String token);
    boolean findKey(String token);
    AuthData getKey(String token);
}
