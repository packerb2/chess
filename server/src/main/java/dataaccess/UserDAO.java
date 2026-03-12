package dataaccess;

import model.UserData;

public interface UserDAO {
    UserData getUser(UserData info);
    void addUser(UserData info) throws DataAccessException;
    void deleteUsers() throws DataAccessException;
}
