package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public int createGame (String authenticationToken, String name) throws DataAccessException {
        // verify authenticationToken
        if (authDAO.getAuthentication(authenticationToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // validate name of game was received
        if (name == null || name.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        // create game and return id
        return gameDAO.createGame(name);
    }
}
