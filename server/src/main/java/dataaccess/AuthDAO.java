package dataaccess;

import model.AuthData;

public interface AuthDAO {
    String addAuth();
    void deleteAuths();
    void removeKey(AuthData authKey);
    boolean findKey(AuthData authKey);
}
