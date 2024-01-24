package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor startingColor = TeamColor.WHITE;
    public ChessGame() {

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
        throw new RuntimeException("Not implemented");
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
        // Setting all pawns
        for (int i = 0; i < 8; i++) {
            board.addPiece(new ChessPosition(1, i), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            board.addPiece(new ChessPosition(6, i), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        // Setting White pieces
        board.addPiece(new ChessPosition(0, 0), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        board.addPiece(new ChessPosition(0, 1), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        board.addPiece(new ChessPosition(0, 2), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        board.addPiece(new ChessPosition(0, 3), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KING));
        board.addPiece(new ChessPosition(0, 4), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        board.addPiece(new ChessPosition(0, 5), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        board.addPiece(new ChessPosition(0, 6), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        board.addPiece(new ChessPosition(0, 7), new ChessPiece(TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        // Setting Black pieces
        board.addPiece(new ChessPosition(7, 0), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        board.addPiece(new ChessPosition(7, 1), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        board.addPiece(new ChessPosition(7, 2), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        board.addPiece(new ChessPosition(7, 3), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KING));
        board.addPiece(new ChessPosition(7, 4), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        board.addPiece(new ChessPosition(7, 5), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        board.addPiece(new ChessPosition(7, 6), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        board.addPiece(new ChessPosition(7, 7), new ChessPiece(TeamColor.BLACK, ChessPiece.PieceType.ROOK));

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
