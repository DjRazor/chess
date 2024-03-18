package service;

import dataAccess.*;
import model.AuthData;
import model.UserData;

public class UserService {
    private UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public AuthData register(UserData user) throws DataAccessException {
        return userDAO.register(user);
    }
    public AuthData login(UserData user) throws DataAccessException {
        return userDAO.login(user);
    }
    public boolean userExists(String username) throws DataAccessException {
        return userDAO.userExists(username);
    }
    public boolean validateCreds(String username, String password) throws DataAccessException {
        return userDAO.validateCreds(username, password);
    }
    public void removeUser(String username) throws DataAccessException {
        userDAO.removeUser(username);
    }
    public void clear() throws DataAccessException {
        userDAO.clear();
    }
}
