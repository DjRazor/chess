package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private static final int SQR_SIZE_IN_CHARS = 1;
    private static final String[] revLetters = {"h", "g", "f", "e", "d", "c", "b", "a"};
    private static final String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private final ChessClient client;
    public Repl(String serverURL) {
        client = new ChessClient(serverURL);
    }
    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to the Chess Server. Please sign in or register for an account.\n");
        System.out.print(client.help());

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        drawHeader(out, "LET THE GAMES BEGIN");

        Scanner scanner = new Scanner(System.in);
        var input = "";
        while (!input.equals("quit")) {
            String line = scanner.nextLine();
            try {
                input = client.eval(line);
            } catch (Throwable ex) {
                System.out.print(ex.getMessage());
            }
        }
        System.out.println();
    }
    private static void drawHeader(PrintStream out, String string) {
        out.print(SET_BG_COLOR_DARK_GREY);
        out.print(SET_TEXT_COLOR_MAGENTA);
        out.print(string);
    }
    private static void drawHeaderRev(PrintStream out) {

    }
    private static void setWhite(PrintStream out) {

    }
    private static void setBlack(PrintStream out) {

    }
}
