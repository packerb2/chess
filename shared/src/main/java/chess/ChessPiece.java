package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            // create a list of all possible moves assuming an empty board
            List<ChessMove> moves = new ArrayList<>();
            int x_start = myPosition.getRow();
            int y_start = myPosition.getColumn();
            // down right
            int x = x_start;
            int y = y_start;
            while (x++ < 8 && y-- > 1) {
                moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
            }
            // up right
            x = x_start;
            y = y_start;
            while (x++ < 8 && y++ < 8) {
                moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
            }
            // up left
            x = x_start;
            y = y_start;
            while (x-- > 1 && y++ < 8) {
                moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
            }
            // down left
            x = x_start;
            y = y_start;
            while (x-- > 1 && y-- > 1) {
                moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
            }
            return moves;
        }
        return List.of();
    }
}
