package ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.Request;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;

import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_BG_COLOR_BLACK;

public class ChessClient {
    private LogState logState = LogState.OUT;
    private final ServerFacade facade;
    private final String serverURL;
    private String username = null;
    private String authToken;

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
                case "logout" -> logout();
                case "creategame" -> createGame(params);
                case "listgames" -> listGames();
                case "joingame" -> joinGame(params);
                case "joinobserver" -> joinObserver(params);
                case "help", "", " " -> help();
                default -> unknown();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }
    public String register(String... params) throws DataAccessException {
        if (params.length == 3) {
            UserData userData = new UserData(params[0], params[1], params[2]);
            Object regRes = facade.register(userData);
            if (regRes.getClass().equals(AuthData.class)) {
                logState = LogState.IN;
                username = params[0];
                authToken = ((AuthData) regRes).authToken();
                return "Successful register for user: " + params[0] + ".\n";
            } else {
                return regRes.toString();
            }
        }
        throw new DataAccessException("Expected 3 arguments, but " + params.length + " were given.");
    }
    public String login(String... params) throws DataAccessException{
        if (params.length == 2) {
            JsonObject loginInfo = new JsonObject();
            loginInfo.addProperty("username", params[0]);
            loginInfo.addProperty("password", params[1]);
            Object loginRes = facade.login(loginInfo);
            if (loginRes.getClass().equals(AuthData.class)) {
                logState = LogState.IN;
                username = params[0];
                authToken = ((AuthData) loginRes).authToken();
                return "Successful login for user: " + params[0] + ".\n";
            } else {
                return loginRes.toString();
            }
        }
        throw new DataAccessException("Expected 2 arguments, but received " + params.length);
    }
    public String logout() throws DataAccessException {
        assertSignIn();
        facade.logout(authToken);
        authToken = null;
        String temp = username;
        username = null;
        logState = LogState.OUT;
        return temp + " signed out.\n";
    }
    public String createGame(String... params) throws DataAccessException {
        assertSignIn();
        if (params.length == 1) {
            JsonObject gameName = new JsonObject();
            gameName.addProperty("gameName", params[0]);
            Object game = facade.createGame(gameName, authToken);
            if (game.getClass().equals(GameData.class)) {
                return "Successfully created game " + gameName;
            }
            return game.toString();
        }
        throw new DataAccessException("Expected 1 argument, but " + params.length + " were given.\n");
    }
    public String listGames() throws DataAccessException {
        assertSignIn();

        int count = 1;
        ArrayList<String> gamesAsString = new ArrayList<>();

        JsonObject games = facade.listGames(authToken);
        JsonArray gamesArray = games.getAsJsonArray("games");

        for (JsonElement element : gamesArray) {
            JsonObject gameElem = element.getAsJsonObject();

            int gameID = gameElem.get("gameID").getAsInt();
            String gameName = gameElem.get("gameName").getAsString();
            String whiteUsername;
            if (gameElem.get("whiteUsername") != null) {
                whiteUsername = gameElem.get("whiteUsername").getAsString();
            } else {
                whiteUsername = "empty";
            }
            String blackUsername;
            if (gameElem.get("blackUsername") != null) {
                blackUsername = gameElem.get("blackUsername").getAsString();
            } else {
                blackUsername = "empty";
            }

            String game = """
                    %s:
                    Game ID: %d
                    Game Name: %s
                    White player: %s
                    Black player: %s
                    
                    """.formatted(count, gameID, gameName, whiteUsername, blackUsername);
            gamesAsString.add(game);
            count += 1;
        }
        String gamesString = "";
        if (gamesAsString.isEmpty()) {
            gamesString += "No games currently.\n";
        }
        for (String game : gamesAsString) {
            gamesString += game;
        }
        return gamesString;
    }
    public String joinGame(String... params) throws DataAccessException {
        assertSignIn();

        if (params.length == 2) {
            JsonObject joinStatus = facade.joinGame(Integer.parseInt(params[1]), params[0], authToken);
            if (joinStatus.entrySet().isEmpty()) {
                // Print boards
                PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
                out.print(ERASE_SCREEN);

                drawBoard1(out);
                out.println("\u001B[0m");
                drawBoard2(out);

                // Resets all attributes to default
                out.println("\u001B[0m");

                return "Successfully joined " + params[0].toUpperCase() + " in game " + params[1] + "\n";
            }
        }
        throw new DataAccessException("Expected 2 arguments but " + params.length + " were given.\n");
    }
    public String joinObserver(String... params) throws DataAccessException {
        assertSignIn();
        if (params.length == 1) {
            JsonObject joinStatus = facade.joinGame(Integer.parseInt(params[0]), null, authToken);
            if (joinStatus.entrySet().isEmpty()) {
                // Print boards
                PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
                out.print(ERASE_SCREEN);

                drawBoard1(out);
                out.println("\u001B[0m");
                drawBoard2(out);

                // Resets all attributes to default
                out.println("\u001B[0m");

                return "Observing game " + params[0];
            }
        }
        throw new DataAccessException("Expected 1 argument but " + params.length + " were given.\n");
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
    public String unknown() {
        return "Unknown command. Please enter a valid command.\n" + help();
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
