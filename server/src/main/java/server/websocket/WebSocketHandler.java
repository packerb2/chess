package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import model.GameList;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import service.Service;

import java.io.IOException;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Service service;
    private GameData currentGame;
    private ChessGame.TeamColor color;
    private boolean observing = false;

    public WebSocketHandler(Service service) {
        this.service = service;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand action = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CONNECT -> connect(action.getAuthToken(), action.getGameID(), ctx.session);
                case LEAVE -> leave(action.getAuthToken(), ctx.session);
                case RESIGN -> resign(action.getAuthToken(), ctx.session);
                case MAKE_MOVE -> makeMove(action.getAuthToken(), action.getMove(), ctx.session);
            }
        } catch (IOException | DataAccessException | InvalidMoveException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String auth, Integer id, Session session) throws IOException, DataAccessException {
        //use auth to find player and color in game id
        GameData inGame = null;
        AuthData authData = service.authData().getKey(auth);
        if (authData == null) {
            var error = new Error("Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else {
            String user = authData.username();
            String games = service.listGames(auth);
            GameList gamesList = new Gson().fromJson(games, GameList.class);
            for (GameData game : gamesList.games) {
                if (Objects.equals(game.gameID(), id)) {
                    inGame = game;
                    if (game.whiteUsername().equals(user)) {
                        color = ChessGame.TeamColor.WHITE;
                    } else if (game.blackUsername().equals(user)) {
                        color = ChessGame.TeamColor.BLACK;
                    }
                }
            }

            if (inGame == null) {
                var error = new Error("Error: game was not found");
                session.getRemote().sendString(new Gson().toJson(error));
            } else {
                connections.add(session);
                currentGame = inGame;
                if (color.equals(null)) {
                    observing = true;
                }
                var loadGame = new LoadGame(currentGame);
                session.getRemote().sendString(new Gson().toJson(loadGame));
                var broadcast = String.format("%s joined game %d as %s", user, id, color);
                var notification = new Notification(broadcast);
                connections.broadcast(session, notification);
            }
        }
    }

    public void makeMove(String auth, ChessMove move, Session session) throws IOException, InvalidMoveException {
        AuthData authData = service.authData().getKey(auth);
        if (authData == null) {
            var error = new Error("Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else {
            if (!currentGame.game().playing) {
                var error = new Error("Error: this game is over");
                session.getRemote().sendString(new Gson().toJson(error));
            }
//            if (!currentGame.game().getTeamTurn().equals(color)) {
//                var error = new Error("Error: those ain't your pieces");
//                session.getRemote().sendString(new Gson().toJson(error));
//            }
            else if (observing) {
                var error = new Error("Error: you are an observer");
                session.getRemote().sendString(new Gson().toJson(error));
            }
            else {
                try {
                    currentGame.game().makeMove(move);
                    var loadGame = new LoadGame(currentGame);
                    session.getRemote().sendString(new Gson().toJson(loadGame));
                    connections.broadcast(session, loadGame);
                    var message = String.format("Move was made: %s", move);
                    var notification = new Notification(message);
                    connections.broadcast(session, notification);
                    if (!currentGame.game().playing) {
                        var endMessage = String.format("Checkmate. %s WINS", currentGame.game().getTeamTurn());
                        var finalNotification = new Notification(endMessage);
                        connections.broadcast(null, finalNotification);
                    }
                } catch (InvalidMoveException e) {
                    var error = new Error("Error: please enter a valid move");
                    session.getRemote().sendString(new Gson().toJson(error));
                }
            }
        }
    }

    public void leave(String auth, Session session) throws IOException {
        var message = String.format("placeholder_string %s", auth);
        var notification = new Notification(message);
        connections.broadcast(null, notification);
    }

    private void resign(String auth, Session session) throws IOException {
        AuthData authData = service.authData().getKey(auth);
        if (authData == null) {
            var error = new Error("Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else if (observing) {
            var error = new Error("Error: you are an observer");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else if (!currentGame.game().playing) {
            var error = new Error("Error: this game has already ended");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else {
            String user = authData.username();
            var message = String.format("%s has resigned.", user);
            var notification = new Notification(message);
            currentGame.game().endGame();
            connections.broadcast(null, notification);
            connections.remove(session);
        }
    }
}