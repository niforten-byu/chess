package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    // counter to create unique Game IDs automatically
    private int nextId = 1;

    // database table for games
    private final HashMap<Integer, GameData> games = new HashMap<>();

    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextId++;
        // create brand new game
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameID, game);
        return gameID;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    public void updateGame(GameData game) throws DataAccessException {
        // throw an error if they try to update a game that doesn't exist
        if (games.get(game.gameID()) == null) {
            throw new DataAccessException("Error: Game does not exist.");
        }

        // replace old game data with the new game data (example: when someone joins the game)
        games.put(game.gameID(), game);
    }

    public void clear() throws DataAccessException {
        games.clear();
        nextId = 1; // reset id counter back to 1 for unique ids
    }
}
