package model;

public class GameIDs {
    public int gameID;

    public GameIDs(int id) {
        gameID = id;
    }

    @Override
    public String toString() {
        return String.format("%d", gameID);
    }
}
