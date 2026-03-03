package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    void deleteGames();
    Integer createGame(String gameName);
    GameData getGame(int gameID);
    void updatePlayer(int gameID, ChessGame.TeamColor color, String username);
    ArrayList<GameData> getGamesList();
}
