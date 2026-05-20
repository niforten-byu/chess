package dataaccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO {
    // database table for users
    private final HashMap <String, UserData> users = new HashMap<>();

    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }
}
