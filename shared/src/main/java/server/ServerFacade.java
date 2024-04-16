package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class ServerFacade {
    private final String serverURL;

    public ServerFacade(String URL) {
        serverURL = URL;
    }

    public AuthData register(UserData userData) throws URISyntaxException, IOException {
            String path = "/user";
            return this.makeRequest("POST", path, null, userData, AuthData.class);
    }

    public AuthData login(JsonObject loginInfo) throws URISyntaxException, IOException {
            String path = "/session";
            return this.makeRequest("POST", path, null, loginInfo, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        try {
            String path = "/session";
            this.makeRequest("DELETE", path, authToken, null, null);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public Object createGame(JsonObject gameName, String authToken) throws URISyntaxException, IOException {
            String path = "/game";
            return this.makeRequest("POST", path, authToken, gameName, JsonObject.class);
    }

    public JsonObject listGames(String authToken) throws URISyntaxException, IOException {
            String path = "/game";
            return this.makeRequest("GET", path, authToken, null, JsonObject.class);
    }

    public JsonObject joinGame(int gameID, String playerColor, String authToken) throws URISyntaxException, IOException {
            String path = "/game";
            JsonObject jgo = new JsonObject();
            if (playerColor != null) {
                jgo.addProperty("playerColor", playerColor.toUpperCase());
            }
            jgo.addProperty("gameID", gameID);
            return this.makeRequest("PUT", path, authToken, jgo, JsonObject.class);
    }

    public void updateGame(GameData gameData, String authToken) throws URISyntaxException, IOException {
            String path = "/gameupdate";
            this.makeRequest("PUT", path, authToken, gameData, null);
    }

    public void clear() throws Exception {
        try {
            String path = "/db";
            this.makeRequest("DELETE", path, null, null, null);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private <T> T makeRequest(String method, String path, String authToken, Object request, Class<T> responseClass) throws URISyntaxException, IOException {

            URL url = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            http.setRequestProperty("authorization", authToken);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);

    }
    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            } catch (IOException ex) {
                throw new IOException("writeBody exception: " + ex.getMessage());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            } catch (IOException ex) {
                throw new IOException("readBody exception: " + ex.getMessage());
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new IOException("throw not successful");
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
