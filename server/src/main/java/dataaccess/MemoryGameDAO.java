package dataaccess;

import chess.ChessGame;
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

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : data) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public void updatePlayer(int gameID, ChessGame.TeamColor color, String username) {
        GameData game = getGame(gameID);
        if (color == ChessGame.TeamColor.WHITE) {
            GameData gameUpdated = new GameData(gameID, username, null, game.gameName());
            data.add(gameUpdated);
        }
        else if (color == ChessGame.TeamColor.BLACK) {
            GameData gameUpdated = new GameData(gameID, null, username, game.gameName());
            data.add(gameUpdated);
        }
        data.remove(game);
    }
}