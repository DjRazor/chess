package dataAccess;

import model.AuthData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.UUID;

public class MemoryUserDAO implements UserDAO {
    private HashSet<UserData> users = new HashSet<>();
    private HashSet<String> usersList = new HashSet<>();
    public AuthData register(UserData user) {
       users.add(user);
       usersList.add(user.username());
       String authToken = UUID.randomUUID().toString();
       return new AuthData(authToken, user.username());
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
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        for (UserData user : users) {
            if (user.username().equals(username)
            && encoder.matches(password, user.password())) {
                return true;
            }
        }
        return false;
    }

    public AuthData login(UserData user) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, user.username());
    }
    public void removeUser(String username) {
        usersList.remove(username);
    }
    public void clear() {
        usersList = new HashSet<>();
        users = new HashSet<>();
    }
}
