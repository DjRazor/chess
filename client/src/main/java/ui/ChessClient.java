package ui;

import dataAccess.DataAccessException;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ChessClient {
    private LogState logState = LogState.OUT;

    public String eval(String input) throws DataAccessException {
        var tokens = input.toLowerCase().split( " ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "quit" -> "quit";
            case "register" -> register(params);
            case "login" -> login(params);
            case "logout" -> logout(params);
            case "createGame" -> createGame(params);
            case "listGames" -> listGames();
            case "joinGame" -> joinGame(params);
            case "joinObserver" -> joinObserver(params);
            default -> help();
        };
    }
    public String register(String... params) {
        return null;
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
        return null;
    }
    public String joinObserver(String... params) throws DataAccessException {
        assertSignIn();
        return null;
    }
    public String help() {
        if (logState == LogState.IN) {
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
}
