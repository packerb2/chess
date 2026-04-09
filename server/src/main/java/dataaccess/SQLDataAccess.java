package dataaccess;

import model.*;

import java.sql.*;

public class SQLDataAccess{

    public SQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS users (
            `username` varchar(255) NOT NULL,
            `password` varchar(255) NOT NULL,
            `email` varchar(255) NOT NULL,
             PRIMARY KEY (`username`)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS auths (
            `token` varchar(255) NOT NULL,
            `username` varchar(255) NOT NULL,
             PRIMARY KEY (`token`)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS games (
            `gameID` int NOT NULL AUTO_INCREMENT,
            `whiteUsername` varchar(255),
            `blackUsername` varchar(255),
            `gameName` varchar(255) NOT NULL,
            `gameData` varchar(255) NOT NULL,
             PRIMARY KEY (`gameID`),
             INDEX (`gameName`)
        )
        """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
