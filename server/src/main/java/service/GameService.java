package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import java.util.Collection;

import model.AuthData;
import model.GameData;
import model.JoinGameRequest;

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

    public Collection<GameData> listGames (String authenticationToken) throws DataAccessException {
        // verify authenticationToken
        if (authDAO.getAuthentication(authenticationToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // return the list of all games
        return gameDAO.listGames();
    }

    public void joinGame(String authenticationToken, JoinGameRequest request) throws DataAccessException {
        // verify token
        AuthData authentication = authDAO.getAuthentication(authenticationToken);
        if (authentication == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // get game
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request"); // Game doesn't exist
        }

        // verify color is valid
        if (request.playerColor() == null || (!request.playerColor().equals("WHITE") && !request.playerColor().equals("BLACK"))) {
            throw new DataAccessException("Error: bad request");
        }

        // assign to correct color based on request
        GameData updatedGame;
        if (request.playerColor().equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken"); // color is in use
            }
            // build new game with white username in use
            updatedGame = new GameData(game.gameID(), authentication.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken"); // color is in use
            }
            // build new game with black username in use
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), authentication.username(), game.gameName(), game.game());
        }

        // send game back to database
        gameDAO.updateGame(updatedGame);
    }
}
