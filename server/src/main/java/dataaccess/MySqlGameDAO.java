package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {

    public MySqlGameDAO() throws DataAccessException {
        // make sure database exists
        configureDatabase();
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        // create new game
        ChessGame newGame = new ChessGame();

        // turn game into JSON string
        String gameJson = new Gson().toJson(newGame);

        // insert game name and json for sql
        String statement = "INSERT INTO game (gameName, game) VALUES (?, ?)";

        // make sure we get generated keys
        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            // set variables in sql
            ps.setString(1, gameName);
            ps.setString(2, gameJson);

            // put into database
            ps.executeUpdate();

            // get the new game id, and return to service
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: unable to create game: ", e);
        }
        // throw error if game id was not created
        throw new DataAccessException("Error: unable to generate game ID");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        // sql query
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            // set game id in sql
            ps.setInt(1, gameID);

            // loop through to find chess game
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // get json from database
                    String gameJson = rs.getString("game");

                    // turn json into ChessGame object
                    ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);

                    // put everything into a GameData record
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            game
                    );
                }
            }
            // if game doesn't exist...
            return null;
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: unable to read game: ", e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();

        // get every game in table
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement);
             ResultSet rs = ps.executeQuery()) {
            // loop through result set grid to get all rows
            while(rs.next()) {
                String gameJson = rs.getString("game");

                // turn into game object
                ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);

                // add to collection
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        game
                ));
            }
            return games;
        }
        catch( SQLException e) {
            throw new DataAccessException("Error: unable to list games: ", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        // overwrite row in database with player and what color they are
        String statement = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            // set sql variables
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            // serialize newly update board
            ps.setString(4, new Gson().toJson(game.game()));
            ps.setInt(5, game.gameID());

            // update database
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: unable to update game: ", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // clear rows but not columns
        String statement = "TRUNCATE TABLE game";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {

            // clear database
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: unable to clear games: ", e);
        }
    }


    // helper functions

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS game (
              gameID INT NOT NULL AUTO_INCREMENT,
              whiteUsername VARCHAR(255),
              blackUsername VARCHAR(255),
              gameName VARCHAR(255) NOT NULL,
              game TEXT NOT NULL,
              PRIMARY KEY (gameID)
            )
            """
    };

    private void configureDatabase() throws DataAccessException {
        // make sure database exists
        DatabaseManager.createDatabase();

        try (Connection connect = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (PreparedStatement ps = connect.prepareStatement(statement)) {
                    // create table if it doesn't exist
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to configure database: ", e);
        }
    }
}
