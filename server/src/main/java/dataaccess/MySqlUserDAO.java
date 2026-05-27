package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO {

    public MySqlUserDAO() throws DataAccessException {
        // ensure database existence
        configureDatabase();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // scramble a users password using gensalt to add random data
        String scrambledPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        // sql query
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        // declare connect and ps before so they safely close during try and catch execution
        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {

            // insert variables into ? spots in sql query
            ps.setString(1, user.username());
            ps.setString(2, scrambledPassword);
            ps.setString(3, user.email());

            // update
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to create user: ", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        // read specific row where username matches request
        String statement = "SELECT username, password, email FROM user WHERE username = ?";

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            //plug in username
            ps.setString(1, username);

            // get rs beforehand to close properly
            try (ResultSet rs = ps.executeQuery()) {
                // check each row until found
                if (rs.next()) {
                    // extract data from current row (get col name, put into Java record, return to service)
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }
            // user didn't exit
            return null;
        }
        catch(SQLException e) {
            throw new DataAccessException("Error: unable to read user: ", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // delete all rows but keep table structure
        String statement = "TRUNCATE TABLE user";
        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement ps = connect.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to clear users: ", e);
        }
    }

    // Helper methods

    // array for table, username is unique primary key
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
              username VARCHAR(255) NOT NULL,
              password VARCHAR(255) NOT NULL,
              email VARCHAR(255) NOT NULL,
              PRIMARY KEY (username)
            )
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection connect = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (PreparedStatement ps = connect.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to configure database: ", e);
        }
    }
}