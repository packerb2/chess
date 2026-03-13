package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class SQLGameDAO implements GameDAO {

    public SQLGameDAO() {
        try {new SQLDataAccess();} catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteGames() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE games";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Could not delete games");
        }
    }

    @Override
    public Integer createGame(String gameName) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, null);
                ps.setString(2, null);
                ps.setString(3, gameName);
                ps.executeUpdate();
                var statement2 = "SELECT gameID FROM games WHERE gameName=?";
                try (PreparedStatement ps2 = conn.prepareStatement(statement2)) {
                    ps2.setString(1, gameName);
                    try (ResultSet rs = ps2.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("gameID");
                        }
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Error creating game");
        }
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM games WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("gameID");
                        String whiteUser = rs.getString("whiteUsername");
                        String blackUser = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        if (gameID == id) {
                            return new GameData(id, whiteUser, blackUser, gameName);
                        }
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("could not retrieve game");
        }
        return null;
    }

    @Override
    public void updatePlayer(int ID, ChessGame.TeamColor color, String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "";
            if (color == ChessGame.TeamColor.WHITE) {
                statement = "UPDATE games SET whiteUsername=? WHERE gameID=?";
            }
            else if (color == ChessGame.TeamColor.BLACK) {
                statement = "UPDATE games SET blackUsername=? WHERE gameID=?";
            }
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                ps.setInt(2, ID);
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("could not update player");
        }
    }

    @Override
    public ArrayList<GameData> getGamesList() throws DataAccessException {
        ArrayList<GameData> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM games";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("gameID");
                        String whiteUser = rs.getString("whiteUsername");
                        String blackUser = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        result.add(new GameData(id, whiteUser, blackUser, gameName));
                    }
                    return result;
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Error retrieving game list");
        }
    }
}
