package ui;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static ui.EscapeSequences.*;

public class ChessClient {
    private LogState logState = LogState.OUT;
    private final ChessClientHelper chessClientHelper = new ChessClientHelper();
    private GameState gameState = GameState.OUT_OF_GAME;
    private final ServerFacade facade;
    private WebSocketFacade ws;
    private final String serverURL;
    private String username = null;
    private String authToken;
    private ChessGame.TeamColor teamColor;
    private GameData currentGameData;
    private HashSet<ChessPosition> showEnds = new HashSet<>();
    private boolean showMovesEnabled;
    private final NotificationHandler notificationHandler;
    private Map<Integer, Integer> gameList;
    private static final String[] revLetters = {"h", "g", "f", "e", "d", "c", "b", "a"};
    private static final String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};

    public ChessClient(String serverURL, NotificationHandler notificationHandler) {
        this.serverURL = serverURL;
        facade = new ServerFacade(serverURL);
        this.notificationHandler = notificationHandler;
    }

    public String eval(String input) {
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
                case "clear" -> clear();
                case "redraw" -> redraw();
                case "showmoves" -> showMoves(params);
                case "resign" -> resign();
                case "leave" -> leave();
                case "makemove" -> makeMove(params);
                default -> unknown();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
    public String register(String... params) throws Exception {
        assertOutOfGame();
        if (params.length == 3) {
            UserData userData = new UserData(params[0], params[1], params[2]);
            AuthData regRes = facade.register(userData);
            logState = LogState.IN;
            username = params[0];
            authToken = regRes.authToken();
            return "Successful register for user: " + params[0] + "\n";
        }
        throw new Exception("Expected 3 arguments, but " + params.length + " were given.");
    }
    public String login(String... params) throws Exception {
        assertOutOfGame();
        if (params.length == 2) {
            JsonObject loginInfo = new JsonObject();
            loginInfo.addProperty("username", params[0]);
            loginInfo.addProperty("password", params[1]);
            AuthData loginRes = facade.login(loginInfo);
            logState = LogState.IN;
            username = params[0];
            authToken = loginRes.authToken();
            return "Successful login for user: " + params[0] + "\n";
        }
        throw new Exception("Expected 2 arguments, but received " + params.length);
    }
    public String logout() throws Exception {
        assertOutOfGame();
        assertSignIn();
        facade.logout(authToken);
        authToken = null;
        String temp = username;
        username = null;
        teamColor = null;
        logState = LogState.OUT;
        return temp + " signed out.";
    }
    public String createGame(String... params) throws Exception {
        assertOutOfGame();
        assertSignIn();
        if (params.length == 1) {
            JsonObject gameName = new JsonObject();
            gameName.addProperty("gameName", params[0]);
            Object game = facade.createGame(gameName, authToken);
            if (game.getClass().equals(JsonObject.class)) {
                String gameList = listGames();
                return "Successfully created game: " + gameName.get("gameName").getAsString() + "\n" + gameList;
            }
            return game.toString();
        }
        throw new Exception("Expected 1 argument, but " + params.length + " were given.\n");
    }
    public String listGames() throws Exception {
        assertSignIn();
        assertOutOfGame();

        int count = 1;
        Map<Integer, Integer> listNumGameID = new HashMap<>();
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
            listNumGameID.put(count, gameID);
            count += 1;
        }
        gameList = listNumGameID;
        String gamesString = "";
        if (gamesAsString.isEmpty()) {
            gamesString += "No games currently.";
        }
        for (String game : gamesAsString) {
            gamesString += game;
        }
        return gamesString;
    }

    private void setCurrentGameData(String gameID) throws Exception {
        JsonObject games = facade.listGames(authToken);
        Integer newGameID = Integer.parseInt(gameID);
        JsonArray gamesArray = games.getAsJsonArray("games");
        for (JsonElement elem : gamesArray) {
            JsonObject gameElem = elem.getAsJsonObject();
            if (!gameElem.has("gameID")) {
                throw new Exception("wack, no gameID!\n");
            }
            if (Integer.parseInt(gameElem.get("gameID").getAsString()) == newGameID) {
                currentGameData = new Gson().fromJson(gameElem, GameData.class);
            }
        }
    }

    public String joinGame(String... params) throws Exception {
        assertSignIn();
        assertOutOfGame();
        listGames(); //generates list of Games with ID num if not present already

        if (params.length == 2) {
            if (params[0].equalsIgnoreCase("black") || params[0].equalsIgnoreCase("white")) {
                Integer gameNum = Integer.parseInt(params[1]);
                Integer correlatingGameID = null;
                for (Map.Entry<Integer, Integer> entry : gameList.entrySet()) {
                    if (entry.getKey().equals(gameNum)) {
                        correlatingGameID = entry.getValue();
                        break;
                    }
                }
                if (correlatingGameID == null) {
                    throw new Exception("Game number not found. Please try again.");
                }
                setCurrentGameData(String.valueOf(correlatingGameID));
                assertNotResigned();

                JsonObject joinStatus = facade.joinGame(correlatingGameID, getTeamColor(params[0]), authToken);
                ws = new WebSocketFacade(serverURL, notificationHandler, authToken);
                if (joinStatus.entrySet().isEmpty()) {
                    gameState = GameState.IN_GAME;
                    String tempTeamColor = params[0].toUpperCase();
                    setUserTeamColor(tempTeamColor);

                    ws.joinPlayer(correlatingGameID, teamColor);
                    return "Successfully joined " + params[0].toUpperCase() + " in game " + gameNum + "\n";
                }
                ws.joinPlayer(correlatingGameID, null);
            }
            //System.out.println("sent null playerColor in joinPlayer");
            return "Invalid color. Please try again.\n";
        }
        throw new Exception("Expected 2 arguments but " + params.length + " were given.\n");
    }
    public String joinObserver(String... params) throws Exception {
        assertSignIn();
        assertOutOfGame();
        listGames();

        if (params.length == 1) {
            Integer gameNum = Integer.parseInt(params[0]);
            Integer correlatingGameID = null;
            for (Map.Entry<Integer, Integer> entry : gameList.entrySet()) {
                if (entry.getKey().equals(gameNum)) {
                    correlatingGameID = entry.getValue();
                    break;
                }
            }
            if (correlatingGameID == null) {
                throw new Exception("Game number not found. Please try again.");
            }
            setCurrentGameData(String.valueOf(correlatingGameID));
            assertNotResigned();

            JsonObject joinStatus = facade.joinGame(correlatingGameID, ChessGame.TeamColor.NONE, authToken);
            if (joinStatus.entrySet().isEmpty()) {
                gameState = GameState.IN_GAME;
                ws = new WebSocketFacade(serverURL, notificationHandler, authToken);
                teamColor = ChessGame.TeamColor.NONE;
                ws.joinObserver(correlatingGameID);
                return "Observing game " + gameNum;
            }
        }
        throw new Exception("Expected 1 argument but " + params.length + " were given.\n");
    }
    public String help() {
        if (logState == LogState.OUT) {
            return """
                    Start Up Commands:
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    - help
                   """;
        } else if (gameState == GameState.IN_GAME) {
            return """
                    Game Commands:
                    - redraw
                    - showMoves <spot>
                    - makeMove <start> <end>
                    - resign
                    - leave
                    - help
                    """;
        }
        return """
               Lobby Commands:
               - logout
               - createGame <gameName>
               - listGames
               - joinGame <color> <game #>
               - joinObserver <game #>
               - quit
               - help
               """;
    }

    /* In Game Methods */
    public String redraw() throws Exception {
        assertSignIn();
        assertInGame();
        setCurrentGameData(String.valueOf(currentGameData.gameID()));
        assertNotResigned();

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        if (teamColor.equals(ChessGame.TeamColor.WHITE)) {
            drawBoard2(out);
        } else if (teamColor.equals(ChessGame.TeamColor.BLACK)) {
            drawBoard1(out);
        } else { // Observer
            drawBoard1(out);
            out.println("\u001B[0m");
            drawBoard2(out);
        }

        // Resets all attributes to default
        out.println("\u001B[0m");
        return "Board drawn.";
    }

    public String showMoves(String ...params) throws Exception {
        assertSignIn();
        assertInGame();
        setCurrentGameData(String.valueOf(currentGameData.gameID()));
        assertNotResigned();

        if (params.length == 1) {
            int col = chessClientHelper.convertLetterToInt(params[0].charAt(0));
            int row = Integer.parseInt(String.valueOf(params[0].charAt(1)));
            boolean emptySpot = currentGameData.game().getBoard().getPiece(new ChessPosition(row, col)) == null;
            if (emptySpot) {
                return "No piece found at " + params[0] + "\n";
            }
            Collection<ChessMove> moves = currentGameData.game().validMoves(new ChessPosition(row, col));
            if (moves.isEmpty()) {
                return "No valid moves for " + params[0] + "\n";
            }

            showMovesEnabled = true;
            showEnds = new HashSet<>();
            for (ChessMove move : moves) {
                showEnds.add(move.getEndPosition());
            }
            redraw();
            showMovesEnabled = false;
            return "Moves found.\n";
        }
        throw new Exception("Expected 1 argument but " + params.length + " were given.");
    }

    public String makeMove(String ...params) throws Exception {
        assertSignIn();
        assertInGame();

        // Pseudocode:
        /*
        Get user input and convert into needed chess object (piece, move) (get color from board or game)
        mAkEmOvE

        FOR PAWN PROMO:
        ask for promo with getPiecePromo (get back from ws), then send THAT into WSF
         */
        setCurrentGameData(String.valueOf(currentGameData.gameID()));
        assertNotResigned();
        Character[] charLetters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

        // JUST create ChessMove, pass in to WSF
        if (params.length == 2) { // Checks param length
            if (params[0].length() == 2 && params[1].length() == 2) { // Checks if valid spot
                String start = params[0].toLowerCase();
                String end = params[1].toLowerCase();
                boolean validLetter = false;
                boolean validLetter2 = false;
                for (Character letter : charLetters) {
                    if (start.charAt(0) == letter) {
                        validLetter = true;
                    }
                    if (end.charAt(0) == letter) {
                        validLetter2 = true;
                    }
                }
                if (validLetter && validLetter2) {
                    int startRow = Integer.parseInt(String.valueOf(start.charAt(1)));
                    int endRow = Integer.parseInt(String.valueOf(end.charAt(1)));
                    int startCol = chessClientHelper.convertLetterToInt(start.charAt(0));
                    int endCol = chessClientHelper.convertLetterToInt(end.charAt(0));

                    if (startRow <= 8 && startRow >= 1 && endRow <= 8 && endRow >= 1) {
                        ChessPosition startPos = new ChessPosition(startRow, startCol);
                        ChessPosition endPos = new ChessPosition(endRow, endCol);
                        ChessMove move = new ChessMove(startPos, endPos, null);
                        if (currentGameData.game().getBoard().getPiece(startPos) == null) {
                            throw new Exception("Can't move null spot");
                        }
                        if (currentGameData.game().isInCheckmate(ChessGame.TeamColor.BLACK)) {
                            throw new Exception("Game over. Black in checkmate");
                        }
                        if (currentGameData.game().isInCheckmate(ChessGame.TeamColor.WHITE)) {
                            throw new Exception("Game over. White in checkmate");
                        }
                        if (currentGameData.game().isInStalemate(ChessGame.TeamColor.WHITE)) {
                            throw new Exception("Game over. White in stalemate");
                        }
                        if (currentGameData.game().isInStalemate(ChessGame.TeamColor.BLACK)) {
                            throw new Exception("Game over. White in stalemate");
                        }
                        if (currentGameData.game().getTeamTurn().equals(ChessGame.TeamColor.NONE)) {
                            throw new Exception("Game has been resigned");
                        }

                        ws.makeMove(currentGameData.gameID(), move);

                        //return "You moved " + params[0] + " to " + params[1];
                        return "\b";
                    }
                    throw new Exception("Invalid number in move.");
                }
                throw new Exception("Invalid letter in move.");
            }
            throw new Exception("Invalid spot. Please try again.");
        }
        throw new Exception("Expected 2 arguments but " + params.length + " were given.\n");
    }

    public String resign() throws Exception {
        assertSignIn();
        assertInGame();

        setCurrentGameData(String.valueOf(currentGameData.gameID()));
        assertNotResigned();

        if (currentGameData.game().getTeamTurn().equals(ChessGame.TeamColor.NONE)) {
            return "Game has been resigned.";
        }
        Scanner resScan = new Scanner(System.in);
        while (true) {
            System.out.println("Are you sure you want to resign? (Y/N)");
            String line = resScan.nextLine();
            line = line.toUpperCase();
            switch (line) {
                case "Y" -> {
                    ws.resign(currentGameData.gameID());
                    return "\b";
                }
                case "N" -> {
                    return "Continuing game.\n";
                }
            }
        }
    }

    public String leave() throws Exception {
        assertSignIn();
        assertInGame();

        // Add code to update game for when player leaves to show empty spot
        // This could be done through the updateGame method
        GameData editedGame;
        if (teamColor == ChessGame.TeamColor.BLACK) {
            editedGame = new GameData(currentGameData.gameID(), currentGameData.whiteUsername(), null, currentGameData.gameName(),currentGameData.game());
        }
        else if (teamColor == ChessGame.TeamColor.WHITE){
            editedGame = new GameData(currentGameData.gameID(), null, currentGameData.blackUsername(), currentGameData.gameName(),currentGameData.game());
        }
        else if (teamColor == null) {
            editedGame = currentGameData;
        }
        else {
            throw new Exception("leave teamColor error");
        }
        facade.updateGame(editedGame, authToken);
        ws.leave(currentGameData.gameID());
        ws = null;
        gameState = GameState.OUT_OF_GAME;
        currentGameData = null;
        teamColor = null;
        return "Left game.\n";
    }

    public String unknown() {
        return "Unknown command. Please enter a valid command.\n" + help();
    }
    public String clear() throws Exception {
        assertSignIn();
        facade.clear();
        username = null;
        authToken = null;
        logState = LogState.OUT;
        gameState = GameState.OUT_OF_GAME;
        teamColor = null;
        currentGameData = null;
        return "Database has been cleared\n";
    }
    private void assertSignIn() throws Exception {
        if (logState == LogState.OUT) {
            throw new Exception("You must sign in or register.");
        }
    }

    private void assertInGame() throws Exception {
        if (gameState == GameState.OUT_OF_GAME) {
            throw new Exception("You must be in a game to use this command.");
        }
    }

    private void assertNotResigned() throws Exception {
        if (currentGameData.game().getTeamTurn().equals(ChessGame.TeamColor.NONE)) {
            throw new Exception("The game has ended.\n");
        }
    }

    private void assertOutOfGame() throws Exception {
        if (gameState == GameState.IN_GAME) {
            throw new Exception("You cannot use this command while in game.");
        }
    }

    private void drawBoard1(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_WHITE);

        drawHeaderRev(out);
        for (int i = 1; i <= 8; i++) {
            boolean black = true;
            if (i % 2 == 0) {
                black = false;
            }
            printWhiteStart(out, black, i, true);
        }
        drawHeaderRev(out);
    }
    private void drawBoard2(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_WHITE);

        drawHeader(out);
        for (int i = 8; i >= 1; i--) {
            boolean white = false;
            if (i % 2 == 0) {
                white = true;
            }
            printWhiteStart(out, white, i, false);
        }
        drawHeader(out);
    }
    private static void drawHeader(PrintStream out) {
        out.printf("    %s\n", String.join("  ", letters));
    }
    private static void drawHeaderRev(PrintStream out) {
        out.printf("    %s\n", String.join("  ", revLetters));
    }
    private void printWhiteStart(PrintStream out, boolean white, int num, boolean rev) {
        boolean whiteSpot = white;
        out.print(" "+ num + " ");

        for (int i = 1; i < 9; i++) {
            // Alternates white and black spots
            if (!rev && showMovesEnabled && showEnds.contains(new ChessPosition(num, i))) {
                chessClientHelper.setGreen(out);
            }
            else if (rev && showMovesEnabled && showEnds.contains(new ChessPosition(num, 9 - i))) {
                chessClientHelper.setGreen(out);
            }
            else if (whiteSpot) {
                chessClientHelper.setWhite(out);
            } else {
                chessClientHelper.setBlack(out);
            }
            whiteSpot = !whiteSpot;

            // Prints piece if args are given
            ChessPosition currentPos;
            if (!rev) {
                currentPos = new ChessPosition(num, i);
            } else {
                currentPos = new ChessPosition(num, 9 - i);
            }
            ChessPiece currentPiece = currentGameData.game().getBoard().getPiece(currentPos);
            if (currentPiece != null) {
                if (currentPiece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                    out.print(SET_TEXT_COLOR_BLUE);
                    out.print(" " + chessClientHelper.convertPiece(currentPiece) + " ");
                } else if (currentPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                    out.print(SET_TEXT_COLOR_RED);
                    out.print(" " + chessClientHelper.convertPiece(currentPiece) + " ");
                }
            } else {
                out.print("   ");
            }
        }
        // Resets background color
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_WHITE);
        out.println(" " + num + " ");
    }
    private void setUserTeamColor(String color) throws Exception {
        if (color.equals("BLACK")) {
            teamColor = ChessGame.TeamColor.BLACK;
        } else if (color.equals("WHITE")) {
            teamColor = ChessGame.TeamColor.WHITE;
        } else {
            throw new Exception("Error setting User team color.");
        }
    }

    private ChessGame.TeamColor getTeamColor(String color) {
        color = color.toUpperCase();
        if (color.equals("WHITE")) {
            return ChessGame.TeamColor.WHITE;
        } else if (color.equals("BLACK")) {
            return ChessGame.TeamColor.BLACK;
        } else if (color.equals("NONE")) {
            return ChessGame.TeamColor.NONE;
        }
        return null;
    }
}