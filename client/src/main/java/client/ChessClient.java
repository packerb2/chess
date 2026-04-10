package client;

import java.util.*;

import chess.*;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
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
    private ChessGame.TeamColor color;
    Map<String, Integer> alphaOrder = new HashMap<>();

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, this);
        playing = null;
        alphaOrder.put("a", 1);
        alphaOrder.put("b", 2);
        alphaOrder.put("c", 3);
        alphaOrder.put("d", 4);
        alphaOrder.put("e", 5);
        alphaOrder.put("f", 6);
        alphaOrder.put("g", 7);
        alphaOrder.put("h", 8);
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
                case "leave" -> leave();
                case "resign" -> resign();
                case "highlight" -> highlight(params);
                case "redraw" -> redraw(params);
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
            return String.format("You joined game %s as %s.\n%s", id, color, board(id));
        }
        throw new ClientException("Expected: <GameNumber> <TeamColor>");
    }

    public String move(String... params) throws Exception {
        assertSignedIn();
        assertPlaying();
        if (params.length < 4) {
            throw new ClientException("Error: Enter the desired move as <startInt> <startChar> <endInt> <endChar>");
        }
        Integer id = playing;
        Integer startRow = alphaOrder.get(params[1]);
        Integer endRow = alphaOrder.get(params[3]);
        ChessPosition start = new ChessPosition(startRow, Integer.parseInt(params[0]));
        ChessPosition end = new ChessPosition(endRow, Integer.parseInt(params[2]));
        GameList gamesList = server.listGames();
        for (GameData game : gamesList.games) {
            if (Objects.equals(game.gameID(), id)) {
                try {
                    ChessPiece piece = game.game().getBoard().getPiece(start);
                    if (piece == null) {throw new ClientException("There is no piece there");}
                    ChessMove moveRequest = new ChessMove(start, end, null);
                    ws.movePiece(authToken, id, moveRequest);
                    return String.format("You made move %s", moveRequest);
                } catch (InvalidMoveException e) {
                    throw new ClientException("Not a valid move");
                }
            }
        }
        throw new ClientException("unable to make move");
    }

    public String leave() throws Exception {
        GameList gamesList = server.listGames();
        for (GameData game : gamesList.games) {
            if (Objects.equals(game.gameID(), playing)) {
                if (game.whiteUsername().equals(userName)) {
                    JoinGameData ld = new JoinGameData(playing, color, new UserData(null, null, null));
                    server.leaveGame(ld);
                    ws.leaveGame(authToken, playing);
                    return String.format("You have left game %d", playing);
                }
            }
        }
        throw new ClientException("Unable to Leave");
    }

    public String resign() throws Exception {
        Scanner confirm = new Scanner(System.in);
        System.out.println("Are you sure you want to resign and forfeit the game? Enter `Yes` to confirm");
        String response = confirm.nextLine();
        if (Objects.equals(response, "Yes")) {
            ws.resignFromGame(authToken, playing);
            return String.format("You have resigned from game %d.", playing);
        }
        return "Resignation cancelled. Have fun!";
    }

    public String observe(String... params) throws Exception {
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
                ws.connectToGame(authToken, playing);
                result.append(String.format("%20s\n%s" + SET_TEXT_COLOR_BLUE + "%20s\n", black, board(num), white));
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

    public String redraw(String... params) throws Exception {
        assertSignedIn();
        if (params.length == 1) {
            int num;
            try {
                num = Integer.parseInt(params[0]);
            } catch (Exception e) {
                throw new ClientException("Error: please use Game Number");
            }
            GameList gamesList = server.listGames();
            if (gamesList.games.isEmpty()) {
                return "No games have been created...";
            }
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
                    if (black.equals(userName)) {
                        result.append(String.format(
                                "%20s\n%s" + SET_TEXT_COLOR_BLUE + "%20s\n", white, board(num), black));
                        return result.toString();
                    }
                    result.append(String.format(
                            "%20s\n%s" + SET_TEXT_COLOR_BLUE + "%20s\n", black, board(num), white));
                    return result.toString();
                }
            }
            return String.format("Error: Game Number: %d does not exist", num);
        }
        throw new ClientException("Error: expected <GameID>");
    }

    public String highlight(String... params) throws Exception {
        assertSignedIn();
        if (params.length == 2) {
            Integer id = playing;
            Integer startRow = alphaOrder.get(params[1]);
            ChessPosition start = new ChessPosition(startRow, Integer.parseInt(params[0]));
            GameList gamesList = server.listGames();
            if (gamesList.games.isEmpty()) {
                return "No games have been created...";
            }
            var result = new StringBuilder();
            for (GameData game : gamesList.games) {
                if (Objects.equals(game.gameID(), id)) {
                    Collection<ChessMove> moves = game.game().validMoves(start);
                    String black = game.blackUsername();
                    if (black == null) {
                        black = "~empty~";
                    }
                    String white = game.whiteUsername();
                    if (white == null) {
                        white = "~empty~";
                    }
                    if (black.equals(userName)) {
                        result.append(String.format(
                                "%20s\n%s" + SET_TEXT_COLOR_BLUE + "%20s\n", white, highlightBoard(id, moves, start), black));
                        return result.toString();
                    }
                    result.append(String.format(
                            "%20s\n%s" + SET_TEXT_COLOR_BLUE + "%20s\n", black, highlightBoard(id, moves, start), white));
                    return result.toString();
                }
            }
            return String.format("Error: Game Number: %d does not exist", id);
        }
        throw new ClientException("Error: expected <startInt> <startChar>");
    }

    public String board(int id) throws ClientException {
        GameList gamesList = server.listGames();
        for (GameData game : gamesList.games) {
            if (Objects.equals(game.gameID(), id)) {
                ChessBoard boardData = game.game().getBoard();
                var board = new StringBuilder();
                if (game.blackUsername() != null && game.blackUsername().equals(userName) &&
                        (game.whiteUsername() == null || !game.whiteUsername().equals(userName))) {
                    board.append(topBottomBorderRev());
                    for (int i = 1; i <= 8; i++) {
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        for (int n = 1; n <= 8; n++) {
                            String bgColor = SET_BG_COLOR_LIGHT_GREY;
                            if ((i%2==0) == (n%2==0)) {
                                bgColor = SET_BG_COLOR_DARK_GREY;
                            }
                            board.append(addPiece(i, n, boardData, bgColor, game.game()));
                        }
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        board.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
                    }
                    board.append(topBottomBorderRev());
                }
                else {
                    board.append(topBottomBorder());
                    for (int i = 8; i >= 1; i--) {
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        for (int n = 8; n >= 1; n--) {
                            String bgColor = SET_BG_COLOR_LIGHT_GREY;
                            if ((i%2==0) == (n%2==0)) {
                                bgColor = SET_BG_COLOR_DARK_GREY;
                            }
                            board.append(addPiece(i, n, boardData, bgColor, game.game()));
                        }
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        board.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
                    }
                    board.append(topBottomBorder());
                }
                return String.format("%s", board);
            }
        }
        throw new ClientException("Expected: <GameNumber>");
    }

    public String highlightBoard(int id, Collection<ChessMove> moves, ChessPosition start) throws ClientException {
        GameList gamesList = server.listGames();
        for (GameData game : gamesList.games) {
            if (Objects.equals(game.gameID(), id)) {
                ChessBoard boardData = game.game().getBoard();
                var board = new StringBuilder();
                if (game.blackUsername() != null && game.blackUsername().equals(userName) &&
                        (game.whiteUsername() == null || !game.whiteUsername().equals(userName))) {
                    board.append(topBottomBorderRev());
                    for (int i = 1; i <= 8; i++) {
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        for (int n = 1; n <= 8; n++) {
                            ChessMove spot = new ChessMove(start, new ChessPosition(i, n), null);
                            String bgColor = SET_BG_COLOR_LIGHT_GREY;
                            if (moves.contains(spot)) {
                                bgColor = SET_BG_COLOR_GREEN;
                            }
                            if ((i%2==0) == (n%2==0)) {
                                bgColor = SET_BG_COLOR_DARK_GREY;
                                if (moves.contains(spot)) {
                                    bgColor = SET_BG_COLOR_DARK_GREEN;
                                }
                            }
                            if (new ChessPosition(i, n).equals(start)) {
                                bgColor = SET_BG_COLOR_YELLOW;
                            }
                            board.append(addPiece(i, n, boardData, bgColor, game.game()));
                        }
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        board.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
                    }
                    board.append(topBottomBorderRev());
                }
                else {
                    board.append(topBottomBorder());
                    for (int i = 8; i >= 1; i--) {
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        for (int n = 8; n >= 1; n--) {
                            String bgColor = SET_BG_COLOR_LIGHT_GREY;
                            if ((i%2==0) == (n%2==0)) {
                                bgColor = SET_BG_COLOR_DARK_GREY;
                            }
                            board.append(addPiece(i, n, boardData, bgColor, game.game()));
                        }
                        board.append(String.format(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + " %d ", i));
                        board.append(RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");
                    }
                    board.append(topBottomBorder());
                }
                return String.format("%s", board);
            }
        }
        throw new ClientException("Expected: <GameNumber>");
    }

    public String addPiece(int row, int col, ChessBoard boardData, String bgColor, ChessGame game) {
        var space = new StringBuilder();
        ChessPiece piece = boardData.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            space.append(bgColor).append(SET_TEXT_COLOR_WHITE).append(EMPTY);
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.PAWN)) {
            if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                space.append(bgColor).append(SET_TEXT_COLOR_WHITE).append(WHITE_PAWN);
            } else {
                space.append(bgColor).append(SET_TEXT_COLOR_BLACK).append(BLACK_PAWN);}
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.ROOK)) {
            if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                space.append(bgColor).append(SET_TEXT_COLOR_WHITE).append(WHITE_ROOK);
            } else {
                space.append(bgColor).append(SET_TEXT_COLOR_BLACK).append(BLACK_ROOK);}
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.KNIGHT)) {
            if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                space.append(bgColor).append(SET_TEXT_COLOR_WHITE).append(WHITE_KNIGHT);
            } else {
                space.append(bgColor).append(SET_TEXT_COLOR_BLACK).append(BLACK_KNIGHT);}
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.BISHOP)) {
            if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                space.append(bgColor).append(SET_TEXT_COLOR_WHITE).append(WHITE_BISHOP);
            } else {
                space.append(bgColor).append(SET_TEXT_COLOR_BLACK).append(BLACK_BISHOP);}
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.QUEEN)) {
            if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                space.append(bgColor).append(SET_TEXT_COLOR_WHITE).append(WHITE_QUEEN);
            } else {
                space.append(bgColor).append(SET_TEXT_COLOR_BLACK).append(BLACK_QUEEN);}
        }
        else if (piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
            if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
                if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                    bgColor = SET_BG_COLOR_RED;
                }
                space.append(bgColor).append(SET_TEXT_COLOR_WHITE).append(WHITE_KING);
            } else {
                if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                    bgColor = SET_BG_COLOR_RED;
                }
                space.append(bgColor).append(SET_TEXT_COLOR_BLACK).append(BLACK_KING);}
        }
        return String.format("%s", space);
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