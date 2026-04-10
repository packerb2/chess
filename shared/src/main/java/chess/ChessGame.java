package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard currentBoard;
    private TeamColor teamTurn;
    public boolean playing;
    public boolean whiteCheck;
    public boolean blackCheck;

    public ChessGame() {
        this.currentBoard = new ChessBoard();
        currentBoard.resetBoard();
        this.teamTurn = TeamColor.WHITE;
        this.playing = true;
        this.whiteCheck = false;
        this.blackCheck = false;
    }

    public void endGame() {playing = false;}

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = currentBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(currentBoard, startPosition);
        Collection<ChessMove> toRemove = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessBoard testBoard = new ChessBoard(currentBoard);
            testBoard.addPiece(move.getEndPosition(), piece);
            testBoard.addPiece(move.getStartPosition(), null);
            if (isMoveIntoCheck(piece.getTeamColor(), testBoard)) {
                toRemove.add(move);
            }
        }
        for (ChessMove wrong : toRemove) {
            moves.remove(wrong);
        }
        return moves;
    }

    public boolean checkCheck(TeamColor teamColor, ChessBoard testBoard, Collection<ChessMove> moves) {
        for (ChessMove move : moves) {
            ChessPosition square = move.getEndPosition();
            ChessPiece target = testBoard.getPiece(square);
            if (target != null && target.getPieceType() == ChessPiece.PieceType.KING && target.getTeamColor() == teamColor) {
                return true;
            }
        }
        return false;
    }

    public boolean isMoveIntoCheck(TeamColor teamColor, ChessBoard testBoard) {
        ChessPosition spot;
        ChessPiece piece;
        int x = 0;
        while (x++ < 8) {
            int y = 0;
            while (y++ < 8) {
                spot = new ChessPosition(x, y);
                piece = testBoard.getPiece(spot);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(testBoard, spot);
                    if (checkCheck(teamColor, testBoard, moves)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // check turn, get move start, check valid moves. if valid, move and change turn. else, throw exception
        TeamColor turn = getTeamTurn();
        ChessPosition location = move.getStartPosition();
        ChessPiece piece = currentBoard.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != turn) {
            throw new InvalidMoveException();
        }
        Collection<ChessMove> moves = validMoves(location);
        if (!moves.contains(move)) {
            throw new InvalidMoveException();
        }
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (piece.getTeamColor() == TeamColor.BLACK && move.getEndPosition().getRow() == 1) {
                piece = new ChessPiece(TeamColor.BLACK, move.getPromotionPiece());
            }
            else if (piece.getTeamColor() == TeamColor.WHITE && move.getEndPosition().getRow() == 8) {
                piece = new ChessPiece(TeamColor.WHITE, move.getPromotionPiece());
            }
        }
        currentBoard.addPiece(move.getEndPosition(), piece);
        currentBoard.addPiece(move.getStartPosition(), null);
        blackCheck = false;
        whiteCheck = false;
        if (turn == TeamColor.WHITE) {
            if (isInCheckmate(TeamColor.BLACK) || isInStalemate(TeamColor.BLACK)) {
                playing = false;
            }
            else {
                setTeamTurn(TeamColor.BLACK);
            }
            blackCheck = isInCheck(TeamColor.BLACK);
        }
        else {
            if (isInCheckmate(TeamColor.WHITE) || isInStalemate(TeamColor.WHITE)) {
                playing = false;
            }
            else {
                setTeamTurn(TeamColor.WHITE);
            }
            whiteCheck = isInCheck(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition spot;
        ChessPiece piece;
        int x = 0;
        while (x++ < 8) {
            int y = 0;
            while (y++ < 8) {
                spot = new ChessPosition(x, y);
                piece = currentBoard.getPiece(spot);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(currentBoard, spot);
                    if (checkCheck(teamColor, currentBoard, moves)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isSurrounded(TeamColor teamColor) {
        ChessPosition spot;
        ChessPiece piece;
        int x = 0;
        while (x++ < 8) {
            int y = 0;
            while (y++ < 8) {
                spot = new ChessPosition(x, y);
                piece = currentBoard.getPiece(spot);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> escape = validMoves(spot);
                    if (!escape.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }



    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            if (isSurrounded(teamColor)) {
                playing = false;
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            if (isSurrounded(teamColor)) {
                playing = false;
                return true;
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        currentBoard = new ChessBoard(board);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(currentBoard, chessGame.currentBoard) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBoard, teamTurn);
    }
}
