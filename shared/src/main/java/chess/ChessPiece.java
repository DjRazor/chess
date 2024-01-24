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
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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

//    public Collection<ChessMove> possibleDiagonal(ChessBoard board, ChessPosition position, ChessPosition currentPosition, ChessPiece currentPiece) {
//        Collection<ChessMove> diagonalMoves = new HashSet<>();
//        // top left incrementer
//
//        while (checkSpot(board, position)) {
//            diagonalMoves.add(new ChessMove(currentPosition, positionBooster(position), null));
//        }
//
//        return diagonalMoves;
//    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition, ChessPosition topRight, ChessPosition bottomRight, ChessPosition topLeft, ChessPosition bottomLeft) {
        Collection<ChessMove> bishopPossibles = new HashSet<>();
        ChessPiece currentPiece = board.getPiece(myPosition);

        // Checks top left diagonal
        while(checkSpot(board, topLeft)) {
            bishopPossibles.add(new ChessMove(myPosition, topLeft, null));
            System.out.print(topLeft.getRow());
            System.out.print(topLeft.getColumn());
            topLeft = new ChessPosition(topLeft.getRow() + 1, topLeft.getColumn() - 1);
            System.out.print("TL ");
        }
        topLeft = new ChessPosition(topLeft.getRow() - 1, topLeft.getColumn() + 1);
        if (checkEnemy(board, topLeft, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, topLeft, null));
            System.out.print("TL enem ");
        }

        // Checks top right diagonal
        while(checkSpot(board, topRight)) {
            bishopPossibles.add(new ChessMove(myPosition, topRight, null));
            System.out.print(topRight.getRow());
            System.out.print(topRight.getColumn());
            topRight = new ChessPosition(topRight.getRow() + 1, topRight.getColumn() + 1);
            System.out.print("TR ");
        }
        if (checkEnemy(board, topRight, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, topRight, null));
            System.out.print(topRight.getRow());
            System.out.print(topRight.getColumn());
            System.out.print("TR enem ");
        }

        // Checks bottom left diagonal
        while(checkSpot(board, bottomLeft)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomLeft, null));
            System.out.print(bottomLeft.getRow());
            System.out.print(bottomLeft.getColumn());
            bottomLeft = new ChessPosition(bottomLeft.getRow() - 1, bottomLeft.getColumn() - 1);
            System.out.print("BL ");
        }
        if (checkEnemy(board, bottomLeft, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomLeft, null));
            System.out.print("BL enem ");
        }
        // First BR: row 4 col 3
        // Second BR: row 3 col 4
        // Checks bottom right diagonal
        while(checkSpot(board, bottomRight)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomRight, null));
            System.out.print(bottomRight.getRow());
            System.out.print(bottomRight.getColumn());
            bottomRight = new ChessPosition(bottomRight.getRow() - 1, bottomRight.getColumn() + 1);
            System.out.print("BR ");
        }
        if (checkEnemy(board, bottomRight, currentPiece)) {
            bishopPossibles.add(new ChessMove(myPosition, bottomRight, null));
            System.out.print(bottomRight.getRow());
            System.out.print(bottomRight.getColumn());
            System.out.print("BR enem ");
        }

        return bishopPossibles;
    }

    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece currentPiece = board.getPiece(myPosition);
        Collection<ChessMove> pawnPossibles = new HashSet<>();
        ChessPosition top = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
        ChessPosition twoTop = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
        ChessPosition twoBottom = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
        ChessPosition topLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
        ChessPosition topRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
        ChessPosition bottom = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
        ChessPosition bottomLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
        ChessPosition bottomRight = new ChessPosition(myPosition.getRow() - 1 , myPosition.getColumn() + 1);

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
        } else if (checkSpot(board, bottom) && currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(bottom), null));
        }

        // Checks if pawn can move to a spot to capture enemy
        if (checkEnemy(board, topLeft, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(topLeft), null));
        }
        if (checkEnemy(board, topRight, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(topRight), null));
        }
        if (checkEnemy(board, bottomRight, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(bottomRight), null));
        }
        if (checkEnemy(board, bottomLeft, currentPiece) && currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            pawnPossibles.add(new ChessMove(myPosition, positionBooster(bottomLeft), null));
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

        if (currentPiece.getPieceType() == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        }
        if (currentPiece.getPieceType() == PieceType.BISHOP) {
            return bishopMoves(board, myPosition, topRight, bottomRight, topLeft, bottomLeft);
        }

        Collection<ChessMove> fail = new HashSet<>();
        return fail;
    }
}
