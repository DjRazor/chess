package service;


import dataAccess.AuthDAO;
import dataAccess.MemoryAuthDAO;
import model.AuthData;

// Runs general AuthDAO; AuthDAO will decide to use Memory or SQL
// Methods should be the same name as the methods in the corresponding DAO
// All methods should throw DataAccessException
public class AuthService {
    AuthDAO authDAO = new MemoryAuthDAO();

    public void addAuthUser(AuthData authData) {
        authDAO.addAuthUser(authData);
    }
}
