package dataaccess;

import model.AuthData;

public interface AuthDAO {
    AuthData createAuthentication(String username) throws DataAccessException;
    AuthData getAuthentication(String authToken) throws DataAccessException;
    void deleteAuthentication(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
