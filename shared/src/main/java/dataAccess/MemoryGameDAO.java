package dataAccess;

import com.google.gson.JsonObject;
import model.GameData;

import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {
    private HashSet<GameData> games = new HashSet<>();
    private HashSet<String> watcher = new HashSet<>();

    public boolean createGame(GameData gameData) {
        if (gameData.gameName() == null) {
            return false;
        }
        games.add(gameData);
        return true;
    }
    public boolean gameIDInUse(int gameID) {
        for (GameData gameData : games) {
            if (gameData.gameID() == gameID) {
                return true;
            }
        }
        return false;
    }
    public HashSet<GameData> listGames() {
        return games;
    }

    public Object joinGame(int gameID, String playerColor, String username) {
        GameData currentGame;
        for (GameData gameData : games) {
            if (gameData.gameID() == gameID) {
                if (playerColor.equals("WHITE") && gameData.whiteUsername() == null) {
                    currentGame = new GameData(gameID, username, gameData.blackUsername(), gameData.gameName(), gameData.game());
                } else if (playerColor.equals("BLACK") && gameData.blackUsername() == null) {
                    currentGame = new GameData(gameID, gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
                } else {
                    return null;
                }
                games.remove(gameData);
                games.add(currentGame);
                return new JsonObject();
            }
        }
        return null;
    }

    public void updateGame(GameData gameData) {
        // Update later
    }

    public void clear() {
        games = new HashSet<>();
    }

    public GameData getGame(int gameID) {
        return null;
    }
}
