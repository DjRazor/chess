package server;

import chess.ChessGame;
import com.google.gson.JsonObject;
import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
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

    private HashSet<UserData> users = new HashSet<>();
    private HashSet<String> usersList = new HashSet<>();
    private HashSet<GameData> games = new HashSet<>();
    private HashSet<Integer> gameIDs = new HashSet<>();
    private HashSet<AuthData> authorized = new HashSet<>();
    private HashSet<String> watchers = new HashSet<>();
    private String authToken = null;
    private AuthService authService = new AuthService();
    private GameService gameService = new GameService();
    private UserService userService = new UserService();

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
        //Spark.get("/game", this::listGames);
        //Spark.post("/game", this::createGame);
        //Spark.put("/game", this::joinGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object validateAuth(String authToken) {
        boolean authUser = false;
        for (AuthData auth : authorized) {
            if (auth.authToken().equals(authToken)) {
                authUser = true;
                break;
            }
        }
        if (!authUser) {
            JsonObject unauth = new JsonObject();
            unauth.addProperty("message", "Error: unauthorized");
            return unauth;
        }
        return null;
    }

//    private Object joinGame(Request req, Response res) {
//        String authHeadToken = req.headers("authorization");
//        Object valid = validateAuth(authHeadToken);
//        if (valid != null) {
//            res.status(401);
//            return valid;
//        }
//
//        // Validate request elements
//        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
//        if (!parsedJson.has("gameID")) {
//            res.status(400);
//            JsonObject badReq = new JsonObject();
//            badReq.addProperty("message", "Error: bad request");
//            return badReq;
//        }
//
//        // Gets gameID from request
//        int gameID = parsedJson.get("gameID").getAsInt();
//
//        // Validates that gameID exists
//        if (!gameIDs.contains(gameID)) {
//            JsonObject invalid = new JsonObject();
//            res.status(400);
//            invalid.addProperty("message", "Error: bad request");
//            return invalid;
//        }
//
//        // If watcher, add to watchers; Create playerColor string if player
//        if (!parsedJson.has("playerColor")) {
//            watchers.add(authHeadToken);
//            res.status(200);
//            return new JsonObject();
//        }
//        String playerColor = parsedJson.get("playerColor").getAsString();
//
//        // Gets username via authToken
//        String currentUser = null;
//        for (AuthData authData : authorized) {
//            if (authData.authToken().equals(authHeadToken)) {
//                currentUser = authData.username();
//            }
//        }
//
//        // Finds needed GameData element
//        GameData currentGame;
//        for (GameData gameData : games) {
//            if (gameData.gameID() == gameID) {
//                if (playerColor.equals("WHITE") && gameData.whiteUsername() == null) {
//                    currentGame = new GameData(gameID, currentUser, gameData.blackUsername(), gameData.gameName(), gameData.game());
//                }
//                else if (playerColor.equals("BLACK") && gameData.blackUsername() == null){
//                    currentGame = new GameData(gameID, gameData.whiteUsername(), currentUser, gameData.gameName(), gameData.game());
//                }
//                else {
//                    JsonObject taken = new JsonObject();
//                    taken.addProperty("message", "Error: already taken");
//                    res.status(403);
//                    return taken;
//                }
//                games.remove(gameData);
//                games.add(currentGame);
//                res.status(200);
//                return new JsonObject();
//            }
//        }
//        res.status(500);
//        JsonObject failed = new JsonObject();
//        failed.addProperty("message", "Error: description");
//        return failed;
//    }
//
//    private Object createGame(Request req, Response res) {
//        String gameName;
//        boolean gameIDTaken = true;
//        int gameID = 0;
//
//        // Checks authorization
//        String authHeadToken = req.headers("authorization");
//        Object valid = validateAuth(authHeadToken);
//        if (valid != null) {
//            res.status(401);
//            return valid;
//        }
//
//        // Checks if randomly generated gameID is taken already
//        while (gameIDTaken) {
//            gameID = new Random().nextInt(9000) + 1000;
//            if (!gameIDs.contains(gameID)) {
//                gameIDs.add(gameID);
//                gameIDTaken = false;
//            }
//        }
//
//        // Gets gameName, throws error if bad request
//        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
//        if (parsedJson.has("gameName")) {
//            gameName = parsedJson.get("gameName").getAsString();
//        } else {
//            res.status(400);
//            JsonObject badReq = new JsonObject();
//            badReq.addProperty("message", "Error: bad request");
//            return badReq;
//        }
//
//        ChessGame chessGame = new ChessGame();
//        GameData game = new GameData(gameID, null, null, gameName, chessGame);
//        games.add(game);
//
//        JsonObject returnObj = new JsonObject();
//        returnObj.addProperty("gameID", gameID);
//        res.status(200);
//        return returnObj;
//    }
//
//    private Object listGames(Request req, Response res) {
//        String authHeadToken = req.headers("authorization");
//        Object valid = validateAuth(authHeadToken);
//
//        if (valid != null) {
//            res.status(401);
//            return valid;
//        }
//
//        res.type("application/json");
//        HashSet<JsonObject> altGames = new HashSet<>();
//        for (GameData gameData : games) {
//            JsonObject altGame = new JsonObject();
//            altGame.addProperty("gameID", gameData.gameID());
//            altGame.addProperty("whiteUsername", gameData.whiteUsername());
//            altGame.addProperty("blackUsername", gameData.blackUsername());
//            altGame.addProperty("gameName", gameData.gameName());
//            altGames.add(altGame);
//        }
//        return new Gson().toJson(Map.of("games", altGames));
//    }

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

        // Adds user to existing users and authorized users
        AuthData user = userService.register(parsedJson);
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
