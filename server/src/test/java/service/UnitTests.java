package service;
import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.LoginReturn;
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
    private static UserData badUser;

    @BeforeAll
    public static void setUpAll() {
        testUserDB = new MemoryUserDAO();
        testGameDB = new MemoryGameDAO();
        testAuthDB = new MemoryAuthDAO();
        testService = new Service(testUserDB, testGameDB, testAuthDB);
        p1 = new UserData("p1", "pass", "1@email");
        p2 = new UserData("p2", "passw", "2@email");
        p3 = new UserData("p3", "password", "3@email");
        badUser = new UserData(null, null, null);
    }

    @BeforeEach
    public void setUp() {
        testService.clear();
        try {
            testService.register(p1);
            testService.register(p2);
            testService.register(p3);
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "register threw an error");
        }
    }

    @Test
    public void registerWorksTest() {
        Assertions.assertSame(testUserDB.getUser(p1), p1);
        Assertions.assertSame(testUserDB.getUser(p2), p2);
        Assertions.assertSame(testUserDB.getUser(p3), p3);
    }

    @Test
    public void registerRaisesErrorsTest() {
        Assertions.assertThrows(DataAccessException.class, () -> testService.register(badUser));
    }

    @Test
    public void clearWorksTest() {
        Assertions.assertSame(testUserDB.getUser(p1), p1);
        Assertions.assertSame(testUserDB.getUser(p2), p2);
        Assertions.assertSame(testUserDB.getUser(p3), p3);
        testService.clear();
        Assertions.assertNotSame(testUserDB.getUser(p1), p1);
        Assertions.assertNotSame(testUserDB.getUser(p2), p2);
        Assertions.assertNotSame(testUserDB.getUser(p3), p3);
    }

    @Test
    public void loginWorksTest() {
        try {
            String resultString = testService.login(p1);
            String expectedString = new Gson().toJson(new LoginReturn(p1.username(), "111-aaa-xyz"));
            LoginReturn result = new Gson().fromJson(resultString, LoginReturn.class);
            LoginReturn expected = new Gson().fromJson(expectedString, LoginReturn.class);
            Assertions.assertEquals(result.username, expected.username);
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "login threw an error in logout test");
        }
    }

    @Test
    public void loginWorksExceptionTest() {
            Assertions.assertThrows(DataAccessException.class, () -> testService.login(badUser));
    }

    @Test
    public void logoutWorksTest() {
        try {
            String resultString = testService.login(p1);
            LoginReturn result = new Gson().fromJson(resultString, LoginReturn.class);
            Assertions.assertDoesNotThrow(() -> testService.logout(result.authToken));
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "login threw an error in logout test");
        }
    }

    @Test
    public void logoutWorksExceptionTest() {
        try {
            testService.login(p1);
            Assertions.assertThrows(DataAccessException.class, () -> testService.logout("ooops"));
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "login threw an error in logout test");
        }

    }

    @Test
    public void createWorksTest() {
        try {
            String logString = testService.login(p1);
            LoginReturn logS = new Gson().fromJson(logString, LoginReturn.class);
            Assertions.assertDoesNotThrow(() -> testService.createGame("name", logS.authToken));
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "create threw an error in create test");
        }
    }

    @Test
    public void createWorksExceptionTest() {
        Assertions.assertThrows(DataAccessException.class, () -> testService.createGame("n", "n"));
    }

    @Test
    public void listGamesWorksTest() {
        try {
            String logString = testService.login(p1);
            LoginReturn logS = new Gson().fromJson(logString, LoginReturn.class);
            testService.createGame("game1", logS.authToken);
            testService.createGame("game2", logS.authToken);
            testService.createGame("game3", logS.authToken);
            Assertions.assertDoesNotThrow(() -> testService.listGames(logS.authToken));
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "login threw an error in list test");
        }
    }

    @Test
    public void listGamesWorksExceptionTest() {
        try {
            String logString = testService.login(p1);
            LoginReturn logS = new Gson().fromJson(logString, LoginReturn.class);
            testService.createGame("game1", logS.authToken);
            testService.createGame("game2", logS.authToken);
            testService.createGame("game3", logS.authToken);
            Assertions.assertThrows(DataAccessException.class, () -> testService.listGames("bad_token"));
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "login threw an error in logout test");
        }
    }

    @Test
    public void joinGamesWorksTest() {
        try {
            String logString = testService.login(p1);
            LoginReturn logS = new Gson().fromJson(logString, LoginReturn.class);
            testService.createGame("game1", logS.authToken);
            String games = testService.listGames(logS.authToken);
            Integer id = Integer.parseInt(games.substring(20, 29));
            Assertions.assertDoesNotThrow(() -> testService.joinGame(id, ChessGame.TeamColor.WHITE, logS.authToken));
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "login threw an error in list test");
        }
    }

    @Test
    public void joinGamesWorksExceptionTest() {
        try {
            String logString = testService.login(p1);
            LoginReturn logS = new Gson().fromJson(logString, LoginReturn.class);
            testService.createGame("game1", logS.authToken);
            Assertions.assertThrows(DataAccessException.class, () -> testService.joinGame(0, ChessGame.TeamColor.WHITE, "bad_token"));
        } catch (DataAccessException e) {
            Assertions.assertTrue(0 == 1, "login threw an error in logout test");
        }
    }
}
