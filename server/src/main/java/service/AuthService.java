package service;


import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import model.AuthData;

// Runs general AuthDAO; AuthDAO will decide to use Memory or SQL
// Methods should be the same name as the methods in the corresponding DAO
// All methods should throw DataAccessException
public class AuthService {
    private AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public boolean validateAuth(String authToken) throws DataAccessException {
        return authDAO.validateAuth(authToken);
    }
    public void addAuthUser(AuthData authData) throws DataAccessException {
        authDAO.addAuthUser(authData);
    }

    public boolean logout(String authToken) throws DataAccessException {
        return authDAO.logout(authToken);
    }
    public String usernameForAuth(String authToken) throws DataAccessException {
        return authDAO.usernameForAuth(authToken);
    }
    public void clear() throws DataAccessException {
        authDAO.clear();
    }
}
