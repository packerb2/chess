package service;

import dataaccess.UserDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.*;
import model.UserData;

public class Service {

    private UserDAO userData = new MemoryUserDAO();
    private GameDAO gameData = new MemoryGameDAO();
    private AuthDAO authData = new MemoryAuthDAO();

    public Service(UserDAO userData, GameDAO gameData, AuthDAO authData) {
        this.userData = userData;
        this.gameData = gameData;
        this.authData = authData;
    }

    public void clear() {
        userData.deleteUsers();
        gameData.deleteGames();
        authData.deleteAuths();
    }

    public void register(UserData user) throws DataAccessException{
        UserData data = userData.getUser(user);
        if (data != null) {
            throw new DataAccessException("Error: Username is already taken");
        }
        userData.addUser(user);
        userData.deleteUsers();
    }
}
