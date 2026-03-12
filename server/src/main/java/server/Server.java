package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;

import io.javalin.http.Context;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.*;
import service.*;


public class Server {

    private final Javalin javalin;
    private final Service service = new Service(new SQLUserDAO(), new SQLGameDAO(), new SQLAuthDAO());

    private void clear(Context context) {
        context.status(200);
        if (service.clear() == -1) {
            context.status(500);
        }
    }

    private void register(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);
            context.result(service.register(user));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("EF")) {
                context.status(400);
                context.result(new Gson().toJson(new ErrorObject("Error: Some Fields Are Empty")));
            } else if (e.getMessage().equals("Taken")) {
                context.status(403);
                context.result(new Gson().toJson(new ErrorObject("Error: username already exists")));
            }
            else {
                context.status(500);
                context.result(new Gson().toJson(new ErrorObject("Error: Server Error")));
            }
        }
    }

    private void login(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);
            context.status(200);
            context.result(service.login(user));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("EF")) {
                context.status(400);
                context.result(new Gson().toJson(new ErrorObject("Error: Credentials are Incorrect")));
            }
            else if (e.getMessage().equals("UE")) {
                context.status(401);
                context.result(new Gson().toJson(new ErrorObject("Error: Credentials are Incorrect")));
            } else {
                context.status(500);
                context.result(new Gson().toJson(new ErrorObject("Error: Server Error")));
            }
        }
    }

    private void logout(Context context) {
        try {
            String token = context.header("authorization");
            context.status(200);
            service.logout(token);
        } catch (DataAccessException e) {
            if (e.getMessage().equals("NA")) {
                context.status(401);
                context.result(new Gson().toJson(new ErrorObject("Error: Unauthorized")));
            }
            else {
                context.status(500);
                context.result(new Gson().toJson(new ErrorObject("Error: System Error")));
            }
        }
    }

    private void listGames(Context context) {
        try {
            String token = context.header("authorization");
            context.status(200);
            context.result(service.listGames(token));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(new Gson().toJson(new ErrorObject("Error: Unauthorized")));
        }
    }

    private void createGame(Context context) {
        try {
            GameName game = new Gson().fromJson(context.body(), GameName.class);
            String token = context.header("authorization");
            // authorization capitalized?
            // check that token exists in authData
            context.status(200);
            context.result(service.createGame(game.gameName, token));
        } catch (DataAccessException e) {
            if (e.getMessage().equals("EF")) {
                context.status(400);
                context.result(new Gson().toJson(new ErrorObject("Error: Missing Info")));
            }
            else if (e.getMessage().equals("NA")) {
                context.status(401);
                context.result(new Gson().toJson(new ErrorObject("Error: Not Authorized")));
            } else {
                context.status(500);
                context.result(new Gson().toJson(new ErrorObject("Error: System Error")));
            }
        }
    }

    private void joinGame(Context context) {
        try {
            JoinGameData setUpInfo = new Gson().fromJson(context.body(), JoinGameData.class);
            String token = context.header("authorization");
            context.status(200);
            service.joinGame(setUpInfo.gameID(), setUpInfo.playerColor(), token);
        } catch (DataAccessException e) {
            switch (e.getMessage()) {
                case "EF" -> {
                    context.status(400);
                    context.result(new Gson().toJson(new ErrorObject("Error: Some Fields Were Empty")));
                }
                case "Not Found" -> {
                    context.status(400);
                    context.result(new Gson().toJson(new ErrorObject("Error: Invalid Game ID")));
                }
                case "Bad Color" -> {
                    context.status(403);
                    context.result(new Gson().toJson(new ErrorObject("Error: That Color Is Not A Valid Option")));
                }
                case "Taken" -> {
                    context.status(403);
                    context.result(new Gson().toJson(new ErrorObject("Error: Color Has Already Been Taken")));
                }
                default -> {
                    context.status(500);
                    context.result(new Gson().toJson(new ErrorObject("Error: Unauthorized")));
                }
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
