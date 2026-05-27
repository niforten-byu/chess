package service;

import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.JoinGameRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTest {

    private MySqlAuthDAO authDAO;
    private MySqlGameDAO gameDAO;
    private GameService gameService;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();

        authDAO.clear();
        gameDAO.clear();

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

    @Test
    public void listGamesSuccess() throws DataAccessException {
        // make authenticationToken and put game in database
        AuthData auth = authDAO.createAuthentication("duncan");
        gameDAO.createGame("Gojo vs Geto chess match");

        // get games
        var games = gameService.listGames(auth.authToken());

        // make sure there is only one game
        Assertions.assertEquals(1, games.size());
    }

    @Test
    public void listGamesUnauthorized() {
        // try to get games with non-existent token
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.listGames("non-existent-token");
        });

        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        // create a user and game
        AuthData authentication = authDAO.createAuthentication("duncan");
        int gameID = gameService.createGame(authentication.authToken(), "Sukuna vs Mahoraga in the sky");

        // attempt to join as white
        JoinGameRequest request = new JoinGameRequest("WHITE", gameID);
        gameService.joinGame(authentication.authToken(), request);

        // verify user is white
        Assertions.assertEquals("duncan", gameDAO.getGame(gameID).whiteUsername());
        Assertions.assertNull(gameDAO.getGame(gameID).blackUsername()); // black should be empty
    }

    @Test
    public void joinGameUnauthorized() throws DataAccessException {
        // create a user and game
        AuthData auth = authDAO.createAuthentication("duncan");
        int gameID = gameService.createGame(auth.authToken(), "Mahito vs Yuji Itadori: The last time we'll curse each other");

        // attempt join with wron token
        JoinGameRequest request = new JoinGameRequest("BLACK", gameID);
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("non-existent-token", request);
        });

        // verify it didn't work
        Assertions.assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    public void joinGameSeatAlreadyTaken() throws DataAccessException {
        // create two users and game
        AuthData authentication1 = authDAO.createAuthentication("Megumi");
        AuthData authentication2 = authDAO.createAuthentication("Toji");
        int gameID = gameService.createGame(authentication1.authToken(), "Rabbit Escape");

        // user 1 joins game as white
        JoinGameRequest request1 = new JoinGameRequest("WHITE", gameID);
        gameService.joinGame(authentication1.authToken(), request1);

        // user 2 joins game, but also tries as white (naughty Toji)
        JoinGameRequest request2 = new JoinGameRequest("WHITE", gameID);
        DataAccessException exception = Assertions.assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(authentication2.authToken(), request2);
        });

        // make sure user 2 was not able to join as white
        Assertions.assertEquals("Error: already taken", exception.getMessage());
    }

}
