package service;

import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import dataaccess.MySqlUserDAO;
import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTest {
    private MySqlUserDAO userDAO;
    private MySqlAuthDAO authDAO;
    private MySqlGameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();

        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();

        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    // ... leave all your @Test methods exactly the same! ...

    @Test
    public void clearSuccess() throws DataAccessException {
        // add test data to the databases
        userDAO.createUser(new UserData("duncan", "drowssap", "duncan@byu.edu"));
        authDAO.createAuthentication("duncan");
        gameDAO.createGame("Friday Night Magic: Chess edition");

        // verify databases obtained the data before clear
        Assertions.assertNotNull(userDAO.getUser("duncan"));
        Assertions.assertTrue(gameDAO.listGames().size() > 0);

        // call the clear service
        clearService.clear();

        // verify that everything has been cleared
        Assertions.assertNull(userDAO.getUser("duncan"));
        Assertions.assertEquals(0, gameDAO.listGames().size());
    }
}
