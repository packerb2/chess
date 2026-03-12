package dataaccess;

import com.google.gson.Gson;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO{

    public SQLUserDAO() {
        try {new SQLDataAccess();} catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(UserData info) {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, info.username());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String gotUsername = rs.getString("username");
                        String gotPassword = rs.getString("password");
                        String gotEmail = rs.getString("email");
                        if (BCrypt.checkpw(info.password(), gotPassword)) {
                            return new UserData(gotUsername, gotPassword, gotEmail);
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
    public void addUser(UserData info) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String hashedPassword = BCrypt.hashpw(info.password(), BCrypt.gensalt());
            var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, info.username());
                ps.setString(2, hashedPassword);
                ps.setString(3, info.email());
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            return;
        }
    }

    @Override
    public void deleteUsers() {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "TRUNCATE users";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            return;
        }
    }
}
