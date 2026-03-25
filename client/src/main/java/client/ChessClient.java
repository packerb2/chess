package client;

import java.util.Arrays;
import java.util.Scanner;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.*;
//import client.websocket.NotificationHandler;
import server.ServerFacade;
//import client.websocket.WebSocketFacade;
import webSocketMessages.Notification;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String userName = null;
    private String password = null;
    private final ServerFacade server;
//    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) throws DataAccessException {
        server = new ServerFacade(serverUrl);
//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void run() {
        System.out.println(SET_TEXT_BOLD + " Welcome to the chess game. Login to start.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void notify(Notification notification) {
        System.out.println(SET_TEXT_COLOR_RED + notification.message());
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }


    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "createGame" -> createGame(params);
                case "listGames" -> listGames();
                case "joinGame" -> joinGame(params);
                case "clear" -> clear();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws DataAccessException {
        if (params.length == 3) {
            userName = params[0];
            password = params[1];
            String email = params[2];
            UserData userNew = new UserData(userName, password, email);
            server.register(userNew);
            server.login(userNew);
            state = State.SIGNEDIN;
            return String.format("You registered and logged in as %s.", userName);
        }
        throw new DataAccessException("Expected: <UserName, Password, Email>");
    }

    public String login(String... params) throws DataAccessException {
        if (params.length >= 1) {
            try {
                userName = params[0];
                password = params[1];
                UserData userNew = new UserData(userName, password, null);
                server.login(userNew);
                state = State.SIGNEDIN;
                return String.format("You logged in as %s.", userName);
            } catch (DataAccessException e) {
                throw new DataAccessException("could not login");
            }
        }
        throw new DataAccessException("Expected: <UserName, Password>");
    }

    public String logout() throws DataAccessException {
        assertSignedIn();
        server.logout();
        state = State.SIGNEDOUT;
        return "You logged out";
    }

    public String createGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length == 1) {
            var result = new StringBuilder();
            var gson = new Gson();
            GameIDs id = server.createGame(params[0]);
            result.append(gson.toJson(id));
            return result.toString();
        }
        throw new DataAccessException("Expected: <GameName>");
    }

    public String listGames() throws DataAccessException {
        assertSignedIn();
        GameList gamesList = server.listGames();
        var result = new StringBuilder();
        var gson = new Gson();
        for (GameData game : gamesList.games) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String joinGame(String... params) throws DataAccessException {
        assertSignedIn();
        var request = new StringBuilder();
        var gson = new Gson();
        if (params.length == 2) {
            GameIDs id = new GameIDs(Integer.parseInt(params[0]));
            ChessGame.TeamColor color = null;
            if (params[1].equals("white")) {
                color = ChessGame.TeamColor.WHITE;
            }
            else if (params[1].equals("black")) {
                color = ChessGame.TeamColor.BLACK;
            }
            else {
                throw new DataAccessException("color is not correct");
            }
            request.append(gson.toJson(id)).append(gson.toJson(color));
            server.joinGame(request.toString());
            return String.format("You joined game %s as %s.", id, color);
        }
        throw new DataAccessException("Expected: <GameID> <TeamColor>");
    }

    public String clear() throws DataAccessException {
        assertSignedIn();
        server.clear();
        return "System has been cleared.";
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - register <UserName> <Password> <Email>
                    - login <UserName> <Password>
                    - quit
                    """;
        }
        return """
                - createGame <GameName>
                - listGames
                - joinGame <GameID> <TeamColor>
                - logout
                - clear
                - quit
                """;
    }

    private void assertSignedIn() throws DataAccessException {
        if (state == State.SIGNEDOUT) {
            throw new DataAccessException("You must log in");
        }
    }
}
