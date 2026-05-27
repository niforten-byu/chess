package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAOTest {

    private MySqlUserDAO userDao;


    // setup database before each test
    @BeforeEach
    public void begin() throws DataAccessException {
        userDao = new MySqlUserDAO();
        userDao.clear();
    }

    // insert user and hash password
    @Test
    public void createUserSuccess() throws DataAccessException {
        // create user
        UserData user = new UserData("Yoichi Isagi", "bluelock", "bluelock@byu.edu");

        // save to database
        userDao.createUser(user);

        // make sure user actually got saved
        UserData retrievedUser = userDao.getUser("Yoichi Isagi");
        Assertions.assertNotNull(retrievedUser);
        Assertions.assertEquals("Yoichi Isagi", retrievedUser.username());
        Assertions.assertEquals("bluelock@byu.edu", retrievedUser.email());

        // check that the password was scambled but that BCrypt can verify that bluelock matches the hashing
        Assertions.assertNotEquals("bluelock", retrievedUser.password());
        Assertions.assertTrue(BCrypt.checkpw("bluelock", retrievedUser.password()));
    }

    @Test
    public void createUserFailure() throws DataAccessException {
        // create user
        UserData user = new UserData("Meguru Bachira", "bluelock", "theMonster@byu.edu");
        userDao.createUser(user);

        // try to make the same user again and ensure it fails
        Assertions.assertThrows(DataAccessException.class, () -> {
            userDao.createUser(user);
        });
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        // create user
        UserData user = new UserData("Rin Itoshi", "bluelock", "brotherIssues@byu.edu");
        userDao.createUser(user);

        // attmept to get user
        UserData retrievedUser = userDao.getUser("Rin Itoshi");

        // make sure data is correct
        Assertions.assertNotNull(retrievedUser);
        Assertions.assertEquals("brotherIssues@byu.edu", retrievedUser.email());
    }

    @Test
    public void getUserFailure() throws DataAccessException {
        // try to get non-existent user
        UserData retrievedUser = userDao.getUser("nobody");

        // dao should be null (and not crash program)
        Assertions.assertNull(retrievedUser);
    }

    @Test
    public void clearUsersSuccess() throws DataAccessException {
        // create a user
        UserData user = new UserData("Seishiro Nagi", "bluelock", "mrIveOnlyBeenPlayingFor6Months@byu.edu");
        userDao.createUser(user);

        // clear database table
        userDao.clear();

        // make sure database is actually clear
        UserData retrievedUser = userDao.getUser("Seishiro Nagi");
        Assertions.assertNull(retrievedUser);
    }
}
