package dataAccess;

import model.AuthData;
import model.UserData;

public interface UserDAO {
      AuthData register(UserData user) throws DataAccessException;
      AuthData login(UserData user) throws DataAccessException;
      boolean userExists(String username) throws DataAccessException;
      boolean validateCreds(String username, String password) throws DataAccessException;
      void clear() throws DataAccessException;
}
