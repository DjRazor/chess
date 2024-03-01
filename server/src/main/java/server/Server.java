package server;

import com.google.gson.JsonObject;
import model.AuthData;
import model.UserData;
import spark.*;
import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Server {
    public static void main(String[] args) {
        new Server().run(8080);
    }
    private HashSet<UserData> users = new HashSet<>();
    private HashSet<String> usersList = new HashSet<>();
    private HashSet<String> games = new HashSet<>();
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

    private Object joinGame(Request req, Response res) {
        return listGames(req, res);
    }

    private Object createGame(Request req, Response res) {
        return listGames(req, res);
    }

    private Object listGames(Request req, Response res) {
        res.type("application/json");
        return new Gson().toJson(Map.of("games", games));
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
        String username = null;
        String password = null;
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
        System.out.print("Cleared");
        users = new HashSet<>();
        return listUsers(req, res);
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

    private Object listUsers(Request req, Response res) {
        res.type("application/json");
        return new Gson().toJson(Map.of("name", users));
    }

    private Object deleteUser(Request req, Response res) {
        users.remove(req.params(":name"));
        return listUsers(req, res);
    }

    private static <T> T getBody(Request request, Class<T> clazz) {
        var body = new Gson().fromJson(request.body(), clazz);
        if (body == null) {
            throw new RuntimeException("missing required body");
        }
        return body;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
