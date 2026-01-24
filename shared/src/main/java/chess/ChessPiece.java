package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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


        /** list of all BISHOP moves */
        if (piece.getPieceType() == PieceType.BISHOP) {
            List<ChessMove> moves = new ArrayList<>();
            int x_start = myPosition.getRow();
            int y_start = myPosition.getColumn();
            // up left
            int x = x_start;
            int y = y_start;
            boolean b = false;
            while (!b && x++ < 8 && y-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // up right
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x++ < 8 && y++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // down right
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x-- > 1 && y++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // down left
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x-- > 1 && y-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            return moves;
        }


        /** list of all KING moves */
        if (piece.getPieceType() == PieceType.KING) {
            List<ChessMove> moves = new ArrayList<>();
            int x = myPosition.getRow();
            int y = myPosition.getColumn();
            // up
            if (x < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 1, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y), null));
                }
            }
            // up right
            if (x < 8 && y < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 1, y + 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 1), null));
                }
            }
            // right
            if (y < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y + 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y + 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y + 1), null));
                }
            }
            // down right
            if (x > 1 && y < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 1, y + 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 1), null));
                }
            }
            // down
            if (x > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 1, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y), null));
                }
            }
            // down left
            if (x > 1 && y > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 1, y - 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 1), null));
                }
            }
            // left
            if (y > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y - 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y - 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y - 1), null));
                }
            }
            // up left
            if (x < 8 && y > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 1, y - 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 1), null));
                }
            }
            return moves;
        }


        /** list of all KNIGHT moves */
        if (piece.getPieceType() == PieceType.KNIGHT) {
            List<ChessMove> moves = new ArrayList<>();
            int x = myPosition.getRow();
            int y = myPosition.getColumn();
            // up up right
            if (x < 7 && y < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 2, y + 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 2, y + 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 2, y + 1), null));
                }
            }
            // up right right
            if (x < 8 && y < 7) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 1, y + 2));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 2), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 2), null));
                }
            }
            // down right right
            if (x > 1 && y < 7) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 1, y + 2));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 2), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 2), null));
                }
            }
            // down down right
            if (x > 2 && y < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 2, y + 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 2, y + 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 2, y + 1), null));
                }
            }
            // down down left
            if (x > 2 && y > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 2, y - 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 2, y - 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 2, y - 1), null));
                }
            }
            // down left left
            if (x > 1 && y > 2) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 1, y - 2));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 2), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 2), null));
                }
            }
            // up left left
            if (x < 8 && y > 2) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 1, y - 2));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 2), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 2), null));
                }
            }
            // up up left
            if (x < 7 && y > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 2, y - 1));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 2, y - 1), null));
                }
                if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 2, y - 1), null));
                }
            }
            return moves;
        }


        /** list of all ROOK moves */
        if (piece.getPieceType() == PieceType.ROOK) {
            List<ChessMove> moves = new ArrayList<>();
            int x_start = myPosition.getRow();
            int y_start = myPosition.getColumn();
            // up
            int x = x_start;
            int y = y_start;
            boolean b = false;
            while (!b && x++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // right
            x = x_start;
            y = y_start;
            b = false;
            while (!b && y++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // down
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // left
            x = x_start;
            y = y_start;
            b = false;
            while (!b && y-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            return moves;
        }


        /** list of all QUEEN moves */
        if (piece.getPieceType() == PieceType.QUEEN) {
            List<ChessMove> moves = new ArrayList<>();
            int x_start = myPosition.getRow();
            int y_start = myPosition.getColumn();
            // up
            int x = x_start;
            int y = y_start;
            boolean b = false;
            while (!b && x++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // right
            x = x_start;
            y = y_start;
            b = false;
            while (!b && y++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // down
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // left
            x = x_start;
            y = y_start;
            b = false;
            while (!b && y-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // up left
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x++ < 8 && y-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // up right
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x++ < 8 && y++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // down right
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x-- > 1 && y++ < 8) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            // down left
            x = x_start;
            y = y_start;
            b = false;
            while (!b && x-- > 1 && y-- > 1) {
                ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
                if (encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                }
                if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
                    b = true;
                }
                else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
                    b = true;
                }
            }
            return moves;
        }


        /** list of all PAWN moves */
        if (piece.getPieceType() == PieceType.PAWN) {
            List<ChessMove> moves = new ArrayList<>();
            int x = myPosition.getRow();
            int y = myPosition.getColumn();

            // white
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                // up
                ChessPiece encounter = board.getPiece(new ChessPosition(x + 1, y));
                if (encounter == null) {
                    if (x + 1 == 8) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y), PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y), PieceType.KNIGHT));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y), null));
                    }
                }
                // right capture
                if (y < 8) {
                    ChessPiece diagonal_right_encounter = board.getPiece(new ChessPosition(x + 1, y + 1));
                    if (diagonal_right_encounter != null && diagonal_right_encounter.getTeamColor() != piece.getTeamColor()) {
                        if (x + 1 == 8) {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 1), PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 1), PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 1), PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 1), PieceType.KNIGHT));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y + 1), null));
                        }
                    }
                }
                // left capture
                if (y > 1) {
                    ChessPiece diagonal_left_encounter = board.getPiece(new ChessPosition(x + 1, y - 1));
                    if (diagonal_left_encounter != null && diagonal_left_encounter.getTeamColor() != piece.getTeamColor()) {
                        if (x + 1 == 8) {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 1), PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 1), PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 1), PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 1), PieceType.KNIGHT));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x + 1, y - 1), null));
                        }
                    }
                }
                // initial
                if (x == 2) {
                    ChessPiece encounter_2 = board.getPiece(new ChessPosition(x + 2, y));
                    if (encounter_2 == null && encounter == null) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x + 2, y), null));
                    }
                }
            }

            // black
            if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                // up
                ChessPiece encounter = board.getPiece(new ChessPosition(x - 1, y));
                if (encounter == null) {
                    if (x - 1 == 1) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y), PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y), PieceType.KNIGHT));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y), null));
                    }
                }
                // right capture
                if (y < 8) {
                    ChessPiece diagonal_right_encounter = board.getPiece(new ChessPosition(x - 1, y + 1));
                    if (diagonal_right_encounter != null && diagonal_right_encounter.getTeamColor() != piece.getTeamColor()) {
                        if (x - 1 == 1) {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 1), PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 1), PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 1), PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 1), PieceType.KNIGHT));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y + 1), null));
                        }
                    }
                }
                // left capture
                if (y > 1) {
                    ChessPiece diagonal_left_encounter = board.getPiece(new ChessPosition(x - 1, y - 1));
                    if (diagonal_left_encounter != null && diagonal_left_encounter.getTeamColor() != piece.getTeamColor()) {
                        if (x - 1 == 1) {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 1), PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 1), PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 1), PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 1), PieceType.KNIGHT));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, new ChessPosition(x - 1, y - 1), null));
                        }
                    }
                }
                // initial
                if (x == 7) {
                    ChessPiece encounter_2 = board.getPiece(new ChessPosition(x - 2, y));
                    if (encounter_2 == null && encounter == null) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x - 2, y), null));
                    }
                }
            }
            return moves;
        }

        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
