package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {

    private ArrayList<GameData> data;

    @Override
    public void deleteGames() {
        data.clear();
    }
}