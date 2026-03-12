package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    void deleteGames() throws DataAccessException;
    Integer createGame(String gameName) throws  DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void updatePlayer(int gameID, ChessGame.TeamColor color, String username) throws DataAccessException;
    ArrayList<GameData> getGamesList() throws DataAccessException;
}
