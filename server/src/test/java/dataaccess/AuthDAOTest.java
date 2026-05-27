package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthDAOTest {
    private MySqlAuthDAO authDAO;

    @BeforeEach
    public void begin() throws DataAccessException {
        // clear database before each test
        authDAO = new MySqlAuthDAO();
        authDAO.clear();
    }

    @Test
    public void createAuthenticationSuccess() throws DataAccessException {
        // create an authentication for a user
        AuthData authentication = authDAO.createAuthentication("Hyoma Chigiri");

        // ensure it was actually created
        Assertions.assertNotNull(authentication);
        Assertions.assertNotNull(authentication.authToken());
        Assertions.assertEquals("Hyoma Chigiri", authentication.username());
    }

    @Test
    public void createAuthenticationFailure() {
        // try to make an authentication token for a non-existent user, should throw SQL exception
        Assertions.assertThrows(DataAccessException.class, () -> {
            authDAO.createAuthentication(null);
        });
    }

    @Test
    public void getAuthenticationSuccess() throws DataAccessException {
        // create authentication for user
        AuthData authentication = authDAO.createAuthentication("Gin Gagamaru");

        // get that authentication
        AuthData retrievedAuthentication = authDAO.getAuthentication(authentication.authToken());

        // make sure it actually got something and it matches the user's authentication
        Assertions.assertNotNull(retrievedAuthentication);
        Assertions.assertEquals("Gin Gagamaru", retrievedAuthentication.username());
    }

    @Test
    public void getAuthenticationFailure() throws DataAccessException {
        // try and get non-existent authentication token
        AuthData retrievedAuthentication = authDAO.getAuthentication("non-existent token");

        // should return a null when token doesn't exist, not crash
        Assertions.assertNull(retrievedAuthentication);
    }

    @Test
    public void deleteAuthenticationSuccess() throws DataAccessException {
        // create an authentication token
        AuthData authentication = authDAO.createAuthentication("Jingo Raichi");

        // delete authentication token
        authDAO.deleteAuthentication(authentication.authToken());

        // look for token (should be null since we deleted it)
        AuthData retrievedAuthentication = authDAO.getAuthentication(authentication.authToken());
        Assertions.assertNull(retrievedAuthentication);
    }

    @Test
    public void deleteAuthenticationFailure() throws DataAccessException {
        // create authentication for a user
        AuthData authentication = authDAO.createAuthentication("Ryusei Shidou");

        // attempt to delete a non-existent token
        authDAO.deleteAuthentication("non-existent token");

        // ensure created authentication token was not deleted
        AuthData retrievedAuthentication = authDAO.getAuthentication(authentication.authToken());
        Assertions.assertNotNull(retrievedAuthentication);
        Assertions.assertEquals("Ryusei Shidou", retrievedAuthentication.username());
    }

    @Test
    public void clearAuthenticationSuccess() throws DataAccessException {
        // create authentication token
        AuthData authentication = authDAO.createAuthentication("Tabito Karasu");

        // clear authentications
        authDAO.clear();

        // make sure they were actually cleared
        AuthData retrievedAuth = authDAO.getAuthentication(authentication.authToken());
        Assertions.assertNull(retrievedAuth);
    }

}
