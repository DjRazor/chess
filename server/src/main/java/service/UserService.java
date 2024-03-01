package service;

import model.AuthData;
import model.UserData;

public class UserService {
    public AuthData register(UserData user) {
        String username = user.username();
        String password = user.password();
        return null;
    }
    public AuthData login(UserData user) {
        return null;
    }
    public void logout(UserData user) {

    }
}
