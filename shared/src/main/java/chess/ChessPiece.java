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

    //private int id;
    //private String name;
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;



    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece that)) return false;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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

    public boolean checkSpot(ChessBoard board, ChessPosition position) {
//        System.out.print("Row val: ");
//        System.out.println(position.getRow());
//        System.out.print("Col val: ");
//        System.out.println(position.getColumn());
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8 && board.getPiece(position) == null;
    }
    public boolean checkEnemy(ChessBoard board, ChessPosition position, ChessPiece currentPiece) {
        if (position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8 && board.getPiece(position) != null) {
            if (currentPiece.getTeamColor() != board.getPiece(position).getTeamColor()) {
                // System.out.println(currentPiece.getTeamColor());
                return true;
            }
        }
        return false;
    }

    public boolean checkSpotAvail(ChessBoard board, ChessPosition position, ChessPiece currentPiece) {
        if (checkSpot(board, position)) {
            return true;
        } else if (checkEnemy(board, position, currentPiece)) {
            return true;
        }
        return false;
    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition, ChessPosition topRight, ChessPosition bottomRight, ChessPosition topLeft, ChessPosition bottomLeft) {
        Collection<ChessMove> bishopPossibles = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);

        // Checks top left diagonal
        while(checkSpot(board, topLeft)) {
            bishopPossibles.add(new ChessMove(myPosition, topLeft, null));
            topLeft = new ChessPosition(topLeft.getRow() + 1, topLeft.getColumn() - 1);
        }
        if (checkEnemy(board, topLeft, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, topLeft, null));
        }

        // Checks top right diagonal
        while(checkSpot(board, topRight)) {
            bishopPossibles.add(new ChessMove(myPosition, topRight, null));
            topRight = new ChessPosition(topRight.getRow() + 1, topRight.getColumn() + 1);
        }
        if (checkEnemy(board, topRight, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, topRight, null));
        }

        // Checks bottom left diagonal
        while(checkSpot(board, bottomLeft)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomLeft, null));
            bottomLeft = new ChessPosition(bottomLeft.getRow() - 1, bottomLeft.getColumn() - 1);
        }
        if (checkEnemy(board, bottomLeft, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomLeft, null));
        }
        // Checks bottom right diagonal
        while(checkSpot(board, bottomRight)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomRight, null));
            bottomRight = new ChessPosition(bottomRight.getRow() - 1, bottomRight.getColumn() + 1);
        }
        if (checkEnemy(board, bottomRight, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomRight, null));
        }

        return bishopPossibles;
    }

    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition, ChessPosition topLeft, ChessPosition top, ChessPosition topRight, ChessPosition bottomLeft, ChessPosition bottom, ChessPosition bottomRight) {
        /* NEEDS WORK FOR promotionPiece */
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> pawnPossibles = new HashSet<>();
        ChessPosition twoTop = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
        ChessPosition twoBottom = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());

        // Checks to see if unmoved pawn can move 2 spaces
        if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2 && board.getPiece(twoTop) == null && board.getPiece(top) == null) {
            pawnPossibles.add(new ChessMove(myPosition, twoTop, null));
        } else if (currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7 && board.getPiece(twoBottom) == null && board.getPiece(bottom) == null) {
            pawnPossibles.add(new ChessMove(myPosition, twoBottom, null));
        }

        // Checks if pawn can move one spot forward
        if (checkSpot(board, top) && currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (myPosition.getRow() == 7) {
                pawnPossibles.add(new ChessMove(myPosition, top, PieceType.QUEEN));
                pawnPossibles.add(new ChessMove(myPosition, top, PieceType.BISHOP));
                pawnPossibles.add(new ChessMove(myPosition, top, PieceType.KNIGHT));
                pawnPossibles.add(new ChessMove(myPosition, top, PieceType.ROOK));
            } else {
                pawnPossibles.add(new ChessMove(myPosition, top, null));
            }
        } else if (checkSpot(board, bottom) && currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            if (myPosition.getRow() == 2) {
                pawnPossibles.add(new ChessMove(myPosition, bottom, PieceType.QUEEN));
                pawnPossibles.add(new ChessMove(myPosition, bottom, PieceType.BISHOP));
                pawnPossibles.add(new ChessMove(myPosition, bottom, PieceType.KNIGHT));
                pawnPossibles.add(new ChessMove(myPosition, bottom, PieceType.ROOK));
            } else {
                pawnPossibles.add(new ChessMove(myPosition, bottom, null));
            }
        }

        // Checks if pawn can move to a spot to capture enemy
        if (checkEnemy(board, topLeft, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (myPosition.getRow() == 7) {
                pawnPossibles.add(new ChessMove(myPosition, topLeft, PieceType.QUEEN));
                pawnPossibles.add(new ChessMove(myPosition, topLeft, PieceType.BISHOP));
                pawnPossibles.add(new ChessMove(myPosition, topLeft, PieceType.KNIGHT));
                pawnPossibles.add(new ChessMove(myPosition, topLeft, PieceType.ROOK));
            } else {
                pawnPossibles.add(new ChessMove(myPosition, topLeft, null));
            }
        }
        if (checkEnemy(board, topRight, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            if (myPosition.getRow() == 7) {
                pawnPossibles.add(new ChessMove(myPosition, topRight, PieceType.QUEEN));
                pawnPossibles.add(new ChessMove(myPosition, topRight, PieceType.BISHOP));
                pawnPossibles.add(new ChessMove(myPosition, topRight, PieceType.KNIGHT));
                pawnPossibles.add(new ChessMove(myPosition, topRight, PieceType.ROOK));
            } else {
                pawnPossibles.add(new ChessMove(myPosition, topRight, null));
            }
        }
        if (checkEnemy(board, bottomRight, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            if (myPosition.getRow() == 2) {
                pawnPossibles.add(new ChessMove(myPosition, bottomRight, PieceType.QUEEN));
                pawnPossibles.add(new ChessMove(myPosition, bottomRight, PieceType.BISHOP));
                pawnPossibles.add(new ChessMove(myPosition, bottomRight, PieceType.KNIGHT));
                pawnPossibles.add(new ChessMove(myPosition, bottomRight, PieceType.ROOK));
            } else {
                pawnPossibles.add(new ChessMove(myPosition, bottomRight, null));
            }
        }
        if (checkEnemy(board, bottomLeft, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            if (myPosition.getRow() == 2) {
                pawnPossibles.add(new ChessMove(myPosition, bottomLeft, PieceType.QUEEN));
                pawnPossibles.add(new ChessMove(myPosition, bottomLeft, PieceType.BISHOP));
                pawnPossibles.add(new ChessMove(myPosition, bottomLeft, PieceType.KNIGHT));
                pawnPossibles.add(new ChessMove(myPosition, bottomLeft, PieceType.ROOK));
            } else {
                pawnPossibles.add(new ChessMove(myPosition, bottomLeft, null));
            }
        }

        return pawnPossibles;
    }

    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition, ChessPosition top, ChessPosition bottom, ChessPosition left, ChessPosition right) {
        Collection<ChessMove> rookPossibles = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);

        while (checkSpot(board, top)) {
            rookPossibles.add(new ChessMove(myPosition, top, null));
            top = new ChessPosition(top.getRow() + 1, top.getColumn());
        }
        if (checkEnemy(board, top, currentPiece)) {
            rookPossibles.add(new ChessMove(myPosition, top, null));
        }

        while (checkSpot(board, bottom)) {
            rookPossibles.add(new ChessMove(myPosition, bottom, null));
            bottom = new ChessPosition(bottom.getRow() - 1, bottom.getColumn());
        }
        if (checkEnemy(board, bottom, currentPiece)) {
            rookPossibles.add(new ChessMove(myPosition, bottom, null));
        }

        while (checkSpot(board, left)) {
            rookPossibles.add(new ChessMove(myPosition, left, null));
            left = new ChessPosition(left.getRow(), left.getColumn() - 1);
        }
        if (checkEnemy(board, left, currentPiece)) {
            rookPossibles.add(new ChessMove(myPosition, left, null));
        }

        while (checkSpot(board, right)) {
            rookPossibles.add(new ChessMove(myPosition, right, null));
            right = new ChessPosition(right.getRow(), right.getColumn() + 1);
        }
        if (checkEnemy(board, right, currentPiece)) {
            rookPossibles.add(new ChessMove(myPosition, right, null));
        }

        return rookPossibles;
    }

    public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition, ChessPosition topLeft, ChessPosition top, ChessPosition topRight, ChessPosition left, ChessPosition right, ChessPosition bottomLeft, ChessPosition bottom, ChessPosition bottomRight) {
        Collection<ChessMove> queenPossibles = new HashSet<>();

        Collection<ChessMove> diaganolMoves = rookMoves(board, myPosition, top, bottom, left, right);
        Collection<ChessMove> straightLineMoves = bishopMoves(board, myPosition, topRight, bottomRight, topLeft, bottomLeft);

        queenPossibles.addAll(diaganolMoves);
        queenPossibles.addAll(straightLineMoves);

        return queenPossibles;
    }

    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition, ChessPosition topLeft, ChessPosition top, ChessPosition topRight, ChessPosition left, ChessPosition right, ChessPosition bottomLeft, ChessPosition bottom, ChessPosition bottomRight) {
        Collection<ChessMove> kingPossibles = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);

        if (checkSpot(board, topLeft)) {
            kingPossibles.add(new ChessMove(myPosition, topLeft, null));
        } else if (checkEnemy(board, topLeft, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, topLeft, null));
        }

        if (checkSpot(board, top)) {
            kingPossibles.add(new ChessMove(myPosition, top, null));
        } else if (checkEnemy(board, top, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, top, null));
        }

        if (checkSpot(board, topRight)) {
            kingPossibles.add(new ChessMove(myPosition, topRight, null));
        } else if (checkEnemy(board, topRight, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, topRight, null));
        }

        if (checkSpot(board, left)) {
            kingPossibles.add(new ChessMove(myPosition, left, null));
        } else if (checkEnemy(board, left, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, left, null));
        }

        if (checkSpot(board, right)) {
            kingPossibles.add(new ChessMove(myPosition, right, null));
        } else if (checkEnemy(board, right, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, right, null));
        }

        if (checkSpot(board, bottomLeft)) {
            kingPossibles.add(new ChessMove(myPosition, bottomLeft, null));
        } else if (checkEnemy(board, bottomLeft, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, bottomLeft, null));
        }

        if (checkSpot(board, bottom)) {
            kingPossibles.add(new ChessMove(myPosition, bottom, null));
        } else if (checkEnemy(board, bottom, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, bottom, null));
        }

        if (checkSpot(board, bottomRight)) {
            kingPossibles.add(new ChessMove(myPosition, bottomRight, null));
        } else if (checkEnemy(board, bottomRight, currentPiece)) {
            kingPossibles.add(new ChessMove(myPosition, bottomRight, null));
        }

        return kingPossibles;
    }

    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> knightPossibles = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);

        ChessPosition leftUpLow = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2);
        ChessPosition leftUpUp = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1);
        ChessPosition rightUpUp = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1);
        ChessPosition rightUpLow = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2);
        ChessPosition rightDownUp = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2);
        ChessPosition rightDownDown = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1);
        ChessPosition leftDownDown = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1);
        ChessPosition leftDownUp = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2);

        if (checkSpotAvail(board, leftUpLow, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, leftUpLow, null));
        }
        if (checkSpotAvail(board, leftUpUp, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, leftUpUp, null));
        }
        if (checkSpotAvail(board, rightUpUp, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, rightUpUp, null));
        }
        if (checkSpotAvail(board, rightUpLow, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, rightUpLow, null));
        }
        if (checkSpotAvail(board, rightDownUp, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, rightDownUp, null));
        }
        if (checkSpotAvail(board, rightDownDown, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, rightDownDown, null));
        }
        if (checkSpotAvail(board, leftDownDown, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, leftDownDown, null));
        }
        if (checkSpotAvail(board, leftDownUp, currentPiece)) {
            knightPossibles.add(new ChessMove(myPosition, leftDownUp, null));
        }

        return knightPossibles;
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

        if (currentPiece.getPieceType() == PieceType.PAWN) {
            return pawnMoves(board, myPosition, topLeft, top, topRight, bottomLeft, bottom, bottomRight);
        }
        if (currentPiece.getPieceType() == PieceType.BISHOP) {
            return bishopMoves(board, myPosition, topRight, bottomRight, topLeft, bottomLeft);
        }
        if (currentPiece.getPieceType() == PieceType.ROOK) {
            return rookMoves(board, myPosition, top, bottom, left, right);
        }
        if (currentPiece.getPieceType() == PieceType.QUEEN) {
            return queenMoves(board, myPosition, topLeft, top, topRight, left, right, bottomLeft, bottom, bottomRight);
        }
        if (currentPiece.getPieceType() == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        }
        if (currentPiece.getPieceType() == PieceType.KING) {
            return kingMoves(board, myPosition, topLeft, top, topRight, left, right, bottomLeft, bottom, bottomRight);
        }

        Collection<ChessMove> fail = new HashSet<>();
        return fail;
    }
}
