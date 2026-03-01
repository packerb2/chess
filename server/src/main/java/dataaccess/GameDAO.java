package dataaccess;

public interface GameDAO {
    void deleteGames();
    Integer createGame(String gameName);
}
