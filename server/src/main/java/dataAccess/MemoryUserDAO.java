package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.HashSet;
import java.util.UUID;

public class MemoryUserDAO implements UserDAO {
    private HashSet<UserData> users = new HashSet<>();
    private HashSet<String> usersList = new HashSet<>();
    public AuthData register(UserData user) {
       users.add(user);
       usersList.add(user.username());
       String authToken = UUID.randomUUID().toString();
       return new AuthData(authToken,user.username());
    }

    public boolean userExists(String username) {
        for (UserData user : users) {
            if (user.username().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean validateCreds(String username, String password) {
        for (UserData user : users) {
            if (user.username().equals(username)
            && user.password().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public AuthData login(UserData user) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(user.username(), authToken);
    }

    public void logout(UserData user) {

    }
}