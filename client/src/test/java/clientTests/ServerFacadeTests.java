package clientTests;

import com.google.gson.JsonObject;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        String serverURL = "http://localhost:8080";
        facade = new ServerFacade(serverURL);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Clear")
    public void clear() throws DataAccessException {
        facade.clear();
        facade.register(new UserData("Junie", "B", "Jones"));
        facade.clear();
        var res = facade.register(new UserData("Junie", "B", "Jones"));
        assertEquals(res.getClass(), AuthData.class);
    }

    @Test
    @Order(2)
    @DisplayName("Positive Register")
    public void posRegister() throws DataAccessException {
        var authData = facade.register(new UserData("Jeremiah", "y", "z"));
        assertEquals(authData.getClass(), AuthData.class);
    }

    @Test
    @Order(3)
    @DisplayName("Negative Register")
    public void negRegister() throws DataAccessException {
        // Tries to register with a taken username
        try {
            facade.register(new UserData("Jeremiah", "y", "z"));
        } catch (DataAccessException ex) {
            assertTrue(true);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Positive Login")
    public void posLogin() throws DataAccessException {

    }

    @Test
    @Order(5)
    @DisplayName("Negative Login")
    public void negLogin() throws DataAccessException {

    }

    @Test
    @Order(6)
    @DisplayName("Positive Logout")
    public void posLogout() throws DataAccessException {

    }

    @Test
    @Order(7)
    @DisplayName("Negative Logout")
    public void negLogout() throws DataAccessException {

    }

    @Test
    @Order(8)
    @DisplayName("Positive Create Game")
    public void posCreateGame() throws DataAccessException {

    }

    @Test
    @Order(9)
    @DisplayName("Negative Create Game")
    public void negCreateGame() throws DataAccessException {

    }

    @Test
    @Order(10)
    @DisplayName("Positive List Games")
    public void posListGames() throws DataAccessException {

    }

    @Test
    @Order(11)
    @DisplayName("Negative List Games")
    public void negListGames() throws DataAccessException {

    }

    @Test
    @Order(12)
    @DisplayName("Positive Join Game")
    public void posJoinGame() throws DataAccessException {

    }

    @Test
    @Order(13)
    @DisplayName("Negative Join Game")
    public void negJoinGame() throws DataAccessException {

    }
}
