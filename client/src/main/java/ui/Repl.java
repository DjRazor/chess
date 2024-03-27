package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private static final String[] revLetters = {"h", "g", "f", "e", "d", "c", "b", "a"};
    private static final String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String[] backRow = {"R", "N", "B", "K", "Q", "B", "N", "R"};//{BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_KING, BLACK_QUEEN, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK};
    private static final String[] pawns = {"P", "P", "P", "P", "P", "P", "P", "P",};
    private final ChessClient client;
    public Repl(String serverURL) {
        client = new ChessClient(serverURL);
    }
    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to the Chess Server. Please sign in or register for an account.\n");
        System.out.print(client.help());

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        drawBoard1(out);
        out.println();
        drawBoard2(out);

        out.print(ERASE_SCREEN);

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
    private static void drawBoard1(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_WHITE);

        drawHeaderRev(out);

        printWhiteStart(out, true, backRow, 1, SET_TEXT_COLOR_RED);
        printWhiteStart(out, false, pawns, 2, SET_TEXT_COLOR_RED);

        printWhiteStart(out, true, null, 3, null);
        printWhiteStart(out, false, null, 4, null);
        printWhiteStart(out, true, null, 5, null);
        printWhiteStart(out, false, null, 6, null);

        printWhiteStart(out, true, pawns, 7, SET_TEXT_COLOR_BLUE);
        printWhiteStart(out, false, backRow, 8, SET_TEXT_COLOR_BLUE);

        drawHeader(out);
    }
    private static void drawBoard2(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_WHITE);

        drawHeader(out);

        printWhiteStart(out, true, backRow, 8, SET_TEXT_COLOR_BLUE);
        printWhiteStart(out, false, pawns, 7, SET_TEXT_COLOR_BLUE);

        printWhiteStart(out, true, null, 6, null);
        printWhiteStart(out, false, null, 5, null);
        printWhiteStart(out, true, null, 4, null);
        printWhiteStart(out, false, null, 3, null);

        printWhiteStart(out, true, pawns, 2, SET_TEXT_COLOR_RED);
        printWhiteStart(out, false, backRow, 1, SET_TEXT_COLOR_RED);

        drawHeaderRev(out);
    }
    private static void drawHeader(PrintStream out) {
        out.printf("    %s\n", String.join("  ", letters));
    }
    private static void drawHeaderRev(PrintStream out) {
        out.printf("    %s\n", String.join("  ", revLetters));
    }
    private static void drawBackRow(PrintStream out, String color) {
        if (color.equals("WHITE")) {

        }
        for (String x : backRow) {

        }
        setBlack(out);
        out.print(" R ");
        setWhite(out);
        out.print("N");
    }
    private static void printWhiteStart(PrintStream out, boolean white, String[] args, int num, String textColor) {
        boolean whiteSpot = white;
        out.print(" "+ num + " ");

        if (textColor != null) {
            out.print(textColor);
        }

        for (int i = 0; i < 8; i++) {
            // Alternates white and black spots
            if (whiteSpot) {
                setWhite(out);
            } else {
                setBlack(out);
            }
            whiteSpot = !whiteSpot;

            // Prints piece if args are given
            if (args != null) {
                out.print(" " + args[i] + " ");
            } else {
                out.print("   ");
            }
        }
        // Resets background color
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_WHITE);
        out.println(" " + num + " ");
    }
    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
    }
    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
    }
}
