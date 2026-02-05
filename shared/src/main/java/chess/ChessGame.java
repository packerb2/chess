package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard current_board;
    private TeamColor team_turn;

    public ChessGame() {
        this.current_board = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return team_turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        team_turn = team;
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
        ChessPiece piece = current_board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(current_board, startPosition);
        Collection<ChessMove> to_remove = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessBoard test_board = new ChessBoard(current_board);
            test_board.addPiece(move.getEndPosition(), piece);
            test_board.addPiece(move.getStartPosition(), null);
            if (isMoveIntoCheck(piece.getTeamColor(), test_board)) {
                to_remove.add(move);
            }
        }
        for (ChessMove wrong : to_remove) {
            moves.remove(wrong);
        }
        return moves;
    }

    public boolean isMoveIntoCheck(TeamColor teamColor, ChessBoard test_board) {
        ChessPosition spot;
        ChessPiece piece;
        int x = 0;
        while (x++ < 8) {
            int y = 0;
            while (y++ < 8) {
                spot = new ChessPosition(x, y);
                piece = test_board.getPiece(spot);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(test_board, spot);
                    for (ChessMove move : moves) {
                        ChessPosition square = move.getEndPosition();
                        ChessPiece target = test_board.getPiece(square);
                        if (target != null && target.getPieceType() == ChessPiece.PieceType.KING && target.getTeamColor() == teamColor) {
                            return true;
                        }
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
        throw new RuntimeException("Not implemented");
        // get move start, check valid moves. if move is valid, move. If not, throw exception
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
                piece = current_board.getPiece(spot);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(current_board, spot);
                    for (ChessMove move : moves) {
                        ChessPosition square = move.getEndPosition();
                        ChessPiece target = current_board.getPiece(square);
                        if (target != null && target.getPieceType() == ChessPiece.PieceType.KING && target.getTeamColor() == teamColor) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        current_board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return current_board;
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }


}
