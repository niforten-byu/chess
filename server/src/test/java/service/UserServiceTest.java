package service;

// Make sure to import the MySQL versions!
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlUserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import model.LoginRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    private MySqlUserDAO userDAO;
    private MySqlAuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup() throws DataAccessException {
        // implement sql daos
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();

        // clear database before test
        userDAO.clear();
        authDAO.clear();

        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void registerSuccess() throws DataAccessException {
        // create a valid user record
        UserData newUser = new UserData("duncan", "drowssap", "duncan@byu.edu");

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
        UserData user = new UserData("duncan", "drowssap", "duncan@byu.edu");
        userService.register(user);

        // attempt to register the same user again (should fail)
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.register(user);
        });

        Assertions.assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void logoutSuccess() throws DataAccessException{
        // register a new user
        UserData user = new UserData("duncan", "drowssap", "duncan@byu.edu");
        AuthData auth = userService.register(user);

        // logout
        userService.logout(auth.authToken());

        // prove that the first logout worked by attempting a second logout
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.logout(auth.authToken());
        });
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void logoutFailBadToken() {
        // attempt logout with a token that doesn't exist
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.logout("FakeToken");
        });

        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test public void loginSuccess() throws DataAccessException {
        // register a new user in database
        userService.register(new UserData("duncan", "drowssap", "duncan@byu.edu"));

        // attempt login in with credentials that were registered
        LoginRequest request = new LoginRequest("duncan", "drowssap");
        AuthData auth = userService.login(request);

        // verify valid authentication returned
        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("duncan", auth.username());
    }

    @Test
    public void loginFailBadPassword() throws DataAccessException {
        // register a new user in database
        userService.register(new UserData("duncan", "drowssap", "duncan@byu.edu"));

        // attempt login with different credentials (bad passowrd)
        LoginRequest badRequest = new LoginRequest("duncan", "BAD_PASSWORD");

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            userService.login(badRequest);
        });

        // verify service caught bad password
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }
}
