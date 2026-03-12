package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public SQLAuthDAO() {
        try {new SQLDataAccess();} catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData addAuth(UserData info) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String token = generateToken();
            AuthData authKey = new AuthData(token, info.username());
            while (findKey(authKey.token())) {
                token = generateToken();
                authKey = new AuthData(token, info.username());
            }
            var statement = "INSERT INTO auths (token, username) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                ps.setString(2, info.username());
                ps.executeUpdate();
                return authKey;
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Could not add authentication");
        }
    }

    @Override
    public void deleteAuths() {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE auths";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException _) {}
    }

    @Override
    public void removeKey(String token) {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM auths WHERE token=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException _) {}
    }

    @Override
    public boolean findKey(String token) {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT token FROM auths WHERE token=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String gotToken = rs.getString("token");
                        if (Objects.equals(gotToken, token)) {
                            return true;
                        }
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            return false;
        }
        return false;
    }

    @Override
    public AuthData getKey(String token) {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT token, username FROM auths WHERE token=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String gotToken = rs.getString("token");
                        String gotUser = rs.getString("username");
                        AuthData a = new AuthData(gotToken, gotUser);
                        if (Objects.equals(gotToken, token)) {
                            return a;
                        }
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            return null;
        }
        return null;
    }
}
