package client;

import chess.ChessGame;
import client.ClientException;
import client.ServerFacade;
import model.GameName;
import model.JoinGameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;


    UserData adam = new UserData("adam", "first", "email");
    UserData eve = new UserData("eve", "second", "other");
    UserData able = new UserData("able", "third", "thing");
    UserData cain1 = new UserData("cain", "bad", "wrong");
    UserData cain2 = new UserData("cain", "bad", null);
    UserData cain3 = new UserData("cain", null, null);
    UserData nullUser = new UserData(null, null, null);
    GameName friendly = new GameName("friendly");
    GameName competitive = new GameName("competitive");
    GameName oops = new GameName(null);

    @BeforeAll
    public static void init() throws ClientException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(String.format("http://localhost:%s", port));
    }

    @BeforeEach
    public void initialize() throws ClientException {
        facade.clear();
    }

    @AfterAll
    static void stopServer() throws ClientException {
        facade.clear();
        server.stop();
    }

    @Test
    public void registerWorkingTest() {
        assertDoesNotThrow(() -> facade.register(adam));
    }

    @Test
    public void registerBadRequestTest() {
        assertThrows(ClientException.class, () -> facade.register(cain2));
        assertThrows(ClientException.class, () -> facade.register(cain3));
        assertThrows(ClientException.class, () -> facade.register(nullUser));
    }

    @Test
    public void logoutWorkingTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.logout());
    }

    @Test
    public void loginWorkingTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.logout());
        assertDoesNotThrow(() -> facade.login(adam));
    }

    @Test
    public void loginNotExistTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.logout());
        assertThrows(ClientException.class, () -> facade.login(eve));
    }

    @Test
    public void loginIncompleteTest() {
        assertDoesNotThrow(() -> facade.register(cain1));
        assertDoesNotThrow(() -> facade.logout());
        assertThrows(ClientException.class, () -> facade.login(cain3));
    }

    @Test
    public void clearWorkingTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.clear());
        assertThrows(ClientException.class, () -> facade.login(adam));
    }

    @Test
    public void createTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.createGame(friendly));
    }

    @Test
    public void createIncompleteTest() {
        assertDoesNotThrow(() -> facade.register(able));
        assertThrows(ClientException.class, () -> facade.createGame(oops));
    }

    @Test
    public void playOneTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.createGame(friendly));
        JoinGameData f = new JoinGameData(1, ChessGame.TeamColor.WHITE, adam);
        assertDoesNotThrow(() -> facade.joinGame(f));
    }

    @Test
    public void playTwoTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.createGame(friendly));
        JoinGameData f = new JoinGameData(1, ChessGame.TeamColor.WHITE, adam);
        assertDoesNotThrow(() -> facade.joinGame(f));
        assertDoesNotThrow(() -> facade.logout());
        assertDoesNotThrow(() -> facade.register(eve));
        JoinGameData f2 = new JoinGameData(1, ChessGame.TeamColor.BLACK, eve);
        assertDoesNotThrow(() -> facade.joinGame(f2));
    }

    @Test
    public void playWasTakenTest() {
        assertDoesNotThrow(() -> facade.register(adam));
        assertDoesNotThrow(() -> facade.createGame(friendly));
        JoinGameData f = new JoinGameData(1, ChessGame.TeamColor.WHITE, adam);
        assertDoesNotThrow(() -> facade.joinGame(f));
        assertThrows(ClientException.class, () -> facade.joinGame(f));
        assertDoesNotThrow(() -> facade.logout());
        assertDoesNotThrow(() -> facade.register(eve));
        JoinGameData f2 = new JoinGameData(1, ChessGame.TeamColor.BLACK, eve);
        assertDoesNotThrow(() -> facade.joinGame(f2));
        assertDoesNotThrow(() -> facade.logout());
        assertDoesNotThrow(() -> facade.register(able));
        JoinGameData f3 = new JoinGameData(1, ChessGame.TeamColor.BLACK, able);
        assertThrows(ClientException.class, () -> facade.joinGame(f3));
    }

}
