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

    public boolean addMove(ChessBoard board, ChessPosition myPosition, ChessPiece piece,
                           List<ChessMove> moves, int x, int y, boolean b) {
        ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
        if (encounter == null) {
            moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
        }
        if (encounter != null && encounter.getTeamColor() == piece.getTeamColor()) {
            b = true;
        } else if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
            moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
            b = true;
        }
        return b;
    }

    public void certainSpotMove(ChessBoard board, ChessPosition myPosition, ChessPiece piece,
                                List<ChessMove> moves, int x, int y) {
        ChessPiece encounter = board.getPiece(new ChessPosition(x, y));
        if (encounter == null) {
            moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
        }
        if (encounter != null && encounter.getTeamColor() != piece.getTeamColor()) {
            moves.add(new ChessMove(myPosition, new ChessPosition(x, y), null));
        }
    }

    public List<ChessMove> straightMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece,
                                         List<ChessMove> moves, int xStart, int yStart, int x, int y, boolean b) {
        while (!b && x++ < 8) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        // right
        x = xStart;
        y = yStart;
        b = false;
        while (!b && y++ < 8) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        // down
        x = xStart;
        y = yStart;
        b = false;
        while (!b && x-- > 1) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        // left
        x = xStart;
        y = yStart;
        b = false;
        while (!b && y-- > 1) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        return moves;
    }

    public List<ChessMove> diagonalMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece,
                                         List<ChessMove> moves, int xStart, int yStart, int x, int y, boolean b) {
        while (!b && x++ < 8 && y-- > 1) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        // up right
        x = xStart;
        y = yStart;
        b = false;
        while (!b && x++ < 8 && y++ < 8) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        // down right
        x = xStart;
        y = yStart;
        b = false;
        while (!b && x-- > 1 && y++ < 8) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        // down left
        x = xStart;
        y = yStart;
        b = false;
        while (!b && x-- > 1 && y-- > 1) {
            b = addMove(board, myPosition, piece,  moves, x, y, b);
        }
        return moves;

    }

    public List<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece,
                                     List<ChessMove> moves, int x, int y) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
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
            if (y < 8) {
                ChessPiece diagonalRightEncounter = board.getPiece(new ChessPosition(x + 1, y + 1));
                if (diagonalRightEncounter != null && diagonalRightEncounter.getTeamColor() != piece.getTeamColor()) {
                    if (x + 1 == 8) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y+1), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y+1), PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y+1), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y+1), PieceType.KNIGHT));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y+1), null));
                    }
                }
            }
            if (y > 1) {
                ChessPiece diagonalLeftEncounter = board.getPiece(new ChessPosition(x + 1, y - 1));
                if (diagonalLeftEncounter != null && diagonalLeftEncounter.getTeamColor() != piece.getTeamColor()) {
                    if (x + 1 == 8) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y-1), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y-1), PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y-1), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y-1), PieceType.KNIGHT));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x+1, y-1), null));
                    }
                }
            }
            if (x == 2) {
                ChessPiece encounter2 = board.getPiece(new ChessPosition(x + 2, y));
                if (encounter2 == null && encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x + 2, y), null));
                }
            }
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
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
            if (y < 8) {
                ChessPiece diagonalRightEncounter = board.getPiece(new ChessPosition(x - 1, y + 1));
                if (diagonalRightEncounter != null && diagonalRightEncounter.getTeamColor() != piece.getTeamColor()) {
                    if (x - 1 == 1) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y+1), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y+1), PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y+1), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y+1), PieceType.KNIGHT));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y+1), null));
                    }
                }
            }
            if (y > 1) {
                ChessPiece diagonalLeftEncounter = board.getPiece(new ChessPosition(x - 1, y - 1));
                if (diagonalLeftEncounter != null && diagonalLeftEncounter.getTeamColor() != piece.getTeamColor()) {
                    if (x - 1 == 1) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y-1), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y-1), PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y-1), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y-1), PieceType.KNIGHT));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(x-1, y-1), null));
                    }
                }
            }
            if (x == 7) {
                ChessPiece encounter2 = board.getPiece(new ChessPosition(x - 2, y));
                if (encounter2 == null && encounter == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(x - 2, y), null));
                }
            }
        }
        return moves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            List<ChessMove> moves = new ArrayList<>();
            boolean b = false;
            return diagonalMoves(board, myPosition, piece, moves, myPosition.getRow(),
                    myPosition.getColumn(), myPosition.getRow(), myPosition.getColumn(), b);
        }
        if (piece.getPieceType() == PieceType.KING) {
            List<ChessMove> moves = new ArrayList<>();
            if (myPosition.getRow() < 8) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() + 1, myPosition.getColumn());}
            if (myPosition.getRow() < 8 && myPosition.getColumn() < 8) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() + 1, myPosition.getColumn() + 1);}
            if (myPosition.getColumn() < 8) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow(), myPosition.getColumn() + 1);}
            if (myPosition.getRow() > 1 && myPosition.getColumn() < 8) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() - 1, myPosition.getColumn() + 1);}
            if (myPosition.getRow() > 1) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() - 1, myPosition.getColumn());}
            if (myPosition.getRow() > 1 && myPosition.getColumn() > 1) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() - 1, myPosition.getColumn() - 1);}
            if (myPosition.getColumn() > 1) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow(), myPosition.getColumn() - 1);}
            if (myPosition.getRow() < 8 && myPosition.getColumn() > 1) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() + 1, myPosition.getColumn() - 1);}
            return moves;
        }
        if (piece.getPieceType() == PieceType.KNIGHT) {
            List<ChessMove> moves = new ArrayList<>();
            if (myPosition.getRow() < 7 && myPosition.getColumn() < 8) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() + 2, myPosition.getColumn() + 1);}
            if (myPosition.getRow() < 8 && myPosition.getColumn() < 7) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() + 1, myPosition.getColumn() + 2);}
            if (myPosition.getRow() > 1 && myPosition.getColumn() < 7) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() - 1, myPosition.getColumn() + 2);}
            if (myPosition.getRow() > 2 && myPosition.getColumn() < 8) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() - 2, myPosition.getColumn() + 1);}
            if (myPosition.getRow() > 2 && myPosition.getColumn() > 1) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() - 2, myPosition.getColumn() - 1);}
            if (myPosition.getRow() > 1 && myPosition.getColumn() > 2) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() - 1, myPosition.getColumn() - 2);}
            if (myPosition.getRow() < 8 && myPosition.getColumn() > 2) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() + 1, myPosition.getColumn() -2);}
            if (myPosition.getRow() < 7 && myPosition.getColumn() > 1) {certainSpotMove(board, myPosition, piece, moves,
                    myPosition.getRow() + 2, myPosition.getColumn() - 1);}
            return moves;
        }
        if (piece.getPieceType() == PieceType.ROOK) {
            List<ChessMove> moves = new ArrayList<>();
            int xStart = myPosition.getRow();
            int yStart = myPosition.getColumn();
            int x = xStart;
            int y = yStart;
            boolean b = false;
            return straightMoves(board, myPosition, piece, moves, xStart, yStart, x, y, b);
        }
        if (piece.getPieceType() == PieceType.QUEEN) {
            List<ChessMove> moves = new ArrayList<>();
            int xStart = myPosition.getRow();
            int yStart = myPosition.getColumn();
            int x = xStart;
            int y = yStart;
            boolean b = false;
            moves = straightMoves(board, myPosition, piece, moves, xStart, yStart, x, y, b);
            return diagonalMoves(board, myPosition, piece, moves, xStart, yStart, x, y, b);
        }
        if (piece.getPieceType() == PieceType.PAWN) {
            List<ChessMove> moves = new ArrayList<>();
            return pawnMoves(board, myPosition, piece, moves, myPosition.getRow(), myPosition.getColumn());
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
