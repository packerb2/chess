package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Random;

public class MemoryGameDAO implements GameDAO {

    public ArrayList<GameData> games = new ArrayList<>();
    public Integer id = 0;

    @Override
    public void deleteGames() {
        if (games != null) {
            games.clear();
        }
    }

    @Override
    public Integer createGame(String gameName) {
        Random random = new Random();
        id = random.nextInt(999999999 - 100000000) + 100000000;
        GameData game = new GameData(id, null, null, gameName, new ChessGame());
        games.add(game);
        return id;
    }

    @Override
    public GameData getGame(int gameID) {
        if (games.isEmpty()) {
            return null;
        }
        for (GameData game : games) {
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
            GameData gameUpdated = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
            games.add(gameUpdated);
        }
        else if (color == ChessGame.TeamColor.BLACK) {
            GameData gameUpdated = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
            games.add(gameUpdated);
        }
        games.remove(game);
    }

    @Override
    public ArrayList<GameData> getGamesList() {
        return games;
    }
}