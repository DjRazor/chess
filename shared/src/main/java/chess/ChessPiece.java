package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private int id;
    private String name;
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece that)) return false;
        return id == that.id && Objects.equals(name, that.name) && pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, pieceColor, type);
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

        ChessPiece currentPiece = board.getPiece(myPosition);
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        ChessPosition topRight = new ChessPosition(row + 1, col + 1);
        ChessPosition topLeft = new ChessPosition(row + 1, col - 1);
        ChessPosition bottomRight = new ChessPosition(row - 1, col + 1);
        ChessPosition bottomLeft = new ChessPosition(row - 1, col - 1);
        if (currentPiece.type == PieceType.BISHOP) {
            Collection<ChessMove> Test = new HashSet<>();
            //Test.add(new ChessMove(new ChessPosition(5, 4), new ChessPosition(6, 5), null));
            while (topLeft.getColumn() >= 0 && topLeft.getColumn() <= 7 && topLeft.getRow() >= 0 && topLeft.getRow() <= 7 && board.getPiece(topLeft) == null) {
                Test.add(new ChessMove(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1), new ChessPosition(topLeft.getRow() + 1, topLeft.getColumn() + 1), null));
                topLeft = new ChessPosition(topLeft.getRow() + 1, topLeft.getColumn() - 1);
            }
            while (topRight.getColumn() >= 0 && topRight.getColumn() <= 7 && topRight.getRow() >= 0 && topRight.getRow() <= 7 && board.getPiece(topRight) == null) {
                Test.add(new ChessMove(myPosition, topRight, null));
                topRight = new ChessPosition(topRight.getRow() + 1, topRight.getColumn() + 1);
            }
            while (bottomLeft.getColumn() >= 0 && bottomLeft.getColumn() <= 7 && bottomLeft.getRow() >= 0 && bottomLeft.getRow() <= 7 && board.getPiece(bottomLeft) == null) {
                Test.add(new ChessMove(myPosition, bottomLeft, null));
                bottomLeft = new ChessPosition(bottomLeft.getRow() - 1, bottomLeft.getColumn() - 1);
            }
            while (bottomRight.getColumn() >= 0 && bottomRight.getColumn() <= 7 && bottomRight.getRow() >= 0 && bottomRight.getRow() <= 7 && board.getPiece(bottomRight) == null) {
                Test.add(new ChessMove(myPosition, bottomRight, null));
                bottomRight = new ChessPosition(bottomRight.getRow() - 1, bottomRight.getColumn() + 1);
            }
//            int count = 0;
//            for (ChessMove x : Test) {
//                count += 1;
//            }
            //System.out.println(count);
            //Test.add(new ChessMove(new ChessPosition(6, 5), new ChessPosition(2,2), null));
            //Test.add(new ChessMove(new ChessPosition(3, 3), new ChessPosition(4,4), null));
            return Test;
        }
        Collection<ChessMove> fail = new HashSet<>();
        return fail;
    }
}
