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
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("EF");
        }
        UserData data = userData.getUser(user);
        if (data != null) {
            throw new DataAccessException("Taken");
        }
        userData.addUser(user);
        AuthData a = authData.addAuth(user);
        return new Gson().toJson(new LoginReturn(user.username(), a.token()));
    }

    public String login(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null) {
            throw new DataAccessException("EF");
        }
        UserData data = userData.getUser(user);
        if (data == null) {
            throw new DataAccessException("UE");
        }
        else if (!data.password().equals(user.password())) {
            throw new DataAccessException("PE");
        }
        AuthData a = authData.addAuth(user);
        return new Gson().toJson(new LoginReturn(user.username(), a.token()));
    }

    public void logout(String token) throws DataAccessException {
        if (!authData.findKey(token)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        authData.removeKey(token);
    }

    public String createGame(String gameName, String token) throws DataAccessException {
        if (gameName == null || token == null) {
            throw new DataAccessException("EF");
        }
//        AuthData authKey = new AuthData(token);
        if (!authData.findKey(token)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        int id = gameData.createGame(gameName);
        String result = new Gson().toJson(new GameIDs(id));
        return result;
    }

    public void joinGame(Integer gameID, ChessGame.TeamColor color, String token) throws DataAccessException {
//        AuthData authKey = new AuthData(token);
        if (!authData.findKey(token)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        if (gameID == null || color == null) {
            throw new DataAccessException("EF");
        }
        GameData game = gameData.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Not Found");
        }
        if (color != ChessGame.TeamColor.WHITE && color != ChessGame.TeamColor.BLACK) {
            throw new DataAccessException("Bad Color");
        }
        AuthData key = authData.getKey(token);
        if (color == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() == null) {
                gameData.updatePlayer(gameID, ChessGame.TeamColor.WHITE, key.username());
            }
            else {
                throw new DataAccessException("Taken");
            }
        }
        else if (color == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() == null) {
                gameData.updatePlayer(gameID, ChessGame.TeamColor.BLACK, key.username());
            }
            else {
                throw new DataAccessException("Taken");
            }
        }
    }

    public String listGames(String token) throws DataAccessException {
//        AuthData authKey = new AuthData(token);
        if (!authData.findKey(token)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        return new Gson().toJson(new GameList(gameData.getGamesList()));
    }
}
