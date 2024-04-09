package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard currentBoard = new ChessBoard();
    private TeamColor currentColor = TeamColor.WHITE;
    public ChessGame() {
        currentBoard.resetBoard();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame chessGame)) return false;
        return Objects.deepEquals(currentBoard, chessGame.currentBoard) && currentColor == chessGame.currentColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBoard, currentColor);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentBoard=" + currentBoard +
                ", currentColor=" + currentColor +
                '}';
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentColor = team;
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
        if (startPosition == null) {
            return null;
        }
        ChessBoard testingBoard;
        ChessPiece currentPiece = currentBoard.getPiece(startPosition);
        Collection<ChessMove> possibleMoves = currentPiece.pieceMoves(currentBoard, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove x : possibleMoves) {
            testingBoard = currentBoard.clone();
            currentBoard.addPiece(x.getEndPosition(), currentPiece);
            currentBoard.resetPosition(x.getStartPosition());
            if (!isInCheck(currentPiece.getTeamColor())) {
                validMoves.add(x);
            }
            currentBoard = testingBoard;
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece currentPiece = currentBoard.getPiece(move.getStartPosition());
        Collection<ChessMove> plausibleMoves = validMoves(move.getStartPosition());
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();

        // If the move requested isn't a possible option, throw exception
        if (!plausibleMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        // Checks if it is the current piece's color's turn
        if (currentColor == currentBoard.getPiece(move.getStartPosition()).getTeamColor()) {
            currentBoard.addPiece(move.getEndPosition(), currentPiece);
            currentBoard.resetPosition(move.getStartPosition());
            if (isInCheck(currentColor)) {
                currentBoard.addPiece(move.getStartPosition(), currentPiece);
                currentBoard.resetPosition(move.getEndPosition());
                throw new InvalidMoveException("Invalid move");
            } else {
                // Determines if pawn promotion is needed
                if (currentPiece.getPieceType() == ChessPiece.PieceType.PAWN && currentColor == TeamColor.WHITE &&
                        move.getEndPosition().getRow() == 8) {
                    currentBoard.resetPosition(move.getEndPosition());
                    currentBoard.addPiece(move.getEndPosition(), new ChessPiece(TeamColor.WHITE, promotionPiece));
                }
                if (currentPiece.getPieceType() == ChessPiece.PieceType.PAWN && currentColor == TeamColor.BLACK &&
                        move.getEndPosition().getRow() == 1) {
                    currentBoard.resetPosition(move.getEndPosition());
                    currentBoard.addPiece(move.getEndPosition(), new ChessPiece(TeamColor.BLACK, promotionPiece));
                }

                // Sets TeamTurn to opposing color
                if (currentColor == TeamColor.WHITE) {
                    setTeamTurn(TeamColor.BLACK);
                } else {
                    setTeamTurn(TeamColor.WHITE);
                }
            }
        } else {
            throw new InvalidMoveException("Other team's turn");
        }

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        int row = 0;
        int col = 0;

        // Finds where the king of the desired team color is
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = currentBoard.getPiece(new ChessPosition(i, j));
                if (piece != null) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                        if (piece.getTeamColor() == teamColor) {
                            row = i;
                            col = j;
                        }
                    }
                }
            }
        }
        ChessPosition kingPosition = new ChessPosition(row, col);

        // Checks the end positions of all enemy pieces, returns true if one matches king position
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece pieceInQ = currentBoard.getPiece(new ChessPosition(i, j));
                if (pieceInQ != null && pieceInQ.getTeamColor() != teamColor) {
                    Collection<ChessMove> pieceInQMoves = pieceInQ.pieceMoves(currentBoard, new ChessPosition(i, j));
                    for (ChessMove x : pieceInQMoves) {
                        if (x.getEndPosition().getRow() == kingPosition.getRow() && x.getEndPosition().getColumn() == kingPosition.getColumn()) {
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
        if (isInCheck(teamColor) && isInStalemate(teamColor)) {
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece pieceInQ = currentBoard.getPiece(new ChessPosition(i, j));
                if (pieceInQ != null && pieceInQ.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceInQMoves = pieceInQ.pieceMoves(currentBoard, new ChessPosition(i, j));
                    for (ChessMove x : pieceInQMoves) {
                        ChessBoard testingBoard = currentBoard.clone();
                        currentBoard.addPiece(x.getEndPosition(), pieceInQ);
                        currentBoard.resetPosition(x.getStartPosition());
                        if (!isInCheck(teamColor)) {
                            return false;
                        }
                        currentBoard = testingBoard;
                    }
                }
            }
        }
        return true;
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
        return currentBoard;
    }
}
