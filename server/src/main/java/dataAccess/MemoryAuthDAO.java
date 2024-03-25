package dataAccess;

import model.AuthData;

import java.util.HashSet;

public class MemoryAuthDAO implements AuthDAO {
    HashSet<AuthData> authorized = new HashSet<>();
    public boolean validateAuth(String authToken) {
        for (AuthData authData : authorized) {
            if (authData.authToken().equals(authToken)) {
                return true;
            }
        }
        return false;
    }

    public void addAuthUser(AuthData authData) {
        authorized.add(authData);
    }

    public boolean logout(String authToken) {
        for (AuthData authUser : authorized) {
            if (authUser.authToken().equals(authToken)) {
                authorized.remove(authUser);
                return true;
            }
        }
        return false;
    }
    public String usernameForAuth(String authToken) {
        for (AuthData authData : authorized) {
            if (authData.authToken().equals(authToken)) {
                return authData.username();
            }
        }
        return null;
    }
    public void clear() {
        authorized = new HashSet<>();
    }
}
