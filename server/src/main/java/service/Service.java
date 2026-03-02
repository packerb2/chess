package service;

import com.google.gson.Gson;
import dataaccess.UserDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.*;
import model.AuthData;
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

    public String register(UserData user) throws DataAccessException{
        UserData data = userData.getUser(user);
        if (data != null) {
            throw new DataAccessException("Error: Username is already taken");
        }
        userData.addUser(user);
        AuthData a = authData.addAuth(user);
        return "{username: " + user.username() + ", authToken: " + a.token() + "}";
        //return new Gson().toJson({"username": data.username(), "authToken": a.token()});
    }

    public String login(UserData user) throws DataAccessException {
        UserData data = userData.getUser(user);
        if (data == null) {
            throw new DataAccessException("Error: Credentials are Incorrect");
        }
        if (!data.password().equals(user.password())) {
            throw new DataAccessException("Error: Credentials are Incorrect");
        }
        AuthData a = authData.addAuth(user);
        // format: "{"key": "value"}"
        return "{username: " + data.username() + ", authToken: " + a.token() + "}";
        //return new Gson().toJson({"username": data.username(), "authToken": a.token()});
    }

    public void logout(AuthData authKey) throws DataAccessException {
        if (!authData.findKey(authKey)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        authData.removeKey(authKey);
    }

    public String createGame(String gameName, String token) throws DataAccessException {
        AuthData authKey = new AuthData(token);
        if (!authData.findKey(authKey)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        int id = gameData.createGame(gameName);
        return "{gameID: " + id + "}";
        //return new Gson().toJson({"gameID": id});
        //return "{"gameID": id}";
    }
}
