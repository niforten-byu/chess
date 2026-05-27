package dataaccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() throws DataAccessException {
        // make sure database exists
        configureDatabase();
    }

    @Override
    public AuthData createAuthentication(String username) throws DataAccessException {
        // make authentication token
        String newAuthToken = UUID.randomUUID().toString();

        // sql query
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            // put variables in sql
            ps.setString(1, newAuthToken);
            ps.setString(2, username);

            // put it database
            ps.executeUpdate();
            return new AuthData(newAuthToken, username);
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: unable to create authentication: ", e);
        }
    }

    @Override
    public AuthData getAuthentication(String authenticationToken) throws DataAccessException {
        // get row matching authentication token
        String statement = "SELECT authToken, username FROM auth WHERE authToken = ?";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            // put variables in sql
            ps.setString(1, authenticationToken);

            try(ResultSet rs = ps.executeQuery()) {

                // find row with token
                if(rs.next()) {
                    // return that row's authToken and username
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
            }
            // row didn't exist, because token didn't exist
            return null;
        }
        catch(SQLException e) {
            throw new DataAccessException("Error: unable to read authentication: ", e);
        }
    }

    @Override
    public void deleteAuthentication(String authenticationToken) throws DataAccessException {
        // sql query to delete only one authentication
        String statement = "DELETE FROM auth WHERE authToken = ?";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            // put variables in sql statement
            ps.setString(1,authenticationToken);

            // delete from database
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: unable to delete authentication: ", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // delete all rows (but not columns)
        String statement = "TRUNCATE TABLE auth";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            // clear
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to clear auth: ", e);
        }
    }

    // helper functions

    // authentication token in primary key
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auth (
              authToken VARCHAR(255) NOT NULL,
              username VARCHAR(255) NOT NULL,
              PRIMARY KEY (authToken)
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
        }
        catch (SQLException e) {
            throw new DataAccessException("Error: unable to configure database: ", e);
        }
    }
}
