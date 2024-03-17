package dataAccess;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.Types.NULL;

public class SqlUserDAO implements UserDAO {
    public SqlUserDAO() throws DataAccessException {
        configureDatabase();
    }
    public AuthData register(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email, json) VALUES (?,?,?,?)";
        var json = new Gson().toJson(user);
        var id = executeUpdate(statement, user.username(), user.password(), user.email(), json);
        return null;
    }
    public AuthData login(UserData user) throws DataAccessException {
        return null;
    }
    public boolean userExists(String username) throws DataAccessException {
        return false;
    }
    public boolean validateCreds(String username, String password) throws DataAccessException {
        return false;
    }
    public void removeUser(String username) throws DataAccessException {

    }
    public void clear() throws DataAccessException {

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
            CREATE TABLE IF NOT EXISTS users (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(password),
              INDEX(username),
              INDEX(email)
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
