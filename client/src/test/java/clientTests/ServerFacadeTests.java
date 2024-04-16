//package clientTests;
//
//import com.google.gson.JsonObject;
//import dataAccess.DataAccessException;
//import model.AuthData;
//import model.UserData;
//import org.junit.jupiter.api.*;
//import server.Server;
//import server.ServerFacade;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class ServerFacadeTests {
//
//    private static Server server;
//    static ServerFacade facade;
//
//    @BeforeAll
//    public static void init() {
//        server = new Server();
//        var port = server.run(0);
//        System.out.println("Started test HTTP server on " + port);
//        String serverURL = "http://localhost:" + port;
//        facade = new ServerFacade(serverURL);
//    }
//
//    @AfterAll
//    static void stopServer() {
//        server.stop();
//    }
//
//    @Test
//    @Order(1)
//    @DisplayName("Clear")
//    public void clear() throws DataAccessException {
//        facade.clear();
//        facade.register(new UserData("Junie", "B", "Jones"));
//        facade.clear();
//        var res = facade.register(new UserData("Junie", "B", "Jones"));
//        assertEquals(res.getClass(), AuthData.class);
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("Positive Register")
//    public void posRegister() throws DataAccessException {
//        var authData = facade.register(new UserData("Jeremiah", "y", "z"));
//        assertEquals(authData.getClass(), AuthData.class);
//    }
//
//    @Test
//    @Order(3)
//    @DisplayName("Negative Register")
//    public void negRegister() {
//        // Tries to register with a taken username
//        boolean success = false;
//        try {
//            facade.register(new UserData("Jeremiah", "y", "z"));
//        } catch (DataAccessException ex) {
//            success = true;
//        }
//        assertTrue(success);
//    }
//
//    @Test
//    @Order(4)
//    @DisplayName("Positive Login")
//    public void posLogin() throws DataAccessException {
//        JsonObject loginInfo = new JsonObject();
//        loginInfo.addProperty("username", "Jeremiah");
//        loginInfo.addProperty("password", "y");
//        var loginRes = facade.login(loginInfo);
//        assertEquals(loginRes.getClass(), AuthData.class);
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("Negative Login")
//    public void negLogin() {
//        // Incorrect password
//        JsonObject loginInfo = new JsonObject();
//        loginInfo.addProperty("username", "Jeremiah");
//        loginInfo.addProperty("password", "Mr. Dude");
//        boolean success = false;
//        try {
//            facade.login(loginInfo);
//        } catch (DataAccessException ex) {
//            success = true;
//        }
//        assertTrue(success);
//    }
//
//    @Test
//    @Order(6)
//    @DisplayName("Positive Logout")
//    public void posLogout() throws DataAccessException {
//        JsonObject loginInfo = new JsonObject();
//        loginInfo.addProperty("username", "Jeremiah");
//        loginInfo.addProperty("password", "y");
//        var loginRes = facade.login(loginInfo);
//        // Fails if there is an error thrown
//        try {
//            facade.logout(loginRes.authToken());
//            assertTrue(true);
//        } catch (DataAccessException ex) {
//            fail();
//        }
//    }
//
//    @Test
//    @Order(7)
//    @DisplayName("Negative Logout")
//    public void negLogout() throws DataAccessException {
//        // Tries to log out user twice
//        JsonObject loginInfo = new JsonObject();
//        loginInfo.addProperty("username", "Jeremiah");
//        loginInfo.addProperty("password", "y");
//        var loginRes = facade.login(loginInfo);
//        // Fails if there is an error thrown
//        try {
//            facade.logout(loginRes.authToken());
//            facade.logout(loginRes.authToken());
//            fail();
//        } catch (DataAccessException ex) {
//            assertTrue(true);
//        }
//    }
//
//    @Test
//    @Order(8)
//    @DisplayName("Positive Create Game")
//    public void posCreateGame() throws DataAccessException {
//        boolean status = false;
//        AuthData authData = facade.register(new UserData("A", "B", "C"));
//        JsonObject gameName = new JsonObject();
//        gameName.addProperty("gameName", "game time baby");
//        Object gameRes = facade.createGame(gameName, authData.authToken());
//        if (gameRes.getClass().equals(JsonObject.class)) {
//            if (((JsonObject) gameRes).has("gameID")) {
//                status = true;
//            }
//        }
//        assertTrue(status);
//    }
//
//    @Test
//    @Order(9)
//    @DisplayName("Negative Create Game")
//    public void negCreateGame() throws DataAccessException {
//        // Bad request
//        boolean status = false;
//        AuthData authData = facade.register(new UserData("T", "O", "T"));
//        JsonObject gameName = new JsonObject();
//        gameName.addProperty("game name", "game time baby");
//        try {
//            Object gameRes = facade.createGame(gameName, authData.authToken());
//        } catch (DataAccessException ex) {
//            status = true;
//        }
//        assertTrue(status);
//    }
//
//    @Test
//    @Order(10)
//    @DisplayName("Positive List Games")
//    public void posListGames() throws DataAccessException {
//        AuthData authData = facade.register(new UserData("old", "mac", "donald"));
//        var games = facade.listGames(authData.authToken());
//        assertFalse(games.isEmpty());
//    }
//
//    @Test
//    @Order(11)
//    @DisplayName("Negative List Games")
//    public void negListGames() throws DataAccessException {
//        // Invalid authToken
//        boolean status = true;
//        AuthData authData = facade.register(new UserData("new", "mac", "donald"));
//        try {
//            var games = facade.listGames(authData.username());
//        } catch (DataAccessException ex) {
//            status = false;
//        }
//        assertFalse(status);
//    }
//
//    @Test
//    @Order(12)
//    @DisplayName("Positive Join Game")
//    public void posJoinGame() throws DataAccessException {
//        AuthData authData = facade.register(new UserData("young", "mac", "donald"));
//        JsonObject gameName = new JsonObject();
//        gameName.addProperty("gameName", "final showdown");
//        JsonObject gameRes = (JsonObject) facade.createGame(gameName, authData.authToken());
//        assertTrue(gameRes.has("gameID"));
//        JsonObject joinRes = facade.joinGame(gameRes.get("gameID").getAsInt(), "white", authData.authToken());
//        assertTrue(joinRes.entrySet().isEmpty());
//    }
//
//    @Test
//    @Order(13)
//    @DisplayName("Negative Join Game")
//    public void negJoinGame() throws DataAccessException {
//        // Tries to join taken spot
//        boolean status = true;
//        AuthData authData = facade.register(new UserData("max", "mac", "donald"));
//        JsonObject gameName = new JsonObject();
//        gameName.addProperty("gameName", "final showdown");
//        JsonObject gameRes = (JsonObject) facade.createGame(gameName, authData.authToken());
//        assertTrue(gameRes.has("gameID"));
//        JsonObject joinRes = facade.joinGame(gameRes.get("gameID").getAsInt(), "white", authData.authToken());
//
//        AuthData authData2 = facade.register(new UserData("P", "O", "P"));
//        try {
//            JsonObject joinRes2 = facade.joinGame(gameRes.get("gameID").getAsInt(), "white", authData2.authToken());
//        } catch (DataAccessException ex) {
//            status = false;
//        }
//        assertFalse(status);
//    }
//
//    @Test
//    @Order(14)
//    @DisplayName("Clear Added Info")
//    public void clearInfo() throws DataAccessException {
//        facade.clear();
//        assertTrue(true);
//    }
//}
