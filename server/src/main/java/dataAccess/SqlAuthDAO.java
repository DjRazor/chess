package dataAccess;

import com.google.gson.JsonObject;
import model.AuthData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import static java.sql.Types.NULL;

public class SqlAuthDAO implements AuthDAO {
    public SqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }
    public boolean validateAuth(String authToken) throws DataAccessException {
        configureDatabase();
        var statement = "SELECT * FROM authorized WHERE authToken = ?";
        var authStore = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        authStore.add(rs.getString("authToken"));
                    }
                    return authStore.size() == 1;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("validateAuth Error: " + ex.getMessage());
        }
    }
    public void addAuthUser(AuthData authData) throws DataAccessException {
        configureDatabase();
        var statement = "INSERT INTO authorized (username, authToken, json) VALUES (?,?,?)";
        JsonObject jo = new JsonObject();
        jo.addProperty("username", authData.username());
        jo.addProperty("authToken", authData.authToken());
        var id = executeUpdate(statement, authData.username(), authData.authToken(), jo.toString());
        if (id < 1) {
            throw new DataAccessException("addAuthUser Error " + id);
        }

    }
    public boolean logout(String authToken) throws DataAccessException {
        boolean validness = validateAuth(authToken);
        if (validness) {
            var statement = "DELETE FROM authorized WHERE authToken = ?";
            var id = executeUpdate(statement, authToken);
            System.out.print("ID: " + id + "\n");
            return id == 0;
        }
        return false;
    }
    public String usernameForAuth(String authToken) throws DataAccessException {
        var statement = "SELECT username FROM authorized WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("usernameForAuth Error: " + ex.getMessage());
        }
        return null;
    }
    public void clear() throws DataAccessException {
        var statement = "DROP TABLE IF EXISTS authorized";
        executeUpdate(statement);
    }
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof UserData p) ps.setString(i + 1, p.toString());
                    else if (param instanceof JsonObject p) ps.setString(i + 1, p.toString());
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
            CREATE TABLE IF NOT EXISTS authorized (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL,
              `authToken` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(authToken),
              INDEX(username)
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
