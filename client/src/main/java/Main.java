import chess.*;
import ui.EscapeSequences;
import ui.Repl;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        String serverURL = "https://localhost:8080";

        /*
        System.out.print("\u001b[35;100m");
        System.out.printf("   %s   ", String.join(" ", revLetters));
        System.out.print("\u001b[107m");
        System.out.print("\n");
        System.out.print("\u001b[35;100m");
        System.out.printf("   %s   ", String.join(" ", letters));
        System.out.print("\u001b[107m");
        System.out.printf(" %d ", result);
         */

        new Repl(serverURL).run();


    }
}