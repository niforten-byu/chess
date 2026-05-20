package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTest {
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    public void setup() {
        // create empty DAOs for the test
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

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
