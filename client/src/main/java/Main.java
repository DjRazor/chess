import chess.*;
import ui.EscapeSequences;
import ui.Repl;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        String serverURL = "http://localhost:8080";

        new Repl(serverURL).run();

    }
}