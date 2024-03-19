package dataAccess;

import com.google.gson.JsonObject;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import static java.sql.Types.NULL;

public class SqlGameDAO implements GameDAO{
    public SqlGameDAO() throws DataAccessException {
        configureDatabase();
    }
    public void createGame(GameData gameData) throws DataAccessException {
        var statement = "INSERT INTO chess.games (gameID, whiteUsername, blackUsername, gameName, ChessGame) VALUES (?,?,?,?,?)";
        executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), gameData.game().toString());
    }
    public boolean gameIDInUse(int gameID) throws DataAccessException {
        configureDatabase();
        var gameIDs = new ArrayList<>();
        var statement = "SELECT * FROM chess.games";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        gameIDs.add(rs.getString("gameID"));
                    }
                    return gameIDs.contains(gameID);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("gameIDInUse Error: " + ex.getMessage());
        }
    }
    public HashSet<JsonObject> listGames() throws DataAccessException {
        HashSet<JsonObject> chessGames = new HashSet<>();
        var statement = "SELECT * FROM chess.games";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer gameID = rs.getInt("gameID");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        JsonObject jo = new JsonObject();
                        jo.addProperty("gameID", gameID);
                        jo.addProperty("whiteUsername", whiteUsername);
                        jo.addProperty("blackUsername", blackUsername);
                        jo.addProperty("gameName", gameName);
                        chessGames.add(jo);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("listGames Error: " + ex.getMessage());
        }

        return chessGames;
    }
    public Object joinGame(int gameID, String playerColor, String username) throws DataAccessException {
        return null;
    }
    public void clear() throws DataAccessException {
        var statement = "DROP TABLE IF EXISTS chess.games";
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
            CREATE TABLE IF NOT EXISTS chess.games (
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
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var prepStatement = conn.prepareStatement(statement)) {
                    prepStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
