package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData addAuth(UserData userData);
    void deleteAuths();
    void removeKey(AuthData authKey);
    boolean findKey(AuthData authKey);
}
