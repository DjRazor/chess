package dataAccess;

import com.google.gson.JsonObject;
import model.GameData;

import java.util.HashSet;

public interface GameDAO {
    boolean createGame(GameData gameData) throws DataAccessException;
    boolean gameIDInUse(int gameID) throws DataAccessException;
    HashSet<GameData> listGames() throws DataAccessException;
    Object joinGame(int gameID, String playerColor, String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
