package serviceTests;
import static org.junit.jupiter.api.Assertions.*;

import chess.ChessGame;
import com.google.gson.JsonObject;
import dataAccess.DataAccessException;
import dataAccess.SqlAuthDAO;
import dataAccess.SqlGameDAO;
import dataAccess.SqlUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.AuthService;
import service.GameService;
import service.UserService;

import java.util.HashSet;

public class ServiceTests {
    private UserService userService = new UserService(new SqlUserDAO());
    private GameService gameService = new GameService(new SqlGameDAO());
    private AuthService authService = new AuthService(new SqlAuthDAO());
    private final String username = "Han";
    private final String password = "Solo";
    private final String email = "starwars@yahoo.com";
    private UserData userData = new UserData(username, password, email);
    private GameData gameData;

    public ServiceTests() throws DataAccessException {
    }

    private void clear() throws DataAccessException {
        userService = new UserService(new SqlUserDAO());
        gameService = new GameService(new SqlGameDAO());
        authService = new AuthService(new SqlAuthDAO());
    }
    @Test
    @Order(1)
    @DisplayName("Positive register")
    public void posRegister() throws DataAccessException {
        clear();

        // Registers user
        AuthData res = userService.register(userData);
        authService.addAuthUser(res);

        // Assertions
        assertEquals(res.username(), username);
        assertEquals(res.username().getClass(), String.class);
    }

    @Test
    @Order(2)
    @DisplayName("Negative register")
    public void negRegister() throws DataAccessException {
        // Ensure username has been taken
        UserData testUser = new UserData("Jack", password, email);
        AuthData res = userService.register(testUser);
        assertTrue(userService.userExists(res.username()));
    }

    @Test
    @Order(3)
    @DisplayName("Positive login")
    public void posLogin() throws DataAccessException {
        // Logs user in
        userService.register(userData);
        AuthData res = userService.login(userData);

        // Assertions
        assertTrue(userService.userExists(username));
        assertEquals(res.username(), username);
        assertEquals(res.authToken().getClass(), String.class);
    }

    @Test
    @Order(4)
    @DisplayName("Negative login")
    public void negLogin() throws DataAccessException {
        // Tries to log in non-existent user
        assertFalse(userService.userExists("bob"));

        // Invalid credentials
        assertFalse(userService.validateCreds(username, "Chewy"));
        assertFalse(userService.validateCreds("bob", password));
    }

    @Test
    @Order(5)
    @DisplayName("Positive createGame")
    public void posCreateGame() throws DataAccessException {
        gameData = new GameData(1234, null, null, "howdy", new ChessGame());
        gameService.createGame(gameData);

        // Asserts game is in list of games
        assertTrue(gameService.gameIDInUse(gameData.gameID()));
    }

    @Test
    @Order(6)
    @DisplayName("Negative createGame")
    public void negCreateGame() throws DataAccessException {
        // Tries to add game with gameID in use
        GameData testGame = new GameData(5555, null, null, "yippy", new ChessGame());
        gameService.createGame(testGame);
        int beforeSecAdd = gameService.listGames().size();
        gameService.createGame(testGame);
        int afterSecAdd = gameService.listGames().size();
        assertEquals(beforeSecAdd, afterSecAdd);
    }

    @Test
    @Order(7)
    @DisplayName("Positive joinGame")
    public void posJoinGame() throws DataAccessException {
        // Joins game as white
        GameData testGame = new GameData(1111, null, null, "yap", new ChessGame());
        gameService.createGame(testGame);
        Object res = gameService.joinGame(1111, "WHITE", username);
        assertNotNull(res);
    }

    @Test
    @Order(8)
    @DisplayName("Negative joinGame")
    public void negJoinGame() throws DataAccessException {
        // Tries to join taken spot
        Object res = gameService.joinGame(1111, "WHITE", username);
        assertNull(res);
    }

    @Test
    @Order(9)
    @DisplayName("Positive listGames")
    public void posListGames() throws DataAccessException {
        // Ensure games are in games list
        GameData testGame = new GameData(8080, null, null, "good day", new ChessGame());
        gameService.createGame(testGame);
        HashSet<JsonObject> games = gameService.listGames();
        assertFalse(games.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("Negative listGames")
    public void negListGames() throws DataAccessException {
        // Non-auth request
        assertFalse(authService.validateAuth("jimmy neutron"));
    }

    @Test
    @Order(11)
    @DisplayName("Positive logout")
    public void posLogout() throws DataAccessException {
        // Logs out user
        UserData testUser = new UserData("hey", "there", "delilah");
        AuthData res = userService.register(testUser);
        authService.addAuthUser(res);

        // Asserts successful logout
        boolean logoutStatus = authService.logout(res.authToken());
        assertTrue(logoutStatus);
    }

    @Test
    @Order(12)
    @DisplayName("Negative logout")
    public void negLogout() throws DataAccessException {
        // Logs out user
        UserData testUser = new UserData("hey", "there", "delilah");
        AuthData res = userService.register(testUser);
        authService.addAuthUser(res);

        // Asserts successful logout
        boolean logoutStatus = authService.logout(res.authToken());
        assertTrue(logoutStatus);

        // Tries to logout user
    }

    @Test
    @Order(13)
    @DisplayName("Clear")
    public void posClear() throws DataAccessException {
        // Asserts games are removed
        gameService.clear();
        HashSet<JsonObject> games = gameService.listGames();
        assertTrue(games.isEmpty());

        // Asserts user has been removed
        userService.clear();
        assertFalse(userService.userExists(username));

        // Asserts authToken has been removed
        authService.clear();
        //assertFalse(authService.validateAuth(authData.authToken()));
    }

}
