package server;

import chess.ChessGame;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import model.UserData;
import spark.*;
import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Server {
    public static void main(String[] args) {
        new Server().run(8080);
    }
    private HashSet<UserData> users = new HashSet<>();
    private HashSet<String> usersList = new HashSet<>();
    private HashSet<GameData> games = new HashSet<>();
    private HashSet<Integer> gameIDs = new HashSet<>();
    private HashSet<AuthData> authorized = new HashSet<>();
    private String authToken = null;

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clearApp);
        Spark.post("/user", this::createUser);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

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

    private Object joinGame(Request req, Response res) {
        String authHeadToken = req.headers("authorization");
        Object valid = validateAuth(authHeadToken);
        if (valid != null) {
            res.status(401);
            return valid;
        }

        // Validate request elements
        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
        if (!parsedJson.has("playerColor")
        || !parsedJson.has("gameID")) {
            res.status(400);
            JsonObject badReq = new JsonObject();
            badReq.addProperty("message", "Error: bad request");
            return badReq;
        }

        // Gets elements from body
        String playerColor = parsedJson.get("playerColor").getAsString();
        int gameID = parsedJson.get("gameID").getAsInt();

        // Validates that gameID exists
        if (!gameIDs.contains(gameID)) {
            JsonObject invalid = new JsonObject();
            invalid.addProperty("message", "Error: Invalid gameID");
            return invalid;
        }

        // Gets username via authToken
        String currentUser = null;
        for (AuthData authData : authorized) {
            if (authData.authToken().equals(authHeadToken)) {
                currentUser = authData.username();
            }
        }

        // Finds needed GameData element
        GameData currentGame;
        for (GameData gameData : games) {
            if (gameData.gameID() == gameID) {
                if (playerColor.equals("WHITE") && gameData.whiteUsername() == null) {
                    currentGame = new GameData(gameID, currentUser, gameData.blackUsername(), gameData.gameName(), gameData.game());
                }
                else if (playerColor.equals("BLACK") && gameData.blackUsername() == null){
                    currentGame = new GameData(gameID, gameData.whiteUsername(), currentUser, gameData.gameName(), gameData.game());
                }
                else {
                    JsonObject taken = new JsonObject();
                    taken.addProperty("message", "Error: already taken");
                    res.status(403);
                    return taken;
                }
                games.remove(gameData);
                games.add(currentGame);
                res.status(200);
                return new JsonObject();
            }
        }
        res.status(500);
        JsonObject failed = new JsonObject();
        failed.addProperty("message", "Error: description");
        return failed;
    }

    private Object createGame(Request req, Response res) {
        String gameName;
        boolean gameIDTaken = true;
        int gameID = 0;

        // Checks authorization
        String authHeadToken = req.headers("authorization");
        Object valid = validateAuth(authHeadToken);
        if (valid != null) {
            res.status(401);
            return valid;
        }

        // Checks if randomly generated gameID is taken already
        while (gameIDTaken) {
            gameID = new Random().nextInt(9000) + 1000;
            if (!gameIDs.contains(gameID)) {
                gameIDs.add(gameID);
                gameIDTaken = false;
            }
        }

        // Gets gameName, throws error if bad request
        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
        if (parsedJson.has("gameName")) {
            gameName = parsedJson.get("gameName").getAsString();
        } else {
            res.status(400);
            JsonObject badReq = new JsonObject();
            badReq.addProperty("message", "Error: bad request");
            return badReq;
        }

        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(gameID, null, null, gameName, chessGame);
        games.add(game);

        JsonObject returnObj = new JsonObject();
        returnObj.addProperty("gameID", gameID);
        res.status(200);
        return returnObj;
    }

    private Object listGames(Request req, Response res) {
        String authHeadToken = req.headers("authorization");
        Object valid = validateAuth(authHeadToken);

        if (valid != null) {
            res.status(401);
            return valid;
        }

        res.type("application/json");
        HashSet<JsonObject> altGames = new HashSet<>();
        for (GameData gameData : games) {
            JsonObject altGame = new JsonObject();
            altGame.addProperty("gameID", gameData.gameID());
            altGame.addProperty("whiteUsername", gameData.whiteUsername());
            altGame.addProperty("blackUsername", gameData.blackUsername());
            altGame.addProperty("gameName", gameData.gameName());
            altGames.add(altGame);
        }
        return new Gson().toJson(Map.of("games", altGames));
    }

    private Object logout(Request req, Response res) {
        String authHeadToken = req.headers("authorization");
        String currentUser = null;
        for (AuthData auth : authorized) {
            if (auth.authToken().equals(authHeadToken)) {
                currentUser = auth.username();
                usersList.remove(currentUser);
                for (UserData user : users) {
                    if (user.username().equals(currentUser)) {
                        res.status(200);
                        return new JsonObject();
                    }
                }
            }
        }
        res.status(401);
        JsonObject unauth = new JsonObject();
        unauth.addProperty("message", "Error: unauthorized");
        return unauth;
    }

    private Object login(Request req, Response res) {
        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
        String username;
        String password;
        if (parsedJson.has("username")
        && parsedJson.has("password")) {
            username = parsedJson.get("username").getAsString();
            password = parsedJson.get("password").getAsString();
            for (UserData userData : users) {
                if (userData.username().equals(username) && userData.password().equals(password)) {
                    // Creates AuthData instance
                    authToken = UUID.randomUUID().toString();
                    AuthData auth = new AuthData(authToken, username);
                    authorized.add(auth);

                    // Success response
                    JsonObject returnObj = new JsonObject();
                    returnObj.addProperty("username", username);
                    returnObj.addProperty("authToken", authToken);
                    res.status(200);
                    return returnObj;
                }
            }
        }
        res.status(401);
        JsonObject unauth = new JsonObject();
        unauth.addProperty("message", "Error: unauthorized");
        return unauth;
    }

    private Object clearApp(Request req, Response res) {
        // Resets all private variables
        users = new HashSet<>();
        games = new HashSet<>();
        gameIDs = new HashSet<>();
        usersList = new HashSet<>();
        authorized = new HashSet<>();
        authToken = null;

        res.status(200);
        return new JsonObject();
    }

    private Object createUser(Request req, Response res) {
        // Gets username key's value
        JsonObject parsedJson = new Gson().fromJson(req.body(), JsonObject.class);
        if (!parsedJson.has("username")
        || !parsedJson.has("password")
        || !parsedJson.has("email")) {
            res.status(400);
            JsonObject badReq = new JsonObject();
            badReq.addProperty("message", "bad request");
            return badReq;
        }
        String username = parsedJson.get("username").getAsString();
        String password = parsedJson.get("password").getAsString();
        String email = parsedJson.get("email").getAsString();

        // Adds user to users (checks if username already exits)
        if (usersList.contains(username)) {
            res.status(403);
            JsonObject taken = new JsonObject();
            taken.addProperty("message", "Error: already taken");
            return taken;
        }
        usersList.add(username);
        UserData user = new UserData(username, password, email);
        users.add(user);

        // Creates AuthData instance to add to authorized set
        authToken = UUID.randomUUID().toString();
        AuthData userAuth = new AuthData(authToken, username);
        authorized.add(userAuth);

        // Creates JsonObject to return
        JsonObject returnObj = new JsonObject();
        returnObj.addProperty("username", username);
        returnObj.addProperty("authToken", authToken);

        return returnObj;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
