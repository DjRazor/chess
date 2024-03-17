package dataAccess;

import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.Types.NULL;

public class SqlAuthDAO implements AuthDAO {
    public SqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }
    public boolean validateAuth(String authToken) throws DataAccessException {
        return false;
    }
    public void addAuthUser(AuthData authData) throws DataAccessException {

    }
    public boolean logout(String authToken) throws DataAccessException {
        return false;
    }
    public String usernameForAuth(String authToken) throws DataAccessException {
        return null;
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
