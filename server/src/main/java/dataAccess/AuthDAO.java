package dataAccess;

import model.AuthData;

// Center for memory and sql DAO
public interface AuthDAO {
    void addAuthUser(AuthData authData) throws DataAccessException;
    boolean logout(String authToken) throws DataAccessException;
    String usernameForAuth(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
