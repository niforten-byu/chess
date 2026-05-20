package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.LoginRequest;
import model.UserData;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws DataAccessException {
        // validate the request
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // check if  username is taken
        if (userDAO.getUser(user.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        // create user in the database
        userDAO.createUser(user);

        // generate and return a new session token
        return authDAO.createAuthentication(user.username());
    }

    public AuthData login(LoginRequest request) throws DataAccessException {
        // validate login request
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // get user from database using username
        UserData user = userDAO.getUser(request.username());

        // check if user exists and password matches user
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        // generate new authentication token for user and return it
        return authDAO.createAuthentication(user.username());
    }
}
