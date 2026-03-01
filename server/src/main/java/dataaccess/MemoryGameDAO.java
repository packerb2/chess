package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {

    private ArrayList<GameData> data;
    private Integer highestID = 0;

    @Override
    public void deleteGames() {
        data.clear();
    }

    @Override
    public Integer createGame(String gameName) {
        highestID = highestID + 1;
        GameData game = new GameData(highestID, null, null, gameName);
        data.add(game);
        return highestID;
    }
}