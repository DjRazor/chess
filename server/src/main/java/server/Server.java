package server;

import chess.ChessGame;
import com.google.gson.JsonObject;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.MemoryUserDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;
import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Server {
    private HashSet<String> watchers = new HashSet<>();
    private final AuthService authService = new AuthService();
    private final GameService gameService = new GameService();
    private final UserService userService = new UserService();

    public static void main(String[] args) {
        new Server().run(8080);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        // Checks if request has valid elements
        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
        if (!parsedJson.has("gameName")) {
            res.status(400);
            return badReq();
        }

        // Checks authorization
        String authHeadToken = req.headers("authorization");
        if (!authService.validateAuth(authHeadToken)) {
            res.status(401);
            return unauth();
        }

        // Generates gameID and checks that it isn't already in use
        int gameID = 0;
        boolean gameIDTaken = true;
        while (gameIDTaken) {
            gameID = new Random().nextInt(9000) + 1000;
            if (!gameService.gameIDInUse(gameID)) {
                gameIDTaken = false;
            }
        }

        // Creates GameData instance and adds to games set
        GameData gameData = new GameData(gameID, null,
                null, parsedJson.get("gameName").getAsString(), new ChessGame());
        gameService.createGame(gameData);

        // Success response
        JsonObject returnObj = new JsonObject();
        returnObj.addProperty("gameID", gameID);
        res.status(200);
        return returnObj;
    }

    private Object listGames(Request req, Response res) throws DataAccessException {
        // Validates authToken
        String authHeadToken = req.headers("authorization");
        if (!authService.validateAuth(authHeadToken)) {
            res.status(401);
            return unauth();
        }

        // Success response
        res.type("application/json");
        HashSet<JsonObject> altGames = gameService.listGames();
        res.status(200);
        return new Gson().toJson(Map.of("games", altGames));
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        // Validates authToken
        String authHeadToken = req.headers("authorization");
        if (!authService.validateAuth(authHeadToken)) {
            res.status(401);
            return unauth();
        }

        // Validates gameID has been given
        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
        if (!parsedJson.has("gameID")) {
            res.status(400);
            return badReq();
        }

        // Validates that gameID exists
        int gameID = parsedJson.get("gameID").getAsInt();
        if (!gameService.gameIDInUse(gameID)) {
            res.status(400);
            return badReq();
        }

        // If watcher, add to watchers; If desired user slot is open, fill it
        if (!parsedJson.has("playerColor")) {
            watchers.add(authHeadToken);
            res.status(200);
            return new JsonObject();
        }
        String playerColor = parsedJson.get("playerColor").getAsString();
        String currentUsername = authService.usernameForAuth(authHeadToken);
        Object response = gameService.joinGame(gameID, playerColor, currentUsername);
        if (response == null) {
            res.status(403);
            return taken();
        }

        // Success response
        res.status(200);
        return response;
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        String authHeadToken = req.headers("authorization");
        boolean logoutVal = authService.logout(authHeadToken);
        String usernameForAuth = authService.usernameForAuth(authHeadToken);
        if (logoutVal) {
            userService.removeUser(usernameForAuth);
            res.status(200);
            return new JsonObject();
        }
        res.status(401);
        return unauth();
    }

    private Object login(Request req, Response res) throws DataAccessException {
        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
        if (parsedJson.has("username")
        && parsedJson.has("password")) {
            // Checks if username and password are valid
            if (!userService.validateCreds(parsedJson.get("username").getAsString(),
                    parsedJson.get("password").getAsString())) {
                res.status(401);
                return unauth();
            }
        }

        // Logs in user with new authToken
        UserData userData = new Gson().fromJson(req.body(), UserData.class);
        AuthData authUser = userService.login(userData);
        authService.addAuthUser(authUser);

        // Success response
        JsonObject returnObj = new JsonObject();
        returnObj.addProperty("username", authUser.username());
        returnObj.addProperty("authToken", authUser.authToken());
        res.status(200);
        return returnObj;
    }

    private Object clear(Request req, Response res) throws DataAccessException {
        // Resets all private variables
        userService.clear();
        authService.clear();
        gameService.clear();
        res.status(200);
        return new JsonObject();
    }

    private Object register(Request req, Response res) throws DataAccessException {
        // Gets User info
        JsonObject testObj = new Gson().fromJson(req.body(), JsonObject.class);

        // Checks if all elements are valid
        if (!testObj.has("username")
        || !testObj.has("password")
        || !testObj.has("email")) {
            res.status(400);
            return badReq();
        }

        // Create UserData Object if all elements are valid
        UserData parsedJson = new Gson().fromJson(req.body(), UserData.class);

        // Checks if UserData username already exits
        if (userService.userExists(parsedJson.username())) {
            res.status(403);
            return taken();
        }

        // Encrypts password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserData encryptUser = new UserData(parsedJson.username(),
                encoder.encode(parsedJson.password()), parsedJson.email());

        // Adds user to existing users and authorized users
        AuthData user = userService.register(encryptUser);
        authService.addAuthUser(user);

        // Returns username and authToken
        JsonObject returnObj = new JsonObject();
        returnObj.addProperty("username", user.username());
        returnObj.addProperty("authToken", user.authToken());
        res.status(200);
        return returnObj;
    }

    public Object badReq() {
        JsonObject badRequest = new JsonObject();
        badRequest.addProperty("message", "Error: bad request");
        return badRequest;
    }

    public Object taken() {
        JsonObject taken = new JsonObject();
        taken.addProperty("message", "Error: already taken");
        return taken;
    }

    public Object unauth() {
        JsonObject unauth = new JsonObject();
        unauth.addProperty("message", "Error: unauthorized");
        return unauth;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
