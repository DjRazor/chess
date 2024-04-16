package ui;

import chess.ChessPiece;

import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class ChessClientHelper {
    public static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
    }
    public static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
    }
    public static void setGreen(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREEN);
    }
    public String convertPiece(ChessPiece piece) {
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            return "P";
        }
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
            return "B";
        }
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            return "R";
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
            return "N";
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            return "K";
        }
        if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
            return "Q";
        }
        return null;
    }

    public String convertPieceToString(String param) {
        String upperParam = param.toUpperCase();
        if (upperParam.equals("P")) {
            return "Pawn";
        }
        if (upperParam.equals("R")) {
            return "Rook";
        }
        if (upperParam.equals("N")) {
            return "Knight";
        }
        if (upperParam.equals("B")) {
            return "Bishop";
        }
        if (upperParam.equals("Q")) {
            return "Queen";
        }
        if (upperParam.equals("K")) {
            return "Knight";
        }
        return null;
    }

    public int convertLetterToInt(char letter) {
        int i;
        switch (letter) {
            case 'a' -> i = 1;
            case 'b' -> i = 2;
            case 'c' -> i = 3;
            case 'd' -> i = 4;
            case 'e' -> i = 5;
            case 'f' -> i = 6;
            case 'g' -> i = 7;
            case 'h' -> i = 8;
            default -> i = 0;
        }
        return i;
    }
}
