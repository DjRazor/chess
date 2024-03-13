package dataAccess;

import model.AuthData;
import model.UserData;

public class SqlUserDAO implements UserDAO {
    public AuthData register(UserData user) throws DataAccessException {
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
}
