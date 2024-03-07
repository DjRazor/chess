package dataAccess;

import model.AuthData;

import java.util.HashSet;

public class MemoryAuthDAO implements AuthDAO {
    HashSet<AuthData> authorized = new HashSet<>();
    void createAuth() throws DataAccessException {

    }

    public void addAuthUser(AuthData authData) {
        // Checks if user has a previous authToken; replaces it if so
        for (AuthData authUser : authorized) {
            if (authUser.username().equals(authData.username())) {
                authorized.remove(authUser);
            }
        }
        authorized.add(authData);
    }

    void getAuth() throws DataAccessException {

    }

    void deleteAuth() throws DataAccessException {

    }
}
