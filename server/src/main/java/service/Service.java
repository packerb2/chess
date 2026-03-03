package service;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.UserDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.*;
import model.*;

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
        return new Gson().toJson(new loginReturn(user.username(), a.token()));
    }

    public String login(UserData user) throws DataAccessException {
        UserData data = userData.getUser(user);
        if (data == null) {
            throw new DataAccessException("UE");
        }
        else if (!data.password().equals(user.password())) {
            throw new DataAccessException("PE");
        }
        AuthData a = authData.addAuth(user);
        return new Gson().toJson(new loginReturn(user.username(), a.token()));
    }

    public String logout(AuthData authKey) throws DataAccessException {
        if (!authData.findKey(authKey)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        authData.removeKey(authKey);
        return new Gson().toJson("");
    }

    public String createGame(String gameName, String token) throws DataAccessException {
        AuthData authKey = new AuthData(token);
        if (!authData.findKey(authKey)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        int id = gameData.createGame(gameName);
        return new Gson().toJson(new gameIDs(id));
    }

    public String joinGame(int gameID, ChessGame.TeamColor color, String token, UserData user) throws DataAccessException {
        AuthData authKey = new AuthData(token);
        if (!authData.findKey(authKey)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        GameData game = gameData.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Not Found");
        }
        if (color == ChessGame.TeamColor.WHITE) {
            if (game.player_white() == null) {
                gameData.updatePlayer(gameID, ChessGame.TeamColor.WHITE, user.username());
            }
            else {
                throw new DataAccessException("Taken");
            }
        }
        else if (color == ChessGame.TeamColor.BLACK) {
            if (game.player_black() == null) {
                gameData.updatePlayer(gameID, ChessGame.TeamColor.BLACK, user.username());
            }
            else {
                throw new DataAccessException("Taken");
            }
        }
        return new Gson().toJson("");
    }

    public String listGames(String token) throws DataAccessException {
        AuthData authKey = new AuthData(token);
        if (!authData.findKey(authKey)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        return new Gson().toJson(gameData.getGamesList());
    }
}
