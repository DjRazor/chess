package dataAccess;

import com.google.gson.JsonObject;
import model.GameData;

import java.util.HashSet;

public class SqlGameDAO implements GameDAO{
    public void createGame(GameData gameData) throws DataAccessException {

    }
    public boolean gameIDInUse(int gameID) throws DataAccessException {
        return false;
    }
    public HashSet<JsonObject> listGames() throws DataAccessException {
        return null;
    }
    public Object joinGame(int gameID, String playerColor, String username) throws DataAccessException {
        return null;
    }
    public void clear() throws DataAccessException {

    }
}
