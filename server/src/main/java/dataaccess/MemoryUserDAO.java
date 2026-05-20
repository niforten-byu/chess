package dataaccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    // database table for users
    private final HashMap <String, UserData> users = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        users.put(user.username(), user);
    }

    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    public void clear() throws DataAccessException {
        users.clear();
    }
}