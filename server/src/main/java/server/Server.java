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
import model.ErrorObject;
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

    private void register(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);
            context.result(service.register(user));
        } catch (DataAccessException e) {
            context.status(401);
            context.result(new Gson().toJson(new ErrorObject("Error: username already exists")));
        }
    }
//TODO change returns to be inside a context.result(~__~) and change function return types to void

    private void login(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);
            context.status(200);
            context.result(service.login(user));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("UE")) {
                context.status(401);
                context.result(new Gson().toJson(new ErrorObject("Error: Credentials are Incorrect (Username)")));
            } else {
                context.status(401);
                context.result(new Gson().toJson(new ErrorObject("Error: Credentials are Incorrect (Password)")));
            }
        }
    }

    private void logout(Context context) {
        try {
            AuthData authKey = new Gson().fromJson(context.body(), AuthData.class);
            context.status(200);
            context.result(service.logout(authKey));
        } catch (DataAccessException e) {
            context.status(401);
            context.result(new Gson().toJson(new ErrorObject("Error: Unauthorized")));
        }
    }

    private void listGames(Context context) {
        try {
            String token = context.header("authorization");
            context.status(200);
            context.result(service.listGames(token));
        } catch (DataAccessException e) {
            context.status(401);
            context.result(new Gson().toJson(new ErrorObject("Error: Unauthorized")));
        }
    }

    private void createGame(Context context) {
        try {
            Map<String, String> gameName = new Gson().fromJson(context.body(), Map.class);
            String token = context.header("authorization");
            // authorization capitalized?
            // check that token exists in authData
            context.status(200);
            context.result(service.createGame(gameName.get("gameName"), token));
        } catch (DataAccessException e) {
            context.status(401);
            context.result(new Gson().toJson(new ErrorObject("Error: Unauthorized")));
        }
    }

    private void joinGame(Context context) {
        try {
            JoinGameData setUpInfo = new Gson().fromJson(context.body(), JoinGameData.class);
            String token = context.header("authorization");
            context.status(200);
            context.result(service.joinGame(setUpInfo.gameID(), setUpInfo.color(), token, setUpInfo.user()));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("Not Found")) {
                context.status(500);
                context.result(new Gson().toJson(new ErrorObject("Error: Invalid Game ID")));
            }
            else if (e.getMessage().equals("Taken")) {
                context.status(403);
                context.result(new Gson().toJson(new ErrorObject("Error: Color Has Already Been Taken")));
            }
            else {
                context.status(401);
                context.result(new Gson().toJson(new ErrorObject("Error: Unauthorized")));
            }
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
