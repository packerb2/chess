package service;
import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;

import java.util.*;

public class UnitTests {

    private static Service testService;
    private static UserDAO testUserDB;
    private static GameDAO testGameDB;
    private static AuthDAO testAuthDB;
    private static UserData p1;
    private static UserData p2;
    private static UserData p3;

    @BeforeAll
    public static void setUpAll() {
        testUserDB = new MemoryUserDAO();
        testGameDB = new MemoryGameDAO();
        testAuthDB = new MemoryAuthDAO();
        testService = new Service(testUserDB, testGameDB, testAuthDB);
        p1 = new UserData("p1", "pass", "1@email");
        p2 = new UserData("p2", "passw", "2@email");
        p3 = new UserData("p3", "password", "3@email");
    }

    @Test
    public void clearTest() {
        try {
            testService.register(p1);
            testService.register(p2);
            testService.register(p3);
        } catch (DataAccessException e) {
            assert false;
        }
    }
}
