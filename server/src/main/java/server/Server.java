package server;

import io.javalin.*;

import java.util.UUID;

public class Server {

    private final Javalin javalin;

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
