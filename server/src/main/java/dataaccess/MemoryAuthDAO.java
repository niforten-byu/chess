package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO {
    // database table for active sessions
    private final HashMap<String, AuthData> authentications = new HashMap<>();

    public AuthData createAuthentication(String username) {
        // generate a random secure token string
        String newAuthenticationToken = UUID.randomUUID().toString();
        AuthData authentication = new AuthData(newAuthenticationToken, username);

        authentications.put(newAuthenticationToken, authentication);
        return authentication;
    }

    public AuthData getAuthentication(String authToken) {
        return authentications.get(authToken);
    }

    public void deleteAuthentication(String authToken) {
        authentications.remove(authToken);
    }

    public void clear() {
        authentications.clear();
    }
}
