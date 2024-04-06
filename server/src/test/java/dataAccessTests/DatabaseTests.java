package dataAccessTests;

import chess.ChessGame;
import com.google.gson.JsonObject;
import dataAccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTests {
    private final GameDAO gameDAO = new SqlGameDAO();
    private final UserDAO userDAO = new SqlUserDAO();
    private final AuthDAO authDAO = new SqlAuthDAO();
    private final String username = "Master Chief";
    private final String password = "zippy-do-da";
    private final String email = "hey@there.com";
    private final String tempAuthToken = "good boy";
    private final UserData user = new UserData(username, password, email);

    public DatabaseTests() throws DataAccessException { }
    @Test
    @Order(1)
    @DisplayName("Clear GameDAO")
    public void clearGameDAO() throws DataAccessException {
        // Checks games table is empty
        gameDAO.createGame(new GameData(1234,null,null,"clrTest",new ChessGame()));
        gameDAO.clear();
        HashSet<GameData> games = gameDAO.listGames();
        assertEquals(games.size(), 0);
    }
    @Test
    @Order(2)
    @DisplayName("Clear AuthDAO")
    public void clearAuthDAO() throws DataAccessException {
        // Checks authorized table is empty
        authDAO.addAuthUser(new AuthData(tempAuthToken, username));
        authDAO.clear();
        assertFalse(authDAO.validateAuth(tempAuthToken));
    }
    @Test
    @Order(3)
    @DisplayName("Clear UserDAO")
    public void clearUserDAO() throws DataAccessException {
        // Checks users table is empty
        userDAO.clear();
        boolean userExists = userDAO.userExists(username);
        assertFalse(userExists);
    }
    @Test
    @Order(4)
    @DisplayName("Positive Register")
    public void posRegister() throws DataAccessException {
        // Register new user
        AuthData regRes = userDAO.register(user);
        assertEquals(regRes.username(), username);
        assertEquals(regRes.authToken().getClass(), String.class);
    }
    @Test
    @Order(5)
    @DisplayName("Negative Register")
    public void negRegister() throws DataAccessException {
        // Try to register a user with same username
        AuthData regRes = userDAO.register(user);
        assertNull(regRes);
    }
    @Test
    @Order(6)
    @DisplayName("Positive Logout")
    public void posLogout() throws DataAccessException {
        // Valid logout
        AuthData tempAuth = new AuthData("jimmy", "butler");
        authDAO.addAuthUser(tempAuth);
        boolean logoutStatus = authDAO.logout(tempAuth.authToken());
        assertTrue(logoutStatus);
    }
    @Test
    @Order(7)
    @DisplayName("Negative Logout")
    public void negLogout() throws DataAccessException {
        // Logout someone who already logged out
        AuthData tempAuth = new AuthData("Michael", "Jordan");
        authDAO.addAuthUser(tempAuth);
        boolean logoutStatus = authDAO.logout(tempAuth.authToken());
        assertTrue(logoutStatus);
        boolean logoutStatus2 = authDAO.logout(tempAuth.authToken());
        assertFalse(logoutStatus2);
    }
    @Test
    @Order(8)
    @DisplayName("Positive Login")
    public void posLogin() throws DataAccessException {
        // Login existing user
        AuthData regRes = userDAO.login(user);
        assertNotNull(regRes);
    }
    @Test
    @Order(9)
    @DisplayName("Negative Login")
    public void negLogin() throws DataAccessException {
        // Try to log in non-existent user
        AuthData regRes = userDAO.login(new UserData("jim", "bob", "rascal"));
        assertNull(regRes);
    }
    @Test
    @Order(10)
    @DisplayName("Positive User Exists")
    public void posUserExists() throws DataAccessException {
        // Checks for existing user
        assertTrue(userDAO.userExists(username));

    }
    @Test
    @Order(11)
    @DisplayName("Negative User Exists")
    public void negUserExists() throws DataAccessException {
        // Try to check for non-existent user
        assertFalse(userDAO.userExists("john"));
    }
    @Test
    @Order(12)
    @DisplayName("Positive Validate Creds")
    public void posValidateCreds() throws DataAccessException {
        assertTrue(userDAO.validateCreds(username, password));
    }
    @Test
    @Order(13)
    @DisplayName("Negative Validate Creds")
    public void negValidateCreds() throws DataAccessException {
        assertFalse(userDAO.validateCreds(username,"jimbob"));
        assertFalse(userDAO.validateCreds("jack", password));
    }
    @Test
    @Order(14)
    @DisplayName("Positive Validate Auth")
    public void posValidateAuth() throws DataAccessException {
        AuthData tempAuth = new AuthData("Harold", "Johnson");
        authDAO.addAuthUser(tempAuth);
        assertTrue(authDAO.validateAuth(tempAuth.authToken()));
    }
    @Test
    @Order(15)
    @DisplayName("Negative Validate Auth")
    public void negValidateAuth() throws DataAccessException {
        assertFalse(authDAO.validateAuth(tempAuthToken));
    }
    @Test
    @Order(16)
    @DisplayName("Positive Add Auth User")
    public void posAddAuthUser() throws DataAccessException {
        AuthData tempAuth = new AuthData("LeBron", "James");
        authDAO.addAuthUser(tempAuth);
        assertTrue(authDAO.validateAuth(tempAuth.authToken()));
        assertEquals(authDAO.usernameForAuth(tempAuth.authToken()), tempAuth.username());
    }
    @Test
    @Order(17)
    @DisplayName("Negative Add Auth User")
    public void negAddAuthUser() throws DataAccessException {
        AuthData tempAuth = new AuthData("XD", "jack");
        authDAO.addAuthUser(tempAuth);
        authDAO.logout(tempAuth.authToken());
        assertFalse(authDAO.validateAuth(tempAuth.authToken()));
    }
    @Test
    @Order(18)
    @DisplayName("Positive Username for Auth")
    public void posUsernameForAuth() throws DataAccessException {
        AuthData tempAuth = new AuthData("Tommy", "Hilfiger");
        authDAO.addAuthUser(tempAuth);
        assertEquals(tempAuth.username(), authDAO.usernameForAuth(tempAuth.authToken()));
    }
    @Test
    @Order(19)
    @DisplayName("Negative Username for Auth")
    public void negUsernameForAuth() throws DataAccessException {
        AuthData tempAuth = new AuthData("Sparrow", "Falcon");
        authDAO.addAuthUser(tempAuth);
        assertNull(authDAO.usernameForAuth("Jar-Jar Binks"));
    }
    @Test
    @Order(20)
    @DisplayName("Positive Create Game")
    public void posCreateGame() throws DataAccessException {
        int gameID = 1234;
        GameData tempGame = new GameData(gameID, null, null, "EpicGame", new ChessGame());
        gameDAO.createGame(tempGame);
        assertTrue(gameDAO.gameIDInUse(gameID));
    }
    @Test
    @Order(21)
    @DisplayName("Negative Create Game")
    public void negCreateGame() throws DataAccessException {
        // Missing gameName and game
        GameData tempGame = new GameData(9999,null,null, null,null);
        assertFalse(gameDAO.createGame(tempGame));
    }
    @Test
    @Order(22)
    @DisplayName("Positive Game ID In Use")
    public void posGameIDInUse() throws DataAccessException {
        int gameID = 2468;
        gameDAO.createGame(new GameData(gameID, null, null, "Chesster", new ChessGame()));
        assertTrue(gameDAO.gameIDInUse(gameID));
    }
    @Test
    @Order(23)
    @DisplayName("Negative Game ID In Use")
    public void negGameIDInUse() throws DataAccessException {
        assertFalse(gameDAO.gameIDInUse(7777));
    }
    @Test
    @Order(24)
    @DisplayName("Positive List Games")
    public void posListGames() throws DataAccessException {
        HashSet<GameData> games = gameDAO.listGames();
        assertFalse(games.isEmpty());
    }
    @Test
    @Order(25)
    @DisplayName("Negative List Games")
    public void negListGames() throws DataAccessException {
        HashSet<GameData> games = gameDAO.listGames();
        assertTrue(!games.isEmpty());
    }
    @Test
    @Order(26)
    @DisplayName("Positive Join Game")
    public void posJoinGame() throws DataAccessException {
        Object joinStatus = gameDAO.joinGame(1234, "WHITE", "doginator");
        assertEquals(joinStatus, new JsonObject());
    }
    @Test
    @Order(27)
    @DisplayName("Negative Join Game")
    public void negJoinGame() throws DataAccessException {
        // Tries to join taken spot
        Object joinStatus = gameDAO.joinGame(1234, "WHITE", "imposter");
        assertNull(joinStatus);
    }
    @Test
    @Order(28)
    @DisplayName("Negative Clear GameDAO")
    public void negClearGameDAO() throws DataAccessException {
        gameDAO.clear();
        assertTrue(true);
    }
    @Test
    @Order(29)
    @DisplayName("Negative Clear AuthDAO")
    public void negClearAuthDAO() throws DataAccessException {
        authDAO.clear();
        assertTrue(true);
    }
    @Test
    @Order(30)
    @DisplayName("Negative Clear UserDAO")
    public void negClearUserDAO() throws DataAccessException {
        userDAO.clear();
        assertTrue(true);
    }
}