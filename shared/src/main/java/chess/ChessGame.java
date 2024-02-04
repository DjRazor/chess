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
    private ChessBoard testingBoard = currentBoard.clone();
    public ChessGame() {

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
        // Re-initialize testingBoard
        // testingBoard = currentBoard.clone();
        if (startPosition == null) {
            return null;
        }
        ChessPiece currentPiece = currentBoard.getPiece(startPosition);
        TeamColor currentPieceColor = currentPiece.getTeamColor();
        Collection<ChessMove> possibleMoves = currentPiece.pieceMoves(currentBoard, startPosition);

        for (ChessMove x : possibleMoves) {
            try {
                makeMove(x);
            } catch (InvalidMoveException e) {
                possibleMoves.remove(x);
            }
        }


        return possibleMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        testingBoard = currentBoard.clone();
        ChessPiece currentPiece = currentBoard.getPiece(move.getStartPosition());

        // Checks if it is the current piece's color's turn
        if (currentColor == currentBoard.getPiece(move.getStartPosition()).getTeamColor()) {
            testingBoard.addPiece(move.getEndPosition(), currentPiece);
            testingBoard.resetPosition(move.getStartPosition());
            if (isInCheck(currentColor)) {
                throw new InvalidMoveException("Invalid move");
            } else {
                currentBoard.addPiece(move.getEndPosition(), currentPiece);
                currentBoard.resetPosition(move.getStartPosition());
                if (currentColor == TeamColor.WHITE) {
                    setTeamTurn(TeamColor.BLACK);
                } else {
                    setTeamTurn(TeamColor.WHITE);
                }
            }
        } else {
            throw new InvalidMoveException("Invalid move");
        }

        // Sets TeamTurn to opposing color


    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //ChessPiece king;
        testingBoard = currentBoard.clone();
        int row = 0;
        int col = 0;

        // Finds where the king of the desired team color is
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece piece = testingBoard.getPiece(new ChessPosition(i, j));
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
                ChessPiece pieceInQ = testingBoard.getPiece(new ChessPosition(i, j));
                if (pieceInQ != null && pieceInQ.getTeamColor() != teamColor) {
                    Collection<ChessMove> pieceInQMoves = pieceInQ.pieceMoves(testingBoard, new ChessPosition(i, j));
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
        return currentBoard;
    }
}
