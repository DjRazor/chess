package dataAccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import static java.sql.Types.NULL;

public class SqlGameDAO implements GameDAO{
    public SqlGameDAO() throws DataAccessException {
        configureDatabase();
    }
    public boolean createGame(GameData gameData) throws DataAccessException {
        if (gameData.gameName() == null) {
            return false;
        }
        String gameJson = new Gson().toJson(gameData.game());
        var statement = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, ChessGame) VALUES (?,?,?,?,?)";
        executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), gameJson);
        return true;
    }
    public void updateGame(GameData gameData) throws DataAccessException {
        var statement = "UPDATE games SET whiteUsername = ?, blackUsername = ?, ChessGame = ? WHERE gameID = ?";
        String gameJson = new Gson().toJson(gameData.game());
        executeUpdate(statement, gameData.whiteUsername(), gameData.blackUsername(), gameJson, gameData.gameID());
    }
    public GameData getGame(int gameID) throws DataAccessException {
        configureDatabase();
        var statement = "SELECT * FROM games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        ChessGame chessGame = new Gson().fromJson(rs.getString("ChessGame"), ChessGame.class);
                        return new GameData(gameID,whiteUsername, blackUsername, gameName, chessGame);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("getGame SqlGameDAO error: " + ex.getMessage());
        }
        return null;
    }
    public boolean gameIDInUse(int gameID) throws DataAccessException {
        configureDatabase();
        var gameIDs = new ArrayList<>();
        var statement = "SELECT * FROM games";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        gameIDs.add(rs.getInt("gameID"));
                    }
                    return gameIDs.contains(gameID);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("gameIDInUse Error: " + ex.getMessage());
        }
    }
    public HashSet<GameData> listGames() throws DataAccessException {
        configureDatabase();
        HashSet<GameData> chessGames = new HashSet<>();
        var statement = "SELECT * FROM games";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer gameID = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        ChessGame chessGame = new Gson().fromJson(rs.getString("ChessGame"), ChessGame.class);
                        GameData gameData = new GameData(gameID,whiteUsername, blackUsername, gameName, chessGame);
                        chessGames.add(gameData);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("listGames Error: " + ex.getMessage());
        }

        return chessGames;
    }
    public Object joinGame(int gameID, String playerColor, String username) throws DataAccessException {
        if (playerColor.equals("WHITE") && username != null) {
            var stmt = "SELECT whiteUsername FROM games WHERE gameID = ?";
            try (var conn = DatabaseManager.getConnection()) {
                try (var ps = conn.prepareStatement(stmt)) {
                    ps.setInt(1, gameID);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getString("whiteUsername") != null) {
                                return null;
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                throw new DataAccessException("joinGame WHITE Query Error: " + ex.getMessage());
            }
            var statement = "UPDATE games SET whiteUsername= ? WHERE gameID = ?";
            executeUpdate(statement, username, gameID);
            return new JsonObject();
        } else if (playerColor.equals("BLACK") && username != null) {
            var stmt = "SELECT blackUsername FROM games WHERE gameID = ?";
            try (var conn = DatabaseManager.getConnection()) {
                try (var ps = conn.prepareStatement(stmt)) {
                    ps.setInt(1, gameID);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String currentUser = rs.getString("blackUsername");
                            if (currentUser != null) {
                                return null;
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                throw new DataAccessException("joinGame BLACK Query Error: " + ex.getMessage());
            }
            var statement = "UPDATE games SET blackUsername = ? WHERE gameID = ?";
            executeUpdate(statement, username, gameID);
            return new JsonObject();
        }
        return null;
    }
    public void clear() throws DataAccessException {
        var statement = "DROP TABLE IF EXISTS games";
        executeUpdate(statement);
    }
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, ex.getMessage()));
        }

    }
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS games (
              `id` int NOT NULL AUTO_INCREMENT,
              `gameID` int NOT NULL,
              `whiteUsername` TEXT DEFAULT NULL,
              `blackUsername` TEXT DEFAULT NULL,
              `gameName` varchar(256) NOT NULL,
              `ChessGame` TEXT DEFAULT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
    private void configureDatabase() throws DataAccessException {
        ConfigureDatabase configureDatabase = new ConfigureDatabase();
        configureDatabase.configureDatabase(createStatements);
    }
}
