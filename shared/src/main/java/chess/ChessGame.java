package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard currentBoard = new ChessBoard();
    private TeamColor startingColor = TeamColor.WHITE;
    public ChessGame() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame chessGame)) return false;
        return Objects.deepEquals(currentBoard, chessGame.currentBoard) && startingColor == chessGame.startingColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBoard, startingColor);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentBoard=" + currentBoard +
                ", startingColor=" + startingColor +
                '}';
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return startingColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        startingColor = team;
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //ChessPiece king;
        int row = 0;
        int col = 0;

        // Finds where the king of the desired team color is
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = currentBoard.getPiece(new ChessPosition(i, j));
                System.out.println(piece);
                if (piece != null) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                        if (piece.getTeamColor() == teamColor) {
                            //king = currentBoard.getPiece(new ChessPosition(i, j));
                            row = i;
                            col = j;
                        }
                    }
                }
            }
        }
        ChessPosition kingPosition = new ChessPosition(row, col);
        System.out.println(kingPosition);

        // Checks the end positions of all enemy pieces
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece pieceInQ = currentBoard.getPiece(new ChessPosition(i, j));
                if (pieceInQ != null && pieceInQ.getTeamColor() != teamColor) {
                    Collection<ChessMove> pieceInQMoves = pieceInQ.pieceMoves(currentBoard, new ChessPosition(i, j));
                    for (ChessMove x : pieceInQMoves) {
                        if (x.getEndPosition() == kingPosition) {
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
     * no valid moves
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
        currentBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented");
    }
}
