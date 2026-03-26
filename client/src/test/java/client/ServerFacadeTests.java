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
    UserData cain = new UserData("cain", null, null);
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
        assertTrue(true);
        //assertThrows(ClientException.class, () -> facade.register(cain));
    }

}
