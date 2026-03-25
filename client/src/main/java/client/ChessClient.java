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
//import webSocketMessages.Notification;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String userName = null;
    private String password = null;
    private String auth = null;
    private final ServerFacade server;
//    private final WebSocketFacade ws;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
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
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "clear" -> clear();
                case "board" -> board();
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
            LoginReturn lr = server.register(userNew);
            auth = lr.authToken;
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
                LoginReturn lr = server.login(userNew);
                auth = lr.authToken;
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
        auth = null;
        return "You logged out";
    }

    public String createGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length >= 1) {
            GameIDs id = server.createGame(new GameName(params[0]));
            return String.format("%s game created with id: %d", params[0], id.gameID);
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
            ChessGame.TeamColor color;
            if (params[1].equals("white")) {
                color = ChessGame.TeamColor.WHITE;
            }
            else if (params[1].equals("black")) {
                color = ChessGame.TeamColor.BLACK;
            }
            else {
                throw new DataAccessException("color is not correct");
            }
            JoinGameData jd = new JoinGameData(id.gameID, color, new UserData(userName, password, null));
            request.append(gson.toJson(jd));
            server.joinGame(request.toString());
            return String.format("You joined game %s as %s.", id, color);
        }
        throw new DataAccessException("Expected: <GameID> <TeamColor>");
    }

    public String clear() throws DataAccessException {
        assertSignedIn();
        server.logout();
        state = State.SIGNEDOUT;
        auth = null;
        server.clear();
        return "System has been cleared.";
    }

    public String board() {
        var board = new StringBuilder();
        var wArmy = whiteArmy();
        var middle = middle();
        var border = whiteSideBorder();
        board.append(middle);
        board.append(wArmy);
        board.append(border);
        return String.format("%s", board);
    }

    public String whiteArmy() {
        var army = new StringBuilder();
        var wBackLine = new StringBuilder();
        var wPawnLine = new StringBuilder();
        wBackLine.append(SET_BG_COLOR_WHITE + EMPTY);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_ROOK);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_KNIGHT);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_BISHOP);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_QUEEN);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_KING);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_BISHOP);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_KNIGHT);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_ROOK);
        wBackLine.append(SET_BG_COLOR_WHITE + EMPTY);
        wBackLine.append(RESET_BG_COLOR + "\n");
        wPawnLine.append(SET_BG_COLOR_WHITE + EMPTY);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_WHITE + EMPTY);
        wPawnLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", wPawnLine)).append(String.format("%s", wBackLine));
        return String.format("%s", army);
    }

    public String middle() {
        var field = new StringBuilder();
        var line1 = new StringBuilder();
        var line2 = new StringBuilder();
        var pair = new StringBuilder();
        line1.append(SET_BG_COLOR_WHITE + EMPTY);
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line1.append(SET_BG_COLOR_WHITE + EMPTY);
        line1.append(RESET_BG_COLOR + "\n");
        line2.append(SET_BG_COLOR_WHITE + EMPTY);
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(SET_BG_COLOR_WHITE + EMPTY);
        line2.append(RESET_BG_COLOR + "\n");
//        pair.append(String.format("%s", line1)).append(String.format("%s", line2));
//        field.append(String.format("%s", pair)).append(String.format("%s", pair));
        field.append(String.format("%s%s%s%s", line2, line1, line2, line1));
        return String.format("%s", field);
    }

    public String whiteSideBorder() {
        var border = new StringBuilder();
        border.append(SET_BG_COLOR_WHITE + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + EMPTY);
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " a ");
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " b ");
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " c ");
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " d ");
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " e ");
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " f ");
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " g ");
//        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " h ");
        border.append(SET_BG_COLOR_WHITE + EMPTY);
        border.append(RESET_BG_COLOR + "\n");
        return String.format("%s", border);
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
                - board
                - quit
                """;
    }

    private void assertSignedIn() throws DataAccessException {
        if (state == State.SIGNEDOUT) {
            throw new DataAccessException("You must log in");
        }
    }
}
