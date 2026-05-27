package dataaccess;

import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.Collection;

public class GameDAOTest {

    private MySqlGameDAO gameDAO;

    @BeforeEach
    public void begin() throws DataAccessException {
        // clear database before each test
        gameDAO = new MySqlGameDAO();
        gameDAO.clear();
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        // create a new game
        int gameID = gameDAO.createGame("Bluelock vs the world five");

        // make sure there is a id
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    public void createGameFailure() {
        // attempt to create a game with a null game name, which should not be possible
        Assertions.assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null);
        });
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        // create game
        int gameID = gameDAO.createGame("Itoshi vs Isagi");

        // get the game we just created from database
        GameData game = gameDAO.getGame(gameID);

        // make sure we actually got the right game and not a null, and that gson deserializes our games properly
        Assertions.assertNotNull(game);
        Assertions.assertEquals("Itoshi vs Isagi", game.gameName());
        Assertions.assertNotNull(game.game());
    }

    @Test
    public void getGameFailure() throws DataAccessException {
        // attempt to get non-existent game
        GameData game = gameDAO.getGame(1032003);

        // assert that we got a null
        Assertions.assertNull(game);
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        // make games to add to database
        gameDAO.createGame("Tryouts");
        gameDAO.createGame("The assassin and the ninja");
        gameDAO.createGame("the world you feel");

        // add games to collection
        Collection<GameData> games = gameDAO.listGames();

        // make sure we get the three games back
        Assertions.assertEquals(3, games.size());
    }

    @Test
    public void listGamesFailure() throws DataAccessException{
        // get an empty game list from database
        Collection<GameData> games = gameDAO.listGames();

        // make sure collection is empty
        Assertions.assertTrue(games.isEmpty());
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        // create a game and get it from database
        int gameID = gameDAO.createGame("Chameleon");
        GameData game = gameDAO.getGame(gameID);

        // claim white seat and create a new GameData object with info
        GameData updatedGame = new GameData(
                game.gameID(),
                "Eita Otoya",
                game.blackUsername(),
                game.gameName(),
                game.game()
        );

        // push updated game to database
        gameDAO.updateGame(updatedGame);

        // get game and make sure white seat was taken with right user
        GameData retrievedGame = gameDAO.getGame(gameID);
        Assertions.assertEquals("Eita Otoya", retrievedGame.whiteUsername());
    }

    @Test
    public void updateGameFailure() throws DataAccessException {
        // create a game in database
        int gameID = gameDAO.createGame("The Big Stage");
        GameData game = gameDAO.getGame(gameID);

        // corrupt game by making game name null (when it can't be)
        GameData badDataGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                null, // <-- THIS is the illegal data!
                game.game()
        );

        // attempt update, and make sure exception is thrown because it can't accept null
        Assertions.assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(badDataGame);
        });
    }

    @Test
    public void clearGamesSuccess() throws DataAccessException {
        // create a game so we have something to clear
        int gameID = gameDAO.createGame("FLOW");

        // clear it
        gameDAO.clear();

        // make sure it cleared
        GameData game = gameDAO.getGame(gameID);
        Assertions.assertNull(game);
    }

}
