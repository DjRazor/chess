package clientTests;

import dataAccess.DataAccessException;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        String serverURL = "https://localhost:8080";
        facade = new ServerFacade(serverURL);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void reset() {
    }

    @Test
    @DisplayName("Positive Register")
    public void posRegister() throws DataAccessException {

    }

    @Test
    @DisplayName("Negative Register")
    public void negRegister() throws DataAccessException {

    }

    @Test
    @DisplayName("Positive Login")
    public void posLogin() throws DataAccessException {

    }

    @Test
    @DisplayName("Negative Login")
    public void negLogin() throws DataAccessException {

    }

    @Test
    @DisplayName("Positive Logout")
    public void posLogout() throws DataAccessException {

    }

    @Test
    @DisplayName("Negative Logout")
    public void negLogout() throws DataAccessException {

    }

    @Test
    @DisplayName("Positive Create Game")
    public void posCreateGame() throws DataAccessException {

    }

    @Test
    @DisplayName("Negative Create Game")
    public void negCreateGame() throws DataAccessException {

    }

    @Test
    @DisplayName("Positive List Games")
    public void posListGames() throws DataAccessException {

    }

    @Test
    @DisplayName("Negative List Games")
    public void negListGames() throws DataAccessException {

    }

    @Test
    @DisplayName("Positive Join Game")
    public void posJoinGame() throws DataAccessException {

    }

    @Test
    @DisplayName("Negative Join Game")
    public void negJoinGame() throws DataAccessException {

    }

    @Test
    @DisplayName("Clear")
    public void clear() throws DataAccessException {

    }
}
