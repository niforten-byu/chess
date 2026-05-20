package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO {
    // counter to create unique Game IDs automatically
    private int nextId = 1;

    // database table for games
    private final HashMap<Integer, GameData> games = new HashMap<>();

    public int createGame(String gameName) {
        int gameID = nextId++;
        // create brand new game
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameID, game);
        return gameID;
    }

    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    public Collection<GameData> listGames() {
        return games.values();
    }

    public void updateGame(GameData game) {
        // replace old game data with the new game data (example: when someone joins the game)
        games.put(game.gameID(), game);
    }

    public void clear() {
        games.clear();
        nextId = 1; // reset id counter back to 1 for unique ids
    }
}
