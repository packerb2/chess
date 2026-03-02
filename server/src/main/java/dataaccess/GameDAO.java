package dataaccess;

import model.GameData;

public interface GameDAO {
    void deleteGames();
    Integer createGame(String gameName);
    GameData getGame(String gameID);
}
