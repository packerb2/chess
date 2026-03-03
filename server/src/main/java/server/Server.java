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
import model.JoinGameData;
import model.UserData;
import service.*;


public class Server {

    private final Javalin javalin;
    private final Service service = new Service(new MemoryUserDAO(), new MemoryGameDAO(), new MemoryAuthDAO());
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
        context.status(200);
        service.clear();
    }

    private String register(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);
            return service.register(user);
        } catch (DataAccessException e) {
            context.status(401);
            return new Gson().toJson("~ error object ~");
        }
    }

    private String login(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);
            return service.login(user);
        } catch (DataAccessException e) {
            context.status(401);
            return new Gson().toJson("~ error object ~");
        }
    }

    private String logout(Context context) {
        try {
            if (!authorized(context)) {
                throw new DataAccessException("Error: Not Authorized");
            }
            AuthData authKey = new Gson().fromJson(context.body(), AuthData.class);
            return service.logout(authKey);
        } catch (DataAccessException e) {
            context.status(401);
            return new Gson().toJson("~ error object ~");
        }
    }

    private String listGames(Context context) {
        try {
            if (!authorized(context)) {
                throw new DataAccessException("Error: Not Authorized");
            }
            String token = context.header("Authorization");
            return service.listGames(token);
        } catch (DataAccessException e) {
            context.status(401);
            return new Gson().toJson("~ error object ~");
        }
    }

    private String createGame(Context context) {
        try {
            if (!authorized(context)) {
                throw new DataAccessException("Error: Not Authorized");
            }
            Map<String, String> gameName = new Gson().fromJson(context.body(), Map.class);
            String token = context.header("Authorization");
            // authorization capitalized?
            // check that token exists in authData
            return service.createGame(gameName.get("gameName"), token);
        } catch (DataAccessException e) {
            context.status(401);
            return new Gson().toJson("~ error object ~");
        }
    }

    private String joinGame(Context context) {
        try {
            if (!authorized(context)) {
                throw new DataAccessException("Error: Not Authorized");
            }
            JoinGameData setUpInfo = new Gson().fromJson(context.body(), JoinGameData.class);
            String token = context.header("Authorization");
            return service.joinGame(setUpInfo.gameID(), setUpInfo.color(), token, setUpInfo.user());
        } catch (DataAccessException e) {
            context.status(401);
            return new Gson().toJson("~ error object ~");
        }
    }

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"))

                // Register your endpoints and exception handlers here.
                .post("/user", this::register)
                .post("/session", this::login)
                .delete("/session", this::logout)
                .get("/game", this::listGames)
                .post("/game", this::createGame)
                .put("/game", this::joinGame)
                .delete("/db", this::clear);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
