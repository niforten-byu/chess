package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTest {

    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private GameService gameService;

    @BeforeEach
    public void setup() {
        // Spin up fresh databases and the service before each test
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        // make a valid token in database
        AuthData authentication = authDAO.createAuthentication("duncan");

        // create a game using token
        int gameID = gameService.createGame(authentication.authToken(), "Duncan's chess triumph for the ages");

        // verify valid id and name
        Assertions.assertTrue(gameID > 0);
        Assertions.assertNotNull(gameDAO.getGame(gameID));
        Assertions.assertEquals("Duncan's chess triumph for the ages", gameDAO.getGame(gameID).gameName());
    }

    @Test
    public void unauthorizedGameCreation() {
        // create a game with non-existent token
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.createGame("non-existent-token", "non-existent-game");
        });

        // verify it didn't work
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void createGameBadRequest() throws DataAccessException {
        // create authentication token
        AuthData authentication = authDAO.createAuthentication("duncan");

        // attempt to create a game but with no name
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.createGame(authentication.authToken(), null);
        });

        Assertions.assertEquals("Error: bad request", exception.getMessage());
    }
}
