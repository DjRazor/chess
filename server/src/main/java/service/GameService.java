package service;

import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;

import java.util.HashSet;

public class GameService {
    private GameDAO gameDAO;
    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public boolean createGame(GameData gameData) throws DataAccessException {
        return gameDAO.createGame(gameData);
    }
    public boolean gameIDInUse(int gameID) throws DataAccessException {
        return gameDAO.gameIDInUse(gameID);
    }
    public HashSet<GameData> listGames() throws DataAccessException {
        return gameDAO.listGames();
    }
    public Object joinGame(int gameID, String playerColor, String username) throws DataAccessException {
        return gameDAO.joinGame(gameID, playerColor, username);
    }
    public void clear() throws DataAccessException {
        gameDAO.clear();
    }
    public void updateGame(GameData game) throws DataAccessException {
        gameDAO.updateGame(game);
    }
}
