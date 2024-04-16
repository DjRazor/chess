package dataAccess;

import com.google.gson.JsonObject;
import model.AuthData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

import static java.sql.Types.NULL;

public class SqlUserDAO implements UserDAO {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public SqlUserDAO() throws DataAccessException {
        configureDatabase();
    }
    public AuthData register(UserData user) throws DataAccessException {
        if (!userExists(user.username())) {
            var statement = "INSERT INTO users (username, password, email) VALUES (?,?,?)";
            String hashPass = encoder.encode(user.password());
            var id = executeUpdate(statement, user.username(), hashPass, user.email());
            if (id >= 1) {
                return login(user);
            }
        }
        return null;
    }
    public AuthData login(UserData user) throws DataAccessException {
        if (validateCreds(user.username(), user.password())) {
            String authToken = UUID.randomUUID().toString();
            return new AuthData(authToken, user.username());
        }
        return null;
    }
    public boolean userExists(String username) throws DataAccessException {
        var result = new ArrayList<String>();
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users WHERE username = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1,username);
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(rs.getString("username"));
                    }
                }
            }
            return result.size() == 1;
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to read data: %s", ex.getMessage()));
        }
    }
    public boolean validateCreds(String username, String password) throws DataAccessException {
        configureDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT password FROM users WHERE username = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1,username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedPass = rs.getString("password");
                        return encoder.matches(password, storedPass);
                    }
                    return false;
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("validateCreds Error: " + ex.getMessage());
        }
    }
    public void clear() throws DataAccessException {
        var statement = "DROP TABLE IF EXISTS users";
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
            CREATE TABLE IF NOT EXISTS users (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(username),
              INDEX(email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
    private void configureDatabase() throws DataAccessException {
        ConfigureDatabase configureDatabase = new ConfigureDatabase();
        configureDatabase.configureDatabase(createStatements);
    }
}
