package chess;

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

    @Override
    public String toString() {
        return "ChessPiece{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
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

    public ChessPosition positionBooster(ChessPosition position){
        return new ChessPosition(position.getRow() + 1, position.getColumn() + 1);
    }
    public boolean checkSpot(ChessBoard board, ChessPosition position) {
        return position.getRow() >= 0 && position.getRow() <= 7 && position.getColumn() >= 0 && position.getColumn() <= 7 && board.getPiece(position) == null;
    }
    public boolean checkEnemy(ChessBoard board, ChessPosition position, ChessPiece currentPiece) {
        if (position.getRow() >= 0 && position.getRow() <= 7 && position.getColumn() >= 0 && position.getColumn() <= 7 && board.getPiece(position) != null) {
            if (currentPiece.getTeamColor() != board.getPiece(position).getTeamColor()) {
                return true;
            }
        }
        return false;
    }
    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> pawnPossibles = new HashSet<>();
        ChessPosition top = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
        ChessPosition twoTop = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
        ChessPosition twoBottom = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
        ChessPosition topLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
        ChessPosition topRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);

        // Checks to see if unmoved pawn can move 2 spaces
        if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 1 && board.getPiece(twoTop) == null) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(twoTop), null));
        } else if (currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 6 && board.getPiece(twoBottom) == null) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(twoBottom), null));
        }

        // Checks if pawn can move one spot forward
        if (checkSpot(board, top) && currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(top), null));
            //top = new ChessPosition(top.getRow() + 1, top.getColumn());
        } else

        if (checkEnemy(board, topLeft, currentPiece)) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(topLeft), null));
        }

        return pawnPossibles;
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
        ChessPosition top = new ChessPosition(row + 1, col);
        ChessPosition bottom = new ChessPosition(row - 1, col);
        ChessPosition left = new ChessPosition(row, col - 1);
        ChessPosition right = new ChessPosition(row, col + 1);

        if (currentPiece.getPieceType() == PieceType.BISHOP) {
            Collection<ChessMove> Test = new HashSet<>();
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
            int count = 0;
            for (ChessMove x : Test) {
                count += 1;
            }
            System.out.println(count);
            //Test.add(new ChessMove(new ChessPosition(6, 5), new ChessPosition(2,2), null));
            //Test.add(new ChessMove(new ChessPosition(3, 3), new ChessPosition(4,4), null));
            return Test;
        }

        if (currentPiece.getPieceType() == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        }

        Collection<ChessMove> fail = new HashSet<>();
        return fail;
    }
}
