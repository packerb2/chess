package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import io.javalin.*;

import io.javalin.http.Context;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.AuthData;
import model.UserData;
import service.*;


public class Server {

    private final Javalin javalin;
    private Service service = new Service(new MemoryUserDAO(), new MemoryGameDAO(), new MemoryAuthDAO());
    final private HashSet<String> validTokens = new HashSet<>(Set.of("secret1", "secret2"));

    private boolean authorized(Context ctx) {
        String authToken = ctx.header("authorization");
        if (!validTokens.contains(authToken)) {
            ctx.contentType("application/json");
            ctx.status(401);
            ctx.result(new Gson().toJson(Map.of("msg", "invalid authorization")));
            return false;
        }
        return true;
    }

    private void clear(Context context) {
        service.clear();
    }

    private void register(Context context) throws DataAccessException {
        UserData user = new Gson().fromJson(context.body(), UserData.class);
        service.register(user);
    }

    private void login(Context context) throws DataAccessException {
        UserData user = new Gson().fromJson(context.body(), UserData.class);
        service.login(user);
    }

    private void logout(Context context) throws DataAccessException {
        if (!authorized(context)) {
            throw new DataAccessException("Error: Not Authorized");
        }
        AuthData authKey = new Gson().fromJson(context.body(), AuthData.class);
        service.logout(authKey);
    }

    private void listGames(Context context) {
        if (authorized(context)) {
            return;
        }
        return;
    }

    private void createGame(Context context) {
        if (authorized(context)) {
            return;
        }
        return;
    }

    private void joinGame(Context context) {
        if (authorized(context)) {
            return;
        }
        return;
    }

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))

                // Register your endpoints and exception handlers here.
                .post("/name{name}", this::register)
                .post("/name{name}", this::login)
                .delete("/name{name}", this::logout)
                .get("/name{name}", this::listGames)
                .post("/name{name}", this::createGame)
                .put("/name{name}", this::joinGame)
                .delete("/name{name}", this::clear);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
