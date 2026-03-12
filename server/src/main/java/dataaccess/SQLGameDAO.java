package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

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
    public void deleteGames() {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE games";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException _) {}
    }

    @Override
    public Integer createGame(String gameName) {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, null);
                ps.setString(2, null);
                ps.setString(3, gameName);
                return ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException _) {
            return -1;
        }
    }

    @Override
    public GameData getGame(int gameID) {
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
            return null;
        }
        return null;
    }

    @Override
    public void updatePlayer(int gameID, ChessGame.TeamColor color, String username) {

    }

    @Override
    public ArrayList<GameData> getGamesList() {
        ArrayList<GameData> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM games WHERE gameID=?";
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
            return null;
        }
    }
}
