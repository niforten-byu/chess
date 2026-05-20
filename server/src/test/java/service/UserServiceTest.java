package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup() {
        // create empty DAOs for the test
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void registerSuccess() throws DataAccessException {
        // create a valid user record
        UserData newUser = new UserData("duncan", "password123", "duncan@byu.edu");

        AuthData result = userService.register(newUser);

        // check for returned valid token and user in database
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("duncan", result.username());
        Assertions.assertNotNull(userDAO.getUser("duncan"));
    }

    @Test
    public void registerFailDuplicateUser() throws DataAccessException {
        // register a user
        UserData user = new UserData("duncan", "password123", "duncan@byu.edu");
        userService.register(user);

        // attempt to register the same user again (should fail)
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.register(user);
        });

        Assertions.assertEquals("Error: already taken", exception.getMessage());
    }
}
