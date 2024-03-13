package dataAccess;

import model.AuthData;

public class SqlAuthDAO implements AuthDAO {
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
}
