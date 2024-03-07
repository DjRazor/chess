package dataAccess;

import model.AuthData;

// Center for memory and sql DAO
public interface AuthDAO {
    void addAuthUser(AuthData authData);
}
