package client;

import java.util.Arrays;
//import java.util.Objects;
import java.util.Scanner;

import chess.ChessGame;
//import com.google.gson.Gson;
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
                case "rev" -> reverseBoard();
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
                throw new DataAccessException("Error: Could not login");
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
        if (params.length >= 1) {
            GameIDs id = server.createGame(new GameName(params[0]));
            return String.format("%s game created with id: %d", params[0], id.gameID);
        }
        throw new DataAccessException("Expected: <GameName>");
    }

    public String listGames() throws DataAccessException {
        assertSignedIn();
        GameList gamesList = server.listGames();
        if (gamesList.games.isEmpty()) {
            return "No games have been created...";
        }
        var result = new StringBuilder();
        result.append(String.format("%-15s %-10s %-20s %-20s\n",
                "Game Name", "Game ID", "White Player", "Black Player"));
        for (GameData game : gamesList.games) {
            String black = game.blackUsername();
            if (black == null) {
                black = "~empty~";
            }
            String white = game.whiteUsername();
            if (white == null) {
                white = "~empty~";
            }
            result.append(String.format("%-20s %-10d %-20s %-20s\n",
                    game.gameName(), game.gameID(), white, black));
        }
        return result.toString();
    }

    public String joinGame(String... params) throws DataAccessException {
        assertSignedIn();
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
                throw new DataAccessException("Error: Color is invalid. Please enter with 'white' or 'black'");
            }
            JoinGameData jd = new JoinGameData(id.gameID, color, new UserData(userName, password, null));
            ErrorObject error = server.joinGame(jd);
            if (error != null) {
                throw new DataAccessException(error.message);
            }
            return String.format("You joined game %s as %s.", id, color);
        }
        throw new DataAccessException("Expected: <GameID> <TeamColor>");
    }

    public String clear() throws DataAccessException {
        assertSignedIn();
        server.logout();
        state = State.SIGNEDOUT;
        server.clear();
        return "System has been cleared.";
    }

    public String board() {
        var board = new StringBuilder();
        var wArmy = whiteArmy();
        var middle = middle();
        var bArmy = blackArmy();
        var border = topBottomBorder();
        board.append(border);
        board.append(bArmy);
        board.append(middle);
        board.append(wArmy);
        board.append(border);
        return String.format("%s", board);
    }

    public String reverseBoard() {
        var board = new StringBuilder();
        var wArmy = whiteArmyReverse();
        var middle = middleRev();
        var bArmy = blackArmyRev();
        var border = topBottomBorderRev();
        board.append(border);
        board.append(wArmy);
        board.append(middle);
        board.append(bArmy);
        board.append(border);
        return String.format("%s", board);
    }

    public String whiteArmy() {
        var army = new StringBuilder();
        var wBackLine = new StringBuilder();
        var wPawnLine = whitePawns();
        wBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 1 ");
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_ROOK);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_KNIGHT);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_BISHOP);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_QUEEN);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_KING);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_BISHOP);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_KNIGHT);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_ROOK);
        wBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 1 ");
        wBackLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", wPawnLine)).append(String.format("%s", wBackLine));
        return String.format("%s", army);
    }

    public String whiteArmyReverse() {
        var army = new StringBuilder();
        var wBackLine = new StringBuilder();
        var wPawnLine = whitePawnsRev();
        wBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 1 ");
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_ROOK);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_KNIGHT);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_BISHOP);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_KING);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_QUEEN);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_BISHOP);
        wBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_KNIGHT);
        wBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_ROOK);
        wBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 1 ");
        wBackLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", wBackLine)).append(String.format("%s", wPawnLine));
        return String.format("%s", army);
    }

    public String whitePawns() {
        var army = new StringBuilder();
        var wPawnLine = new StringBuilder();
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", wPawnLine));
        return String.format("%s", army);
    }

    public String whitePawnsRev() {
        var army = new StringBuilder();
        var wPawnLine = new StringBuilder();
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", wPawnLine));
        return String.format("%s", army);
    }

    public String middle() {
        var field = new StringBuilder();
        var line3 = line3();
        var line4 = line4();
        var line5 = line5();
        var line6 = line6();
        field.append(String.format("%s%s%s%s", line3, line4, line5, line6));
        return String.format("%s", field);
    }

    public String middleRev() {
        var field = new StringBuilder();
        var line3 = line3Rev();
        var line4 = line4Rev();
        var line5 = line5Rev();
        var line6 = line6Rev();
        field.append(String.format("%s%s%s%s", line6, line5, line4, line3));
        return String.format("%s", field);
    }

    public String line3() {
        var line3 = new StringBuilder();
        line3.append(SET_BG_COLOR_WHITE + " 3 ");
        line3.append(order1());
        line3.append(SET_BG_COLOR_WHITE + " 3 ");
        line3.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line3);
    }

    public String line4() {
        var line4 = new StringBuilder();
        line4.append(SET_BG_COLOR_WHITE + " 4 ");
        line4.append(order2());
        line4.append(SET_BG_COLOR_WHITE + " 4 ");
        line4.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line4);
    }

    public String line5() {
        var line5 = new StringBuilder();
        line5.append(SET_BG_COLOR_WHITE + " 5 ");
        line5.append(order1());
        line5.append(SET_BG_COLOR_WHITE + " 5 ");
        line5.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line5);
    }

    public String line6() {
        var line6 = new StringBuilder();
        line6.append(SET_BG_COLOR_WHITE + " 6 ");
        line6.append(order2());
        line6.append(SET_BG_COLOR_WHITE + " 6 ");
        line6.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line6);
    }

    public String line3Rev() {
        var line3 = new StringBuilder();
        line3.append(SET_BG_COLOR_WHITE + " 3 ");
        line3.append(order2());
        line3.append(SET_BG_COLOR_WHITE + " 3 ");
        line3.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line3);
    }

    public String line4Rev() {
        var line4 = new StringBuilder();
        line4.append(SET_BG_COLOR_WHITE + " 4 ");
        line4.append(order1());
        line4.append(SET_BG_COLOR_WHITE + " 4 ");
        line4.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line4);
    }

    public String line5Rev() {
        var line5 = new StringBuilder();
        line5.append(SET_BG_COLOR_WHITE + " 5 ");
        line5.append(order2());
        line5.append(SET_BG_COLOR_WHITE + " 5 ");
        line5.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line5);
    }

    public String line6Rev() {
        var line6 = new StringBuilder();
        line6.append(SET_BG_COLOR_WHITE + " 6 ");
        line6.append(order1());
        line6.append(SET_BG_COLOR_WHITE + " 6 ");
        line6.append(RESET_BG_COLOR + "\n");
        return String.format("%s", line6);
    }

    public String order2() {
        var line1 = new StringBuilder();
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line1.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        return String.format("%s", line1);
    }

    public String order1() {
        var line2 = new StringBuilder();
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        line2.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        return String.format("%s", line2);
    }

    public String blackArmy() {
        var army = new StringBuilder();
        var BackLine = new StringBuilder();
        var PawnLine = blackPawns();
        BackLine.append(SET_BG_COLOR_WHITE + " 8 ");
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_KING);
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_QUEEN);
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        BackLine.append(SET_BG_COLOR_WHITE + " 8 ");
        BackLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", BackLine)).append(String.format("%s", PawnLine));
        return String.format("%s", army);
    }

    public String blackArmyRev() {
        var army = new StringBuilder();
        var BackLine = new StringBuilder();
        var PawnLine = blackPawnsRev();
        BackLine.append(SET_BG_COLOR_WHITE + " 8 ");
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_QUEEN);
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_KING);
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        BackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        BackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        BackLine.append(SET_BG_COLOR_WHITE + " 8 ");
        BackLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", PawnLine)).append(String.format("%s", BackLine));
        return String.format("%s", army);
    }

    public String blackPawns() {
        var army = new StringBuilder();
        var PawnLine = new StringBuilder();
        PawnLine.append(SET_BG_COLOR_WHITE + " 7 ");
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_WHITE + " 7 ");
        PawnLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", PawnLine));
        return String.format("%s", army);
    }

    public String blackPawnsRev() {
        var army = new StringBuilder();
        var PawnLine = new StringBuilder();
        PawnLine.append(SET_BG_COLOR_WHITE + " 7 ");
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        PawnLine.append(SET_BG_COLOR_WHITE + " 7 ");
        PawnLine.append(RESET_BG_COLOR + "\n");
        army.append(String.format("%s", PawnLine));
        return String.format("%s", army);
    }

    public String topBottomBorder() {
        var border = new StringBuilder();
        border.append(SET_BG_COLOR_WHITE + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " a ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  b ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " c ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  d ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " e ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  f ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " g ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  h ");
        border.append(SET_BG_COLOR_WHITE + EMPTY);
        border.append(RESET_BG_COLOR + "\n");
        return String.format("%s", border);
    }

    public String topBottomBorderRev() {
        var border = new StringBuilder();
        border.append(SET_BG_COLOR_WHITE + EMPTY);
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " h ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  g ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " f ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  e ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " d ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  c ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " b ");
        border.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + "  a ");
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
                - rev
                - quit
                """;
    }

    private void assertSignedIn() throws DataAccessException {
        if (state == State.SIGNEDOUT) {
            throw new DataAccessException("You must log in");
        }
    }
}
