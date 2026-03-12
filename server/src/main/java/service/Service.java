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

    public int clear() {
        try {
            userData.deleteUsers();
            gameData.deleteGames();
            authData.deleteAuths();
            return 0;
        } catch (DataAccessException e) {
            return -1;
        }
    }

    public String register(UserData user) throws DataAccessException{
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("EF");
        }
        try {
            UserData data = userData.getUser(user);
            if (data != null) {
                throw new DataAccessException("Taken");
            }
            userData.addUser(user);
            AuthData a = authData.addAuth(user);
            return new Gson().toJson(new LoginReturn(user.username(), a.token()));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Taken")) {
                throw e;
            }
            throw new DataAccessException("Server Error");
        }
    }

    public String login(UserData user) throws DataAccessException {
        try {
            if (user.username() == null || user.password() == null) {
                throw new DataAccessException("EF");
            }
            UserData data = userData.getUser(user);
            if (data == null) {
                throw new DataAccessException("NU");
            }
            if (data.username().equals("ERROR")) {
                throw new DataAccessException("SE");
            }
            AuthData a = authData.addAuth(user);
            return new Gson().toJson(new LoginReturn(user.username(), a.token()));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("EF") || e.getMessage().equals("NU")) {
                throw e;
            }
            else {
                throw new DataAccessException("Server Error");
            }
        }
    }

    public void logout(String token) throws DataAccessException {
        if (!authData.findKey(token)) {
            throw new DataAccessException("NA");
        }
        try {
            authData.removeKey(token);
        } catch (DataAccessException e) {
            throw new DataAccessException("Server Error");
        }
    }

    public String createGame(String gameName, String token) throws DataAccessException {
        try {
            if (gameName == null || token == null) {
                throw new DataAccessException("EF");
            }
            authData.findKey(token);
            if (!authData.findKey(token)) {
                throw new DataAccessException("NA");
            }
            int id = gameData.createGame(gameName);
            return new Gson().toJson(new GameIDs(id));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("EF") || e.getMessage().equals("NA")) {
                throw e;
            }
            throw new DataAccessException("System Error");
        }
    }

    public void joinGame(Integer gameID, ChessGame.TeamColor color, String token) throws DataAccessException {
        try {
            if (!authData.findKey(token)) {
                throw new DataAccessException("NA");
            }
            if (gameID == null || color == null) {
                throw new DataAccessException("EF");
            }
            GameData game = gameData.getGame(gameID);
            if (game == null) {
                throw new DataAccessException("NF");
            }
            if (color != ChessGame.TeamColor.WHITE && color != ChessGame.TeamColor.BLACK) {
                throw new DataAccessException("BC");
            }
            AuthData key = authData.getKey(token);
            if (color == ChessGame.TeamColor.WHITE) {
                if (game.whiteUsername() == null) {
                    gameData.updatePlayer(gameID, ChessGame.TeamColor.WHITE, key.username());
                } else {
                    throw new DataAccessException("T");
                }
            } else {
                if (game.blackUsername() == null) {
                    gameData.updatePlayer(gameID, ChessGame.TeamColor.BLACK, key.username());
                } else {
                    throw new DataAccessException("T");
                }
            }
        } catch (DataAccessException e) {
            if (e.getMessage().equals("NA")
                    || e.getMessage().equals("EF")
                    || e.getMessage().equals("NF")
                    || e.getMessage().equals("BC")
                    || e.getMessage().equals("T")) {
                throw e;
            } else {
                throw new DataAccessException("Server Error");
            }
        }
    }

    public String listGames(String token) throws DataAccessException {
        try {
            if (!authData.findKey(token)) {
                throw new DataAccessException("NA");
            }
            return new Gson().toJson(new GameList(gameData.getGamesList()));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("NA")) {
                throw e;
            }
            else {
                throw new DataAccessException("Server Error");
            }
        }
    }
}
