package client;

import client.ClientException;
import client.ServerFacade;
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

}
