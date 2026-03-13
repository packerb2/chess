package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.GameIDs;
import model.LoginReturn;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import service.Service;

public class unitTestsSQL {

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
        testUserDB = new SQLUserDAO();
        testGameDB = new SQLGameDAO();
        testAuthDB = new SQLAuthDAO();
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
        Assertions.assertEquals(testUserDB.getUser(p1).username(), p1.username());
        Assertions.assertTrue(BCrypt.checkpw(p1.password(), testUserDB.getUser(p1).password()));
        Assertions.assertEquals(testUserDB.getUser(p1).email(), p1.email());
        Assertions.assertEquals(testUserDB.getUser(p2).username(), p2.username());
        Assertions.assertTrue(BCrypt.checkpw(p2.password(), testUserDB.getUser(p2).password()));
        Assertions.assertEquals(testUserDB.getUser(p2).email(), p2.email());
        Assertions.assertEquals(testUserDB.getUser(p3).username(), p3.username());
        Assertions.assertTrue(BCrypt.checkpw(p3.password(), testUserDB.getUser(p3).password()));
        Assertions.assertEquals(testUserDB.getUser(p3).email(), p3.email());
    }

    @Test
    public void registerRaisesErrorsTest() {
        Assertions.assertThrows(DataAccessException.class, () -> testService.register(badUser));
    }

    @Test
    public void clearWorksTest() {
        Assertions.assertEquals(testUserDB.getUser(p1).username(), p1.username());
        Assertions.assertEquals(testUserDB.getUser(p2).username(), p2.username());
        Assertions.assertEquals(testUserDB.getUser(p3).username(), p3.username());
        testService.clear();
        Assertions.assertNull(testUserDB.getUser(p1));
        Assertions.assertNull(testUserDB.getUser(p2));
        Assertions.assertNull(testUserDB.getUser(p3));
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
            String idString = testService.createGame("game1", logS.authToken);
            GameIDs idObj = new Gson().fromJson(idString, GameIDs.class);
            Integer id = idObj.gameID;
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
