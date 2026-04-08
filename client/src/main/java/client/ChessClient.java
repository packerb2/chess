package client;

import java.util.*;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import com.google.gson.Gson;
import model.*;
import websocket.messages.ServerMessage;

import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {
    private String userName = null;
    private String password = null;
    private String authToken = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    Map<Integer, Integer> ids = new HashMap<>();
    private final WebSocketFacade ws;
    private Integer playing;

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, this);
        playing = null;
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
                case "quit" -> "quit";
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "play" -> joinGame(params);
                case "observe" -> observe(params);
                case "clear" -> clear();
                case "move" -> move(params);
//                case "leave" -> leave();
//                case "resign" -> resign();
//                case "highlight" -> highlight();
//                case "redraw" -> redraw();
                default -> help();
            };
        } catch (Exception ex) {return ex.getMessage();}
    }

    public String register(String... params) throws ClientException {
        if (params.length == 3) {
            userName = params[0];
            password = params[1];
            String email = params[2];
            UserData userNew = new UserData(userName, password, email);
            server.register(userNew);
            authToken = server.login(userNew);
            state = State.SIGNEDIN;
            return String.format("You registered and logged in as %s.", userName);
        }
        throw new ClientException("Expected: <UserName, Password, Email>");
    }

    public String login(String... params) throws ClientException {
        if (params.length == 2) {
            try {
                userName = params[0];
                password = params[1];
                UserData userNew = new UserData(userName, password, null);
                authToken = server.login(userNew);
                state = State.SIGNEDIN;
                return String.format("You logged in as %s.", userName);
            } catch (ClientException e) {throw new ClientException("Error: Could not login");}
        }
        throw new ClientException("Expected: <UserName, Password>");
    }

    public String logout() throws ClientException {
        assertSignedIn();
        server.logout();
        authToken = null;
        state = State.SIGNEDOUT;
        return "You logged out";
    }

    public String createGame(String... params) throws ClientException {
        assertSignedIn();
        if (params.length >= 1) {
            server.createGame(new GameName(params[0]));
            return String.format("Game '%s' has been created", params[0]);
        }
        throw new ClientException("Expected: <GameName>");
    }

    public String listGames() throws ClientException {
        assertSignedIn();
        GameList gamesList = server.listGames();
        if (gamesList.games.isEmpty()) {return "No games have been created...";}
        var result = new StringBuilder();
        result.append(String.format("%-10s %-15s %-20s %-20s\n", "Number", "Name", "White Player", "Black Player"));
        int i = 0;
        for (GameData game : gamesList.games) {
            i += 1;
            ids.put(i, game.gameID());
            String black = game.blackUsername();
            if (black == null) {black = "~empty~";}
            String white = game.whiteUsername();
            if (white == null) {white = "~empty~";}
            result.append(String.format("%-10d %-15s %-20s %-20s\n", i, game.gameName(), white, black));
        }
        return result.toString();
    }

    public String joinGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length == 2) {
            int num;
            try {
                num = Integer.parseInt(params[0]);
            } catch (Exception e) {
                throw new ClientException("Error: please use Game Number");
            }
            listGames();
            var id = ids.get(num);
            if (id == null) {
                throw new ClientException("Error: Game Number was not found");
            }
            ChessGame.TeamColor color;
            if (params[1].equals("white")) {
                color = ChessGame.TeamColor.WHITE;
            }
            else if (params[1].equals("black")) {
                color = ChessGame.TeamColor.BLACK;
            }
            else {
                throw new ClientException("Error: Color is invalid. Please use 'white' or 'black'");
            }
            JoinGameData jd = new JoinGameData(id, color, new UserData(userName, password, null));
            ErrorObject error = server.joinGame(jd);
            if (error != null) {
                throw new ClientException(error.message);
            }
            ws.connectToGame(authToken, id);
            playing = id;
            if (color == ChessGame.TeamColor.WHITE) {
                return String.format("You joined game %s as %s.\n%s", id, color, board());
            } else {return String.format("You joined game %s as %s.\n%s", id, color, reverseBoard());}
        }
        throw new ClientException("Expected: <GameNumber> <TeamColor>");
    }

    public void move(String... params) throws Exception {
        assertSignedIn();
        assertPlaying();
        if (params.length < 1) {
            throw new ClientException("Please try again and enter the desired move");
        }
        Integer id = playing;
        ChessMove moveRequest = new Gson().fromJson(params[0], ChessMove.class);
        GameList gamesList = server.listGames();
        for (GameData game : gamesList.games) {
            if (Objects.equals(game.gameID(), id)) {
                if (!game.game().playing) {
                    throw new ClientException("This game has been finished.");
                }
                ws.movePiece(authToken, id, moveRequest);
                try {
                    game.game().makeMove(moveRequest);
                } catch (InvalidMoveException e) {
                    throw new ClientException("Not a valid move");
                }
                if (!game.game().playing) {
                    throw new ClientException("Game Over.");
                }
            }
        }
        // find game with matching id
        // get the game and have it make a move.
        // update the found game with the new moved game
        // call the websocket
            // ^use as guideline for updating database for leave^
        // add a "finished" boolean to the games. set to false when resign is called. add a check to each related game call to see if game is still being played
    }

    public String observe(String... params) throws ClientException {
        assertSignedIn();
        GameList gamesList = server.listGames();
        if (gamesList.games.isEmpty()) {return "No games have been created...";}
        int num;
        try {
            num = Integer.parseInt(params[0]);
        } catch (Exception e) {throw new ClientException("Error: please use Game Number");}
        var result = new StringBuilder();
        for (GameData game : gamesList.games) {
            if (game.gameID() == num) {
                String black = game.blackUsername();
                if (black == null) {
                    black = "~empty~";
                }
                String white = game.whiteUsername();
                if (white == null) {
                    white = "~empty~";
                }
                result.append(String.format("%20s\n%s" + SET_TEXT_COLOR_BLUE + "%20s\n", black, board(), white));
                return result.toString();
            }
        }
        return String.format("Error: Game Number: %d does not exist", num);
    }

    public String clear() throws ClientException {
        if (state == State.SIGNEDIN) {
            server.logout();
            state = State.SIGNEDOUT;
        }
        server.clear();
        return "System has been cleared.";
    }

    public String board() {
        var board = new StringBuilder();
        var wArmy = whiteArmy();
        var middle = middle();
        var bArmy = blackArmy();
        var border = topBottomBorder();
        board.append(border).append(bArmy).append(middle).append(wArmy).append(border);
        return String.format("%s", board);
    }

    public String reverseBoard() {
        var board = new StringBuilder();
        var wArmy = whiteArmyReverse();
        var middle = middleRev();
        var bArmy = blackArmyRev();
        var border = topBottomBorderRev();
        board.append(border).append(wArmy).append(middle).append(bArmy).append(border);
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
        wBackLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
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
        wBackLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        army.append(String.format("%s", wBackLine)).append(String.format("%s", wPawnLine));
        return String.format("%s", army);
    }

    public String whitePawns() {
        var army = new StringBuilder();
        var wPawnLine = new StringBuilder();
        var lp = new StringBuilder();
        var dp = new StringBuilder();
        dp.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        lp.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(String.format("%s%s%s%s%s%s%s%s", lp, dp, lp, dp, lp, dp, lp, dp));
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        army.append(String.format("%s", wPawnLine));
        return String.format("%s", army);
    }

    public String whitePawnsRev() {
        var army = new StringBuilder();
        var wPawnLine = new StringBuilder();
        var lp = new StringBuilder();
        var dp = new StringBuilder();
        dp.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        lp.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE + WHITE_PAWN);
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(String.format("%s%s%s%s%s%s%s%s", dp, lp, dp, lp, dp, lp, dp, lp));
        wPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 2 ");
        wPawnLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        army.append(String.format("%s", wPawnLine));
        return String.format("%s", army);
    }

    public String middle() {
        var field = new StringBuilder();
        field.append(String.format("%s%s%s%s", line3(), line4(), line5(), line6()));
        return String.format("%s", field);
    }

    public String middleRev() {
        var field = new StringBuilder();
        field.append(String.format("%s%s%s%s", line6Rev(), line5Rev(), line4Rev(), line3Rev()));
        return String.format("%s", field);
    }

    public String line3() {
        var line3 = new StringBuilder();
        line3.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 3 ").append(order1());
        line3.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 3 ");
        line3.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line3);
    }

    public String line4() {
        var line4 = new StringBuilder();
        line4.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 4 ").append(order2());
        line4.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 4 ");
        line4.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line4);
    }

    public String line5() {
        var line5 = new StringBuilder();
        line5.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 5 ").append(order1());
        line5.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 5 ");
        line5.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line5);
    }

    public String line6() {
        var line6 = new StringBuilder();
        line6.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 6 ").append(order2());
        line6.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 6 ");
        line6.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line6);
    }

    public String line3Rev() {
        var line3 = new StringBuilder();
        line3.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 3 ").append(order2());
        line3.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 3 ");
        line3.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line3);
    }

    public String line4Rev() {
        var line4 = new StringBuilder();
        line4.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 4 ").append(order1());
        line4.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 4 ");
        line4.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line4);
    }

    public String line5Rev() {
        var line5 = new StringBuilder();
        line5.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 5 ").append(order2());
        line5.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 5 ");
        line5.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line5);
    }

    public String line6Rev() {
        var line6 = new StringBuilder();
        line6.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 6 ").append(order1());
        line6.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 6 ");
        line6.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", line6);
    }

    public String order2() {
        var line1 = new StringBuilder();
        var ls = new StringBuilder();
        var ds = new StringBuilder();
        ls.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        ds.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line1.append(String.format("%s%s%s%s%s%s%s%s", ds, ls, ds, ls, ds, ls, ds, ls));
        return String.format("%s", line1);
    }

    public String order1() {
        var line2 = new StringBuilder();
        var ls = new StringBuilder();
        var ds = new StringBuilder();
        ls.append(SET_BG_COLOR_LIGHT_GREY + EMPTY);
        ds.append(SET_BG_COLOR_DARK_GREY + EMPTY);
        line2.append(String.format("%s%s%s%s%s%s%s%s", ls, ds, ls, ds, ls, ds, ls, ds));
        return String.format("%s", line2);
    }

    public String blackArmy() {
        var army = new StringBuilder();
        var bBackLine = new StringBuilder();
        var bPawnLine = blackPawns();
        bBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 8 ");
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_QUEEN);
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_KING);
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        bBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 8 ");
        bBackLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        army.append(String.format("%s", bBackLine)).append(String.format("%s", bPawnLine));
        return String.format("%s", army);
    }

    public String blackArmyRev() {
        var army = new StringBuilder();
        var bBackLine = new StringBuilder();
        var bPawnLine = blackPawnsRev();
        bBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 8 ");
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_KING);
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_QUEEN);
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_BISHOP);
        bBackLine.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_KNIGHT);
        bBackLine.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_ROOK);
        bBackLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 8 ");
        bBackLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        army.append(String.format("%s", bPawnLine)).append(String.format("%s", bBackLine));
        return String.format("%s", army);
    }

    public String blackPawns() {
        var army = new StringBuilder();
        var bPawnLine = new StringBuilder();
        var lp = new StringBuilder();
        var dp = new StringBuilder();
        lp.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        dp.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        bPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 7 ");
        bPawnLine.append(String.format("%s%s%s%s%s%s%s%s", dp, lp, dp, lp, dp, lp, dp, lp));
        bPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 7 ");
        bPawnLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        army.append(String.format("%s", bPawnLine));
        return String.format("%s", army);
    }

    public String blackPawnsRev() {
        var army = new StringBuilder();
        var bPawnLine = new StringBuilder();
        var lp = new StringBuilder();
        var dp = new StringBuilder();
        lp.append(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        dp.append(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_BLACK + BLACK_PAWN);
        bPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 7 ");
        bPawnLine.append(String.format("%s%s%s%s%s%s%s%s", lp, dp, lp, dp, lp, dp, lp, dp));
        bPawnLine.append(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " 7 ");
        bPawnLine.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        army.append(String.format("%s", bPawnLine));
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
        border.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
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
        border.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
        return String.format("%s", border);
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - register <UserName> <Password> <Email>
                    - login <UserName> <Password>
                    - help
                    - quit
                    """;
        }
        return """
                - create <GameName>
                - list
                - play <GameNumber> <TeamColor>
                - observe <GameNumber>
                - logout
                - help
                - quit
                """;
    }

    private void assertSignedIn() throws ClientException {
        if (state == State.SIGNEDOUT) {
            throw new ClientException("You must log in");
        }
    }

    private void assertPlaying() throws ClientException {
        if (playing == null) {
            throw new ClientException("You must be in a game");
        }
    }

    @Override
    public void notify(ServerMessage notification) {

    }
}