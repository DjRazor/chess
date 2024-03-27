package ui;

import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import server.ServerFacade;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_BG_COLOR_BLACK;

public class ChessClient {
    private LogState logState = LogState.OUT;
    private final ServerFacade facade;
    private final String serverURL;
    private String username = null;

    private static final String[] revLetters = {"h", "g", "f", "e", "d", "c", "b", "a"};
    private static final String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String[] backRow = {"R", "N", "B", "K", "Q", "B", "N", "R"};//{BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_KING, BLACK_QUEEN, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK};
    private static final String[] pawns = {"P", "P", "P", "P", "P", "P", "P", "P",};

    public ChessClient(String serverURL) {
        facade = new ServerFacade(serverURL);
        this.serverURL = serverURL;
    }

    public String eval(String input) throws DataAccessException {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "quit" -> "quit";
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout(params);
                case "creategame" -> createGame(params);
                case "listgames" -> listGames();
                case "joingame" -> joinGame(params);
                case "joinobserver" -> joinObserver(params);
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }
    public String register(String... params) throws DataAccessException {
        if (params.length == 3) {
            logState = LogState.IN;
            username = params[0];
            UserData userData = new UserData(params[0], params[1], params[2]);
            Object regRes = facade.register(userData);
            if (regRes.getClass().equals(AuthData.class)) {
                return "Successful register for user: " + params[0] + ".\n";
            } else {
                throw new DataAccessException("register error: response was not AuthData class, but rather: " + regRes.getClass());
            }
        }
        throw new DataAccessException("Expected 3 arguments, but " + params.length + " were given.");
    }
    public String login(String... params) {
        return null;
    }
    public String logout(String... params) throws DataAccessException {
        assertSignIn();
        return null;
    }
    public String createGame(String... params) throws DataAccessException {
        assertSignIn();
        return null;
    }
    public String listGames() throws DataAccessException {
        assertSignIn();
        return null;
    }
    public String joinGame(String... params) throws DataAccessException {
        assertSignIn();
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        drawBoard1(out);
        out.println("\u001B[0m");
        drawBoard2(out);

        // Resets all attributes to default
        out.println("\u001B[0m");
        return null;
    }
    public String joinObserver(String... params) throws DataAccessException {
        assertSignIn();
        return null;
    }
    public String help() {
        if (logState == LogState.OUT) {
            return """
                    Commands:
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    - help
                    
                   """;
        }
        return """
               Commands:
               - logout
               - createGame <gameName>
               - listGames
               - joinGame <color> <gameID>
               - joinObserver <gameID>
               - quit
               - help
               
               """;
    }
    private void assertSignIn() throws DataAccessException {
        if (logState == LogState.OUT) {
            throw new DataAccessException("You must sign in or register.");
        }
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
