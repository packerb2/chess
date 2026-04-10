package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.SQLGameDAO;
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
import service.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Service service;
    final SQLGameDAO gameDAO = new SQLGameDAO();
    Map<Integer, String> alphaBack = new HashMap<>();

    public WebSocketHandler(Service service) {
        this.service = service;
        alphaBack.put(1, "a");
        alphaBack.put(2, "b");
        alphaBack.put(3, "c");
        alphaBack.put(4, "d");
        alphaBack.put(5, "e");
        alphaBack.put(6, "f");
        alphaBack.put(7, "g");
        alphaBack.put(8, "h");
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
                case LEAVE -> leave(action.getAuthToken(), action.getGameID(), ctx.session);
                case RESIGN -> resign(action.getAuthToken(), action.getGameID(), ctx.session);
                case MAKE_MOVE -> makeMove(action.getAuthToken(), action.getGameID(), action.getMove(), ctx.session);
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
            ChessGame.TeamColor color = null;
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
                connections.add(session, id);
                var loadGame = new LoadGame(inGame);
                session.getRemote().sendString(new Gson().toJson(loadGame));
                var broadcast = String.format("%s joined game %d as %s", user, id, color);
                if (color == null) {
                    broadcast = String.format("%s joined game %d as an observer", user, id);
                }
                var notification = new Notification(broadcast);
                connections.broadcast(session, notification, id);
            }
        }
    }

    private String moveResponse(ChessMove move) {
        Integer startRow = move.getStartPosition().getRow();
        Integer startCol = move.getEndPosition().getColumn();
        Integer endRow = move.getEndPosition().getRow();
        Integer endCol = move.getEndPosition().getColumn();
        String startChar = alphaBack.get(startCol);
        String endChar = alphaBack.get(endCol);
        return String.format("%d%s to %d%s", startRow, startChar, endRow, endChar);
    }

    public void makeMove(String auth, Integer id, ChessMove move, Session session) throws IOException, InvalidMoveException {
        AuthData authData = service.authData().getKey(auth);
        if (authData == null) {
            var error = new Error("Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else {
            try {
                String user = authData.username();
                String opponent;
                GameData game = service.movePiece(id, move, auth);
                if (game.whiteUsername().equals(user)) {
                    opponent = game.blackUsername();
                }
                else {opponent = game.whiteUsername();}
                var loadGame = new LoadGame(game);
                session.getRemote().sendString(new Gson().toJson(loadGame));
                connections.broadcast(session, loadGame, id);
                String moveString = moveResponse(move);
                var message = String.format("%s moved a move: %s", user, moveString);
                var notification = new Notification(message);
                connections.broadcast(session, notification, id);
                if (!game.game().playing && (game.game().whiteCheck || game.game().blackCheck)) {
                    var checkmate = String.format("Game Over. %s is in Checkmate. %s WINS", opponent, user);
                    var checkmateNotification = new Notification(checkmate);
                    connections.broadcast(null, checkmateNotification, id);
                }
                if (game.game().playing && (game.game().whiteCheck || game.game().blackCheck)) {
                    var check = String.format("%s is in Check", opponent);
                    var checkNotification = new Notification(check);
                    connections.broadcast(null, checkNotification, id);
                }
                if (!game.game().playing && !game.game().whiteCheck && !game.game().blackCheck) {
                    var stale = "Game Over. Stalemate.";
                    var stalemateNotification = new Notification(stale);
                    connections.broadcast(null, stalemateNotification, id);
                }
            } catch (DataAccessException e) {
                if (e.getMessage().equals("GE")) {
                    var error = new Error("Error: this game has ended.");
                    session.getRemote().sendString(new Gson().toJson(error));
                }
                if (e.getMessage().equals("NYP")) {
                    var error = new Error("Error: It is not your turn.");
                    session.getRemote().sendString(new Gson().toJson(error));
                }
                if (e.getMessage().equals("IM")) {
                    var error = new Error("Error: that is not a valid move.");
                    session.getRemote().sendString(new Gson().toJson(error));
                }
                if (e.getMessage().equals("P")) {
                    var error = new Error("Error: Please include a valid promotion.");
                    session.getRemote().sendString(new Gson().toJson(error));
                }
                if (e.getMessage().equals("DNP")) {
                    var error = new Error("Error: This piece cannot promote.");
                    session.getRemote().sendString(new Gson().toJson(error));
                }
            }
        }
    }

    public void leave(String auth, Integer id, Session session) throws IOException, DataAccessException {
        AuthData authData = service.authData().getKey(auth);
        if (authData == null) {
            var error = new Error("Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else {
            String user = authData.username();
            String games = service.listGames(auth);
            GameList gamesList = new Gson().fromJson(games, GameList.class);
            ChessGame.TeamColor color = null;
            for (GameData game : gamesList.games) {
                if (Objects.equals(game.gameID(), id)) {
                    if (game.whiteUsername() != null && game.whiteUsername().equals(user)) {
                        color = ChessGame.TeamColor.WHITE;
                    } else if (game.blackUsername() != null && game.blackUsername().equals(user)) {
                        color = ChessGame.TeamColor.BLACK;
                    }
                }
            }
            var message = String.format("%s is no longer observing the game.", user);
            if (color != null) {
                service.removePlayer(id, color, auth);
                message = String.format("%s has left the game. Waiting for new player...", user);
            }
            var notification = new Notification(message);
            connections.broadcast(session, notification, id);
            connections.remove(session, id);
        }
    }

    private void resign(String auth, Integer id, Session session) throws IOException, DataAccessException {
        AuthData authData = service.authData().getKey(auth);
        if (authData == null) {
            var error = new Error("Error: unauthorized");
            session.getRemote().sendString(new Gson().toJson(error));
        }
        else {
            try {
                String user = authData.username();
                service.surrender(id, user, auth);
                var message = String.format("%s has resigned.", user);
                var notification = new Notification(message);
                connections.broadcast(null, notification, id);
                connections.remove(session, id);
            } catch (DataAccessException e) {
                Error error;
                if (e.getMessage().equals("OE")) {
                    error = new Error("Error: observer cannot resign.");
                }
                else if (e.getMessage().equals("GE")) {
                    error = new Error("Error: Game has already ended.");
                }
                else {
                    error = new Error("Error: system error while resigning.");
                }
                session.getRemote().sendString(new Gson().toJson(error));
            }
        }
    }
}